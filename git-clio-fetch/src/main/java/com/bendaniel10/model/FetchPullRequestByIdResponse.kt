package com.bendaniel10.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchPullRequestByIdResponse(
    val number: Int,
    val comments: Int,
    @SerialName("review_comments")
    val reviewComments: Int,
    val commits: Int,
    val additions: Int,
    val deletions: Int,
    @SerialName("changed_files")
    val changedFiles: Int,
    val title: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("closed_at")
    val closedAt: String?,
    @SerialName("merged_at")
    val mergedAt: String?,
    val state: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("auto_merge")
    val autoMerge: AutoMerge?,
)

@Serializable
data class AutoMerge(@SerialName("merge_method") val mergeMethod: String)