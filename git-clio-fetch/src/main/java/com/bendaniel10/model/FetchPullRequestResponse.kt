package com.bendaniel10.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchPullRequestResponse(
    @SerialName("total_count")
    val totalCount: Int,
    val items: List<FetchPullRequestItem>
)

@Serializable
data class FetchPullRequestItem(
    val url: String,
    val user: FetchPullRequestItemUser,
    val title: String,
    val number: Int,
    val state: String,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("closed_at")
    val closedAt: Instant?
)

@Serializable
data class FetchPullRequestItemUser(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String
)
