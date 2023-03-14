package com.bendaniel10.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchIssuesResponse(
    @SerialName("total_count")
    val totalCount: Int,
    val items: List<FetchIssuesItem>
)

@Serializable
data class FetchIssuesItem(
    val url: String,
    val user: FetchIssuesItemUser,
    val title: String,
    val number: Int,
    val state: String, // open, closed
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("closed_at")
    val closedAt: Instant?
)

@Serializable
data class FetchIssuesItemUser(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String
)
