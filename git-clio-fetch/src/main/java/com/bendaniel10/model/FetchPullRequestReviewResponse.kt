package com.bendaniel10.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias FetchPullRequestReviewResponse = List<FetchPullRequestReviewResponseItem>

@Serializable
data class FetchPullRequestReviewResponseItem(
    val state: String, // COMMENTED, APPROVED
    @SerialName("submitted_at")
    val submittedAt: Instant,
    val user: FetchPullRequestReviewResponseItemUser?
)

@Serializable
data class FetchPullRequestReviewResponseItemUser(
    val login: String
)