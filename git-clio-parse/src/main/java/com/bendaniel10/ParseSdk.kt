package com.bendaniel10

import com.bendaniel10.database.table.ReportEntity
import com.bendaniel10.database.table.ReportStatus
import com.bendaniel10.database.table.Reports
import com.bendaniel10.database.table.Tables
import com.bendaniel10.persist.IssuePersister
import com.bendaniel10.persist.PullRequestPersister
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

interface ParseSdk {
    fun newParseReport(reportProperties: ReportProperties): ProcessReportResult
    fun startProcessingReport(reportId: Int): ProcessReportResult
    fun fetchAllReports(): List<ReportEntity>
    fun onStart()
}

internal class ParseSdkImpl : ParseSdk, KoinComponent {
    private val logger = LoggerFactory.getLogger(ParseSdkImpl::class.java)
    private val fetchSdk: FetchSdkImpl by inject()
    private val pullRequestPersister: PullRequestPersister by inject()
    private val issuePersister: IssuePersister by inject()
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    private val database: Database by inject()
    override fun onStart() {
        initializeDatabase()
        checkForWaitingToBeProcessedReports()
    }

    private fun initializeDatabase() {
        database.also {
            transaction {
                addLogger(Slf4jSqlDebugLogger)
                Tables.list.forEach {
                    logger.info("Initializing table ${it.tableName}")
                    SchemaUtils.createMissingTablesAndColumns(it)
                }
            }
        }
    }

    private fun checkForWaitingToBeProcessedReports() {
        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                val nextReportToProcess = newSuspendedTransaction {
                    val idle = ReportEntity.find {
                        Reports.reportStatus eq ReportStatus.PROCESSING
                    }.empty()

                    if (idle) {
                        logger.info("No report is being processed, checking for awaiting reports")
                        ReportEntity.find {
                            Reports.reportStatus eq ReportStatus.WAITING_TO_BE_PROCESSED
                        }.orderBy(Reports.created to SortOrder.DESC).limit(1).firstOrNull()
                    } else {
                        null
                    }
                }
                if (nextReportToProcess != null) {
                    logger.info("Found awaiting report to process: ${nextReportToProcess.name}")
                    startProcessingReport(nextReportToProcess.id.value)
                }
                delay(30_000)
            }
        }
    }

    override fun newParseReport(reportProperties: ReportProperties): ProcessReportResult {
        return startProcessingReport(createProject(reportProperties).id.value)
    }

    override fun startProcessingReport(reportId: Int): ProcessReportResult {
        val reportEntity = transaction { ReportEntity.findById(reportId) }
            ?: return ProcessReportResult.Failure

        return transaction {
            val stillProcessing = ReportEntity.find { Reports.reportStatus eq ReportStatus.PROCESSING }.empty().not()
            if (stillProcessing) {
                logger.info("Adding report to queue: ${reportEntity.name}")
                reportEntity.reportStatus = ReportStatus.WAITING_TO_BE_PROCESSED
                ProcessReportResult.Queued
            } else {
                logger.info("Starting to process report: ${reportEntity.name}")
                reportEntity.reportStatus = ReportStatus.PROCESSING
                reportEntity.processingStartedAt = LocalDateTime.now()
                fetchAndSaveGithubReport(reportEntity)
                ProcessReportResult.Successful
            }
        }
    }

    override fun fetchAllReports(): List<ReportEntity> = transaction {
        ReportEntity.all().toList()
    }

    private fun fetchAndSaveGithubReport(reportEntity: ReportEntity) {
        coroutineScope.launch {
            fetchSdk.response().collect { fetchSdkResponse ->
                when (fetchSdkResponse) {
                    FetchSdkResponse.Completed -> {
                        transaction {
                            reportEntity.reportStatus = ReportStatus.PROCESSED
                            reportEntity.processingCompletedAt = LocalDateTime.now()
                        }
                        cancel()
                    }

                    is FetchSdkResponse.ExpectedIssuesTotal -> {
                        newSuspendedTransaction {
                            reportEntity.totalIssues = fetchSdkResponse.expected
                        }
                    }

                    is FetchSdkResponse.ExpectedPullRequestTotal -> {
                        newSuspendedTransaction {
                            reportEntity.totalPullRequests = fetchSdkResponse.expected
                        }
                    }

                    is FetchSdkResponse.Issue -> issuePersister.persist(fetchSdkResponse, reportEntity)
                    is FetchSdkResponse.PullRequest -> pullRequestPersister.persist(fetchSdkResponse, reportEntity)
                }
            }
        }
        coroutineScope.launch {
            fetchSdk.start(
                fetchSdkStartParams = FetchSdkStartParams(
                    reportEntity.githubOrganization,
                    reportEntity.githubRepository,
                    reportEntity.analyticsStartDate.toKotlinLocalDate(),
                    reportEntity.analyticsEndDate.toKotlinLocalDate(),
                    reportEntity.gitHubUsername,
                    reportEntity.gitHubAccessToken
                ),
                this
            )
        }
    }

    private fun createProject(reportProperties: ReportProperties) = transaction {
        logger.info("Creating report: ${reportProperties.name}")
        ReportEntity.new {
            name = reportProperties.name
            reportStatus = ReportStatus.NOT_PROCESSED
            created = LocalDateTime.now()
            analyticsStartDate = reportProperties.analyticsStartDate.toJavaLocalDate()
            analyticsEndDate = reportProperties.analyticsEndDate.toJavaLocalDate()
            gitHubUsername = reportProperties.githubUsername
            gitHubAccessToken = reportProperties.githubPersonalAccessToken
            githubOrganization = reportProperties.githubOrganization
            githubRepository = reportProperties.githubRepository
        }
    }
}

data class ReportProperties(
    val name: String,
    val githubUsername: String,
    val githubPersonalAccessToken: String,
    val githubOrganization: String,
    val githubRepository: String,
    val analyticsStartDate: LocalDate,
    val analyticsEndDate: LocalDate
)

sealed class ProcessReportResult {
    object Successful : ProcessReportResult()
    object Queued : ProcessReportResult()
    object Failure : ProcessReportResult()
}
