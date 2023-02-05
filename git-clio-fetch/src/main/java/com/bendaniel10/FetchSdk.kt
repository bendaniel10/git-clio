package com.bendaniel10

import com.bendaniel10.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Integer.min
import java.time.OffsetDateTime
import kotlin.math.ceil

data class FetchSdkStartParams(
    val githubOrganization: String,
    val githubRepository: String,
    val analyticsStartDate: String,
    val analyticsEndDate: String
)

sealed class FetchSdkResponse {
    object NoResponse : FetchSdkResponse()
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
            // Emit pull requests
            processPullRequests(fetchSdkStartParams, coroutineScope)
            // Emit issues
            processIssues(fetchSdkStartParams, coroutineScope)
            // Done
            response.emit(FetchSdkResponse.Completed)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private suspend fun processIssues(fetchSdkStartParams: FetchSdkStartParams, coroutineScope: CoroutineScope) {
        fetchIssues(fetchSdkStartParams, 1).also { issues ->
            issues.items.map { issue ->
                coroutineScope.async {
                    emitIssueResponse(fetchSdkStartParams, issue)
                }
            }.awaitAll()


            val totalPages = ceil(issues.totalCount.toFloat() / 100f).toInt()
            for (currentPage in 2..min(MAX_GITHUB_SEARCH_FETCH_PAGE, totalPages)) {
                fetchIssues(fetchSdkStartParams, currentPage).also { otherIssues ->
                    otherIssues.items.map { issue ->
                        coroutineScope.async {
                            emitIssueResponse(fetchSdkStartParams, issue)
                        }
                    }.awaitAll()
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate = fetchIssues(fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                    .items.sortedByDescending { OffsetDateTime.parse(it.createdAt) }.first().createdAt
                println("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processIssues(
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate),
                    coroutineScope
                )
            }
        }
    }

    private suspend fun processPullRequests(fetchSdkStartParams: FetchSdkStartParams, coroutineScope: CoroutineScope) {
        fetchPullRequests(fetchSdkStartParams, 1).also { pullRequests ->
            pullRequests.items.map { pr ->
                coroutineScope.async {
                    emitPullRequestResponse(fetchSdkStartParams, pr)
                }

            }.awaitAll()

            val totalPages = ceil(pullRequests.totalCount.toFloat() / 100f).toInt()
            for (currentPage in 2..min(MAX_GITHUB_SEARCH_FETCH_PAGE, totalPages)) {
                fetchPullRequests(fetchSdkStartParams, currentPage).also { otherPullRequests ->
                    otherPullRequests.items.map { pr ->
                        coroutineScope.async {
                            emitPullRequestResponse(fetchSdkStartParams, pr)
                        }
                    }.awaitAll()
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate = fetchPullRequests(fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                    .items.sortedByDescending { OffsetDateTime.parse(it.createdAt) }.first().createdAt
                println("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processPullRequests(
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate),
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
            System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds.")
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
        System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds.")
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
        System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds.")
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
            System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds.")
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
            System.err.println("Failed, retrying in $MODERATE_DELAY milliseconds.")
            delay(MODERATE_DELAY)
            fetchIssueEvent(fetchSdkStartParams, issueNumber)
        }

    override fun response() = response
}

const val MODERATE_DELAY = 500L
const val MAX_GITHUB_SEARCH_FETCH_PAGE = 10