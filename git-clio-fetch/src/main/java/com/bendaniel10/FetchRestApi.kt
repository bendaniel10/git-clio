package com.bendaniel10

import com.bendaniel10.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import kotlinx.datetime.LocalDate

internal interface FetchRestApi {
    suspend fun fetchIssues(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: LocalDate,
        analyticsEndDate: LocalDate,
        githubUsername: String,
        githubPersonalAccessToken: String,
        page: Int
    ): FetchIssuesResponse

    suspend fun fetchPullRequests(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: LocalDate,
        analyticsEndDate: LocalDate,
        githubUsername: String,
        githubPersonalAccessToken: String,
        defaultBranchName: String,
        page: Int
    ): FetchPullRequestResponse

    suspend fun fetchPullRequestById(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
        id: Int
    ): FetchPullRequestByIdResponse

    suspend fun fetchPullRequestReviewsById(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
        id: Int
    ): FetchPullRequestReviewResponse

    suspend fun fetchIssueEventByIssueNumber(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
        issueNumber: Int
    ): IssueEventsResponse

    suspend fun fetchDefaultPRBranch(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String
    ): FetchDefaultBranchNameResponse
}

internal class FetchRestApiImpl(private val httpClient: HttpClient) : FetchRestApi {
    override suspend fun fetchDefaultPRBranch(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
    ): FetchDefaultBranchNameResponse = httpClient.withBasicAuthentication(githubUsername, githubPersonalAccessToken)
        .get("https://api.github.com/repos/$githubOrganization/$githubRepository")
        .body()

    override suspend fun fetchIssues(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: LocalDate,
        analyticsEndDate: LocalDate,
        githubUsername: String,
        githubPersonalAccessToken: String,
        page: Int
    ): FetchIssuesResponse = httpClient.withBasicAuthentication(githubUsername, githubPersonalAccessToken)
        .get("https://api.github.com/search/issues?sort=created&order=asc&page=$page&per_page=$MAX_ITEMS_PER_PAGE&q=repo:$githubOrganization/$githubRepository+is:issue+created:$analyticsStartDate..$analyticsEndDate")
        .body()

    override suspend fun fetchPullRequests(
        githubOrganization: String,
        githubRepository: String,
        analyticsStartDate: LocalDate,
        analyticsEndDate: LocalDate,
        githubUsername: String,
        githubPersonalAccessToken: String,
        defaultBranchName: String,
        page: Int
    ): FetchPullRequestResponse = httpClient.withBasicAuthentication(githubUsername, githubPersonalAccessToken)
        .get("https://api.github.com/search/issues?sort=created&order=asc&page=$page&per_page=$MAX_ITEMS_PER_PAGE&q=repo:$githubOrganization/$githubRepository+is:pr+base:$defaultBranchName+created:$analyticsStartDate..$analyticsEndDate")
        .body()

    override suspend fun fetchPullRequestById(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
        id: Int
    ): FetchPullRequestByIdResponse = httpClient.withBasicAuthentication(githubUsername, githubPersonalAccessToken)
        .get("https://api.github.com/repos/$githubOrganization/$githubRepository/pulls/$id").body()

    override suspend fun fetchPullRequestReviewsById(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
        id: Int
    ): FetchPullRequestReviewResponse = httpClient.withBasicAuthentication(githubUsername, githubPersonalAccessToken)
        .get("https://api.github.com/repos/$githubOrganization/$githubRepository/pulls/$id/reviews").body()

    override suspend fun fetchIssueEventByIssueNumber(
        githubOrganization: String,
        githubRepository: String,
        githubUsername: String,
        githubPersonalAccessToken: String,
        issueNumber: Int,
    ): IssueEventsResponse = httpClient.withBasicAuthentication(githubUsername, githubPersonalAccessToken)
        .get("https://api.github.com/repos/$githubOrganization/$githubRepository/issues/$issueNumber/events").body()

    private fun HttpClient.withBasicAuthentication(username: String, password: String) = config {
        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(
                        username, password
                    )
                }
            }
        }
    }
}

const val MAX_ITEMS_PER_PAGE = 100