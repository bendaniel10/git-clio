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
import java.lang.Integer.min
import kotlin.math.ceil

data class FetchSdkStartParams(
    val githubOrganization: String,
    val githubRepository: String,
    val analyticsStartDate: LocalDate,
    val analyticsEndDate: LocalDate
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
    suspend fun start(fetchSdkStartParams: FetchSdkStartParams, coroutineScope: CoroutineScope)
    fun response(): Flow<FetchSdkResponse>
}

class FetchSdkImpl : FetchSdk, KoinComponent {
    private val fetchRestApi: FetchRestApi by inject()
    private val response = MutableSharedFlow<FetchSdkResponse>(replay = 0, extraBufferCapacity = Channel.UNLIMITED)
    override suspend fun start(fetchSdkStartParams: FetchSdkStartParams, coroutineScope: CoroutineScope) {
        try {
            listOf(
                // Emit pull requests
                coroutineScope.launch {
                    processPullRequests(
                        fetchSdkStartParams,
                        coroutineScope,
                        emitTotalCount = true
                    )
                },
                // Emit issues
                coroutineScope.launch { processIssues(fetchSdkStartParams, coroutineScope, emitTotalCount = true) }
            ).joinAll()
            // Done
            response.emit(FetchSdkResponse.Completed)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private suspend fun processIssues(
        fetchSdkStartParams: FetchSdkStartParams,
        coroutineScope: CoroutineScope,
        emitTotalCount: Boolean = false
    ) {
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
                println("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processIssues(
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate.toLocalDateTime(TimeZone.UTC).date),
                    coroutineScope
                )
            }
        }
    }

    private suspend fun processPullRequests(
        fetchSdkStartParams: FetchSdkStartParams,
        coroutineScope: CoroutineScope,
        emitTotalCount: Boolean = false
    ) {
        fetchPullRequests(fetchSdkStartParams, 1).also { pullRequests ->
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
                fetchPullRequests(fetchSdkStartParams, currentPage).also { otherPullRequests ->
                    otherPullRequests.items.map { pr ->
                        coroutineScope.async(Dispatchers.IO) {
                            emitPullRequestResponse(fetchSdkStartParams, pr)
                        }
                    }.awaitAll()
                    delay(LARGE_DELAY)
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate = fetchPullRequests(fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                    .items.maxByOrNull { it.createdAt }!!.createdAt
                println("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processPullRequests(
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
                page
            )
        }.getOrElse {
            System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.")
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
            id
        )
    }.getOrElse {
        System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.")
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
            id
        )
    }.getOrElse {
        System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.")
        delay(MODERATE_DELAY)
        fetchPullRequestById(fetchSdkStartParams, id)
    }

    private suspend fun fetchPullRequests(
        fetchSdkStartParams: FetchSdkStartParams,
        page: Int
    ): FetchPullRequestResponse =
        runCatching {
            fetchRestApi.fetchPullRequests(
                fetchSdkStartParams.githubOrganization,
                fetchSdkStartParams.githubRepository,
                fetchSdkStartParams.analyticsStartDate,
                fetchSdkStartParams.analyticsEndDate,
                page
            )
        }.getOrElse {
            System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.")
            delay(MODERATE_DELAY)
            fetchPullRequests(fetchSdkStartParams, page)
        }

    private suspend fun fetchIssueEvent(
        fetchSdkStartParams: FetchSdkStartParams,
        issueNumber: Int
    ): IssueEventsResponse =
        runCatching {
            fetchRestApi.fetchIssueEventByIssueNumber(
                fetchSdkStartParams.githubOrganization,
                fetchSdkStartParams.githubRepository,
                issueNumber
            )
        }.getOrElse {
            System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds: ${it.message}.")
            delay(MODERATE_DELAY)
            fetchIssueEvent(fetchSdkStartParams, issueNumber)
        }

    override fun response() = response
}

const val MODERATE_DELAY = 2_500L
const val LARGE_DELAY = 7_000L
const val MAX_GITHUB_SEARCH_FETCH_PAGE = 10