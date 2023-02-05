package com.bendaniel10

import com.bendaniel10.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

internal interface FetchRestApi {
    suspend fun fetchIssues(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: String,
        analyticsEndDate: String,
        page: Int
    ): FetchIssuesResponse

    suspend fun fetchPullRequests(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: String,
        analyticsEndDate: String,
        page: Int
    ): FetchPullRequestResponse

    suspend fun fetchPullRequestById(
        githubOrganization: String,
        githubRepository: String,
        id: Int
    ): FetchPullRequestByIdResponse

    suspend fun fetchPullRequestReviewsById(
        githubOrganization: String,
        githubRepository: String,
        id: Int
    ): FetchPullRequestReviewResponse

    suspend fun fetchIssueEventByIssueNumber(
        githubOrganization: String,
        githubRepository: String,
        issueNumber: Int
    ): IssueEventsResponse
}

internal class FetchRestApiImpl(private val httpClient: HttpClient) : FetchRestApi {
    override suspend fun fetchIssues(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: String,
        analyticsEndDate: String,
        page: Int
    ): FetchIssuesResponse =
        httpClient.get("https://api.github.com/search/issues?sort=created&order=asc&page=$page&per_page=$MAX_ITEMS_PER_PAGE&q=repo:$githubOrganization/$githubRepository+is:issue+created:$analyticsStartDate..$analyticsEndDate")
            .body()

    override suspend fun fetchPullRequests(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: String,
        analyticsEndDate: String,
        page: Int
    ): FetchPullRequestResponse =
        httpClient.get("https://api.github.com/search/issues?sort=created&order=asc&page=$page&per_page=$MAX_ITEMS_PER_PAGE&q=repo:$githubOrganization/$githubRepository+is:pr+created:$analyticsStartDate..$analyticsEndDate")
            .body()

    override suspend fun fetchPullRequestById(
        githubOrganization: String,
        githubRepository: String,
        id: Int
    ): FetchPullRequestByIdResponse =
        httpClient.get("https://api.github.com/repos/$githubOrganization/$githubRepository/pulls/$id")
            .body()

    override suspend fun fetchPullRequestReviewsById(
        githubOrganization: String,
        githubRepository: String,
        id: Int
    ): FetchPullRequestReviewResponse =
        httpClient.get("https://api.github.com/repos/$githubOrganization/$githubRepository/pulls/$id/reviews")
            .body()

    override suspend fun fetchIssueEventByIssueNumber(
        githubOrganization: String,
        githubRepository: String,
        issueNumber: Int
    ): IssueEventsResponse =
        httpClient.get("https://api.github.com/repos/$githubOrganization/$githubRepository/issues/$issueNumber/events")
            .body()
}

const val MAX_ITEMS_PER_PAGE = 100