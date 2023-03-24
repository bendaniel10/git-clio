package com.bendaniel10

import com.bendaniel10.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.lang.Integer.min
import kotlin.math.ceil

data class FetchSdkStartParams(
    val githubOrganization: String,
    val githubRepository: String,
    val analyticsStartDate: LocalDate,
    val analyticsEndDate: LocalDate,
    val githubUsername: String,
    val githubPersonalAccessToken: String,
)

sealed class FetchSdkResponse {
    data class ExpectedPullRequestTotal(val expected: Int) : FetchSdkResponse()
    data class ExpectedIssuesTotal(val expected: Int) : FetchSdkResponse()
    object Completed : FetchSdkResponse()
    data class Issue(val fetchIssuesItem: FetchIssuesItem, val fetchIssueEvent: IssueEventsResponse) :
        FetchSdkResponse()

    data class PullRequest(
        val fetchPullRequestItem: FetchPullRequestItem,
        val fetchPullRequestByIdResponse: FetchPullRequestByIdResponse,
        val fetchPullRequestReviewResponse: FetchPullRequestReviewResponse
    ) : FetchSdkResponse()
}

interface FetchSdk {
    fun start(fetchSdkStartParams: FetchSdkStartParams, coroutineScope: CoroutineScope)
    fun response(): Flow<FetchSdkResponse>
}

class FetchSdkImpl : FetchSdk, KoinComponent {
    private val logger = LoggerFactory.getLogger(FetchSdkImpl::class.java)
    private val fetchRestApi: FetchRestApi by inject()
    private val response = MutableSharedFlow<FetchSdkResponse>(replay = 0, extraBufferCapacity = Channel.UNLIMITED)
    override fun start(fetchSdkStartParams: FetchSdkStartParams, coroutineScope: CoroutineScope) {
        try {
            // Check for when issues and prs are completed
            coroutineScope.launch {
                var expectedTotalIssues: Int? = null
                var expectedTotalPrs: Int? = null
                val processedIds = mutableSetOf<Int>()

                response.collect {
                    when (it) {
                        FetchSdkResponse.Completed -> cancel()
                        is FetchSdkResponse.ExpectedIssuesTotal -> expectedTotalIssues = it.expected
                        is FetchSdkResponse.ExpectedPullRequestTotal -> expectedTotalPrs = it.expected
                        is FetchSdkResponse.Issue -> {
                            processedIds.add(it.fetchIssuesItem.number)
                            notifyIfCompleted((expectedTotalIssues ?: 0) + (expectedTotalPrs ?: 0), processedIds.size)
                        }

                        is FetchSdkResponse.PullRequest -> {
                            processedIds.add(it.fetchPullRequestItem.number)
                            notifyIfCompleted((expectedTotalIssues ?: 0) + (expectedTotalPrs ?: 0), processedIds.size)
                        }
                    }
                    if (expectedTotalIssues == 0 && expectedTotalPrs == 0) {
                        notifyIfCompleted(0, processedIds.size)
                    }
                }
            }

            // Emit pull requests
            coroutineScope.launch {
                logger.info("Starting to process pull requests")
                val defaultBranchName = fetchDefaultBranchName(fetchSdkStartParams)
                processPullRequests(
                    defaultBranchName,
                    fetchSdkStartParams,
                    coroutineScope,
                    emitTotalCount = true
                )
            }

            coroutineScope.launch {
                logger.info("Starting to process issues")
                processIssues(
                    fetchSdkStartParams,
                    coroutineScope,
                    emitTotalCount = true
                )
            }
        } catch (ex: Exception) {
            logger.error("Error while starting the FetchSdk", ex)
        }
    }

    private suspend fun notifyIfCompleted(total: Int, expected: Int) {
        if (total == expected) {
            logger.info("Processing completed")
            response.emit(FetchSdkResponse.Completed)
        }
    }

    private suspend fun processIssues(
        fetchSdkStartParams: FetchSdkStartParams,
        coroutineScope: CoroutineScope,
        emitTotalCount: Boolean = false
    ): Unit = withContext(Dispatchers.IO) {
        fetchIssues(fetchSdkStartParams, 1).also { issues ->
            if (emitTotalCount) {
                response.emit(FetchSdkResponse.ExpectedIssuesTotal(issues.totalCount))
            }
            issues.items.map { issue ->
                coroutineScope.async(Dispatchers.IO) {
                    emitIssueResponse(fetchSdkStartParams, issue)
                }
            }.awaitAll()
            delay(LARGE_DELAY)

            val totalPages = ceil(issues.totalCount.toFloat() / 100f).toInt()
            for (currentPage in 2..min(MAX_GITHUB_SEARCH_FETCH_PAGE, totalPages)) {
                fetchIssues(fetchSdkStartParams, currentPage).also { otherIssues ->
                    otherIssues.items.map { issue ->
                        coroutineScope.async(Dispatchers.IO) {
                            emitIssueResponse(fetchSdkStartParams, issue)
                        }
                    }.awaitAll()
                    delay(LARGE_DELAY)
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate = fetchIssues(fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                    .items.maxByOrNull { it.createdAt }!!.createdAt
                logger.info("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processIssues(
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate.toLocalDateTime(TimeZone.UTC).date),
                    coroutineScope
                )
            }
        }
    }

    private suspend fun processPullRequests(
        defaultBranchName: String,
        fetchSdkStartParams: FetchSdkStartParams,
        coroutineScope: CoroutineScope,
        emitTotalCount: Boolean = false
    ): Unit = withContext(Dispatchers.IO) {
        fetchPullRequests(defaultBranchName, fetchSdkStartParams, 1).also { pullRequests ->
            if (emitTotalCount) {
                response.emit(FetchSdkResponse.ExpectedPullRequestTotal(pullRequests.totalCount))
            }
            pullRequests.items.map { pr ->
                coroutineScope.async(Dispatchers.IO) {
                    emitPullRequestResponse(fetchSdkStartParams, pr)
                }

            }.awaitAll()
            delay(LARGE_DELAY)

            val totalPages = ceil(pullRequests.totalCount.toFloat() / 100f).toInt()
            for (currentPage in 2..min(MAX_GITHUB_SEARCH_FETCH_PAGE, totalPages)) {
                fetchPullRequests(defaultBranchName, fetchSdkStartParams, currentPage).also { otherPullRequests ->
                    otherPullRequests.items.map { pr ->
                        coroutineScope.async(Dispatchers.IO) {
                            emitPullRequestResponse(fetchSdkStartParams, pr)
                        }
                    }.awaitAll()
                    delay(LARGE_DELAY)
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate =
                    fetchPullRequests(defaultBranchName, fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                        .items.maxByOrNull { it.createdAt }!!.createdAt
                logger.info("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processPullRequests(
                    defaultBranchName,
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate.toLocalDateTime(TimeZone.UTC).date),
                    coroutineScope
                )
            }
        }
    }

    private suspend fun emitIssueResponse(fetchSdkStartParams: FetchSdkStartParams, issues: FetchIssuesItem) {
        response.emit(
            FetchSdkResponse.Issue(
                issues,
                fetchIssueEvent(fetchSdkStartParams, issues.number)
            )
        )
    }

    private suspend fun emitPullRequestResponse(
        fetchSdkStartParams: FetchSdkStartParams,
        pr: FetchPullRequestItem
    ) {
        response.emit(
            FetchSdkResponse.PullRequest(
                pr,
                fetchPullRequestById(fetchSdkStartParams, pr.number),
                fetchPullRequestReviewsById(fetchSdkStartParams, pr.number)
            )
        )
    }

    private suspend fun fetchIssues(fetchSdkStartParams: FetchSdkStartParams, page: Int): FetchIssuesResponse =
        runCatching {
            fetchRestApi.fetchIssues(
                fetchSdkStartParams.githubOrganization,
                fetchSdkStartParams.githubRepository,
                fetchSdkStartParams.analyticsStartDate,
                fetchSdkStartParams.analyticsEndDate,
                fetchSdkStartParams.githubUsername,
                fetchSdkStartParams.githubPersonalAccessToken,
                page
            )
        }.getOrElse {
            logger.error("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.", it)
            delay(MODERATE_DELAY)
            fetchIssues(fetchSdkStartParams, page)
        }

    private suspend fun fetchPullRequestReviewsById(
        fetchSdkStartParams: FetchSdkStartParams,
        id: Int
    ): FetchPullRequestReviewResponse = runCatching {
        fetchRestApi.fetchPullRequestReviewsById(
            fetchSdkStartParams.githubOrganization,
            fetchSdkStartParams.githubRepository,
            fetchSdkStartParams.githubUsername,
            fetchSdkStartParams.githubPersonalAccessToken,
            id
        )
    }.getOrElse {
        logger.error("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.", it)
        delay(MODERATE_DELAY)
        fetchPullRequestReviewsById(fetchSdkStartParams, id)
    }

    private suspend fun fetchPullRequestById(
        fetchSdkStartParams: FetchSdkStartParams,
        id: Int
    ): FetchPullRequestByIdResponse = runCatching {
        fetchRestApi.fetchPullRequestById(
            fetchSdkStartParams.githubOrganization,
            fetchSdkStartParams.githubRepository,
            fetchSdkStartParams.githubUsername,
            fetchSdkStartParams.githubPersonalAccessToken,
            id
        )
    }.getOrElse {
        logger.error("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.", it)
        delay(MODERATE_DELAY)
        fetchPullRequestById(fetchSdkStartParams, id)
    }

    private suspend fun fetchPullRequests(
        defaultBranchName: String,
        fetchSdkStartParams: FetchSdkStartParams,
        page: Int
    ): FetchPullRequestResponse =
        runCatching {
            fetchRestApi.fetchPullRequests(
                fetchSdkStartParams.githubOrganization,
                fetchSdkStartParams.githubRepository,
                fetchSdkStartParams.analyticsStartDate,
                fetchSdkStartParams.analyticsEndDate,
                fetchSdkStartParams.githubUsername,
                fetchSdkStartParams.githubPersonalAccessToken,
                defaultBranchName,
                page
            )
        }.getOrElse {
            logger.error("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.", it)
            delay(MODERATE_DELAY)
            fetchPullRequests(defaultBranchName, fetchSdkStartParams, page)
        }

    private suspend fun fetchIssueEvent(
        fetchSdkStartParams: FetchSdkStartParams,
        issueNumber: Int
    ): IssueEventsResponse =
        runCatching {
            fetchRestApi.fetchIssueEventByIssueNumber(
                fetchSdkStartParams.githubOrganization,
                fetchSdkStartParams.githubRepository,
                fetchSdkStartParams.githubUsername,
                fetchSdkStartParams.githubPersonalAccessToken,
                issueNumber
            )
        }.getOrElse {
            logger.error("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.", it)
            delay(MODERATE_DELAY)
            fetchIssueEvent(fetchSdkStartParams, issueNumber)
        }

    private suspend fun fetchDefaultBranchName(
        fetchSdkStartParams: FetchSdkStartParams
    ): String = runCatching {
        fetchRestApi.fetchDefaultPRBranch(
            fetchSdkStartParams.githubOrganization,
            fetchSdkStartParams.githubRepository,
            fetchSdkStartParams.githubUsername,
            fetchSdkStartParams.githubPersonalAccessToken,
        ).defaultBranch
    }.getOrElse {
        logger.error("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.", it)
        delay(MODERATE_DELAY)
        fetchDefaultBranchName(fetchSdkStartParams)
    }

    override fun response() = response
}

const val MODERATE_DELAY = 2_500L
const val LARGE_DELAY = 7_000L
const val MAX_GITHUB_SEARCH_FETCH_PAGE = 10