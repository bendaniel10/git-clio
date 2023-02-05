package com.bendaniel10

import com.bendaniel10.model.*
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
    suspend fun start(fetchSdkStartParams: FetchSdkStartParams)
    fun response(): Flow<FetchSdkResponse>
}

class FetchSdkImpl : FetchSdk, KoinComponent {
    private val fetchRestApi: FetchRestApi by inject()
    private val response = MutableSharedFlow<FetchSdkResponse>(replay = 0, extraBufferCapacity = Channel.UNLIMITED)
    override suspend fun start(fetchSdkStartParams: FetchSdkStartParams) {
        try {
            // Emit pull requests
            processPullRequests(fetchSdkStartParams)
            // Emit issues
            processIssues(fetchSdkStartParams)
            // Done
            response.tryEmit(FetchSdkResponse.Completed)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private suspend fun processIssues(fetchSdkStartParams: FetchSdkStartParams) {
        fetchIssues(fetchSdkStartParams, 1).also { issues ->
            for (issue in issues.items) {
                emitIssueResponse(fetchSdkStartParams, issue)
            }

            val totalPages = ceil(issues.totalCount.toFloat() / 100f).toInt()
            for (currentPage in 2..min(MAX_GITHUB_SEARCH_FETCH_PAGE, totalPages)) {
                fetchIssues(fetchSdkStartParams, currentPage).also { otherIssues ->
                    for (issue in otherIssues.items) {
                        emitIssueResponse(fetchSdkStartParams, issue)
                    }
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate = fetchIssues(fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                    .items.sortedByDescending { OffsetDateTime.parse(it.createdAt) }.first().createdAt
                println("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processIssues(
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate)
                )
            }
        }
    }

    private suspend fun processPullRequests(fetchSdkStartParams: FetchSdkStartParams) {
        fetchPullRequests(fetchSdkStartParams, 1).also { pullRequests ->
            for (pr in pullRequests.items) {
                emitPullRequestResponse(fetchSdkStartParams, pr)
            }

            val totalPages = ceil(pullRequests.totalCount.toFloat() / 100f).toInt()
            for (currentPage in 2..min(MAX_GITHUB_SEARCH_FETCH_PAGE, totalPages)) {
                fetchPullRequests(fetchSdkStartParams, currentPage).also { otherPullRequests ->
                    for (pr in otherPullRequests.items) {
                        emitPullRequestResponse(fetchSdkStartParams, pr)
                    }
                }
            }
            if (totalPages > MAX_GITHUB_SEARCH_FETCH_PAGE) {
                val newAnalyticsStartDate = fetchPullRequests(fetchSdkStartParams, MAX_GITHUB_SEARCH_FETCH_PAGE)
                    .items.sortedByDescending { OffsetDateTime.parse(it.createdAt) }.first().createdAt
                println("Total pages more than $MAX_GITHUB_SEARCH_FETCH_PAGE, splitting and start from: $newAnalyticsStartDate to ${fetchSdkStartParams.analyticsEndDate}")
                processPullRequests(
                    fetchSdkStartParams.copy(analyticsStartDate = newAnalyticsStartDate)
                )
            }
        }
    }

    private suspend fun emitIssueResponse(fetchSdkStartParams: FetchSdkStartParams, issues: FetchIssuesItem) {
        response.tryEmit(
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
        response.tryEmit(
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