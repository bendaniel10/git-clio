package com.bendaniel10

import com.bendaniel10.database.table.ReportEntity
import com.bendaniel10.database.table.ReportStatus
import com.bendaniel10.database.table.Tables
import com.bendaniel10.di.FetchModule
import com.bendaniel10.di.ParseModule
import com.bendaniel10.formatter.InfoBagCosmeticFormatter
import com.bendaniel10.parser.IssueParser
import com.bendaniel10.parser.PullRequestParser
import com.bendaniel10.persist.IssuePersister
import com.bendaniel10.persist.PullRequestPersister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


fun main() {
    GitClioParse.run()
}

@OptIn(ExperimentalTime::class)
object GitClioParse : KoinComponent {
    private val fetchSdk: FetchSdkImpl by inject()
    private val issueParser: IssueParser by inject()
    private val pullRequestParser: PullRequestParser by inject()
    private val infoBagCosmeticFormatter: InfoBagCosmeticFormatter by inject()
    private val database: Database by inject()
    private val issuePersister: IssuePersister by inject()
    private val pullRequestPersister: PullRequestPersister by inject()
    private val processedIssues = mutableSetOf<Int>()
    private val processedPullRequests = mutableSetOf<Int>()
    private var expectedTotalPullRequests = 0
    private var expectedTotalIssues = 0

    fun run() {
        val clioParseProjectProperties = ClioParseProjectProperties.fromPropertiesFile(File("local.properties"))
        measureTime {
            runBlocking {
                startKoin {
                    modules(
                        FetchModule.get(
                            clioParseProjectProperties.githubUsername,
                            clioParseProjectProperties.githubPersonalAccessToken
                        ),
                        ParseModule.get()
                    )
                }

                persistResponseToDatabase(clioParseProjectProperties)
                processInfoBag()
                launch {
                    fetchSdk.start(
                        fetchSdkStartParams = FetchSdkStartParams(
                            clioParseProjectProperties.githubOrganization,
                            clioParseProjectProperties.githubRepository,
                            clioParseProjectProperties.analyticsStartDate,
                            clioParseProjectProperties.analyticsEndDate,
                        ),
                        coroutineScope = this
                    )
                }
            }
        }.also {
            println("Analysis for ${clioParseProjectProperties.githubOrganization}/${clioParseProjectProperties.githubRepository} [${clioParseProjectProperties.analyticsStartDate} - ${clioParseProjectProperties.analyticsEndDate}]")
            println("It took ${it.inWholeMinutes} minutes (${it.inWholeSeconds} seconds) to run this analysis")
        }
    }

    private fun CoroutineScope.persistResponseToDatabase(clioParseProjectProperties: ClioParseProjectProperties) {
        database.also {
            transaction {
                Tables.list.forEach {
                    SchemaUtils.create(it)
                }
            }
        }

        val reportEntity = transaction {
            ReportEntity.new {
                name =
                    "GitHub Report [${clioParseProjectProperties.analyticsStartDate} - ${clioParseProjectProperties.analyticsEndDate}]"
                reportStatus = ReportStatus.PROCESSING
                created = LocalDateTime.now()
                analyticsStartDate = clioParseProjectProperties.analyticsEndDate.toJavaLocalDate()
                analyticsEndDate = clioParseProjectProperties.analyticsEndDate.toJavaLocalDate()
                processingStartedAt = LocalDateTime.now()
            }
        }

        launch {
            fetchSdk.response().collect { fetchSdkResponse ->
                when (fetchSdkResponse) {
                    FetchSdkResponse.Completed -> transaction {
                        reportEntity.processingCompletedAt = LocalDateTime.now()
                    }

                    is FetchSdkResponse.ExpectedIssuesTotal, is FetchSdkResponse.ExpectedPullRequestTotal -> Unit
                    is FetchSdkResponse.Issue -> issuePersister.persist(fetchSdkResponse, reportEntity)
                    is FetchSdkResponse.PullRequest -> pullRequestPersister.persist(fetchSdkResponse, reportEntity)
                }
            }
        }
    }

    private fun CoroutineScope.processInfoBag() {
        val json = Json { encodeDefaults = true }
        val infoBag = InfoBag()

        launch {
            fetchSdk.response().collect { fetchSdkResponse ->
                when (fetchSdkResponse) {
                    is FetchSdkResponse.Issue -> issueParser.parse(fetchSdkResponse, infoBag)
                        .also {
                            processedIssues.add(fetchSdkResponse.fetchIssuesItem.number)
                            print("\rProcessed issue: ${processedIssues.size}/$expectedTotalIssues")
                        }

                    is FetchSdkResponse.PullRequest -> pullRequestParser.parse(fetchSdkResponse, infoBag)
                        .also {
                            processedPullRequests.add(fetchSdkResponse.fetchPullRequestItem.number)
                            print("\rProcessed pull requests: ${processedPullRequests.size}/$expectedTotalPullRequests")
                        }

                    FetchSdkResponse.Completed -> {
                        infoBagCosmeticFormatter.format(infoBag)
                        with(File("github_report.json")) {
                            if (exists().not()) {
                                @Suppress("BlockingMethodInNonBlockingContext") createNewFile()
                            }
                            writeText(json.encodeToString(infoBag))
                            println("File can be found here: ${this.absolutePath}")
                        }
                        cancel()
                    }

                    is FetchSdkResponse.ExpectedIssuesTotal -> expectedTotalIssues = fetchSdkResponse.expected
                    is FetchSdkResponse.ExpectedPullRequestTotal -> expectedTotalPullRequests =
                        fetchSdkResponse.expected
                }
            }
        }
    }
}

data class ClioParseProjectProperties(
    val githubUsername: String,
    val githubPersonalAccessToken: String,
    val githubOrganization: String,
    val githubRepository: String,
    val analyticsStartDate: LocalDate,
    val analyticsEndDate: LocalDate
) {
    companion object {
        fun fromPropertiesFile(propertiesFile: File) = with(propertiesFile) {
            if (exists().not()) throw IllegalStateException("Please create add a local.properties file to the root of the project")
            val properties = Properties().apply { load(inputStream()) }
            ClioParseProjectProperties(
                properties.getProperty("githubUsername"),
                properties.getProperty("githubPersonalAccessToken"),
                properties.getProperty("githubOrganization"),
                properties.getProperty("githubRepository"),
                properties.getProperty("analyticsStartDate").toLocalDate(),
                properties.getProperty("analyticsEndDate").toLocalDate(),
            )
        }
    }
}
