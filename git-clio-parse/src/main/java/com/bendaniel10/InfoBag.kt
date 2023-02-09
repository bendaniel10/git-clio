package com.bendaniel10

import kotlinx.serialization.Serializable

@Serializable
data class InfoBag(
    var pullRequestCount: PullRequestCount? = null,
    var mostActivePullRequest: MostActivePullRequest? = null,
    var longestLivedPullRequest: PullRequest? = null,
    var nightOwlPullRequest: NightOwlPullRequest? = null,
    var earlyWormPullRequest: EarlyWormPullRequest? = null,
    var weekendPullRequest: WeekendPullRequest? = null,
    var cleanUpMaster: CleanUpMaster? = null,
    var additionMaster: AdditionMaster? = null,
    var reviewerApprover: ReviewerApprover? = null,
    var reviewerCommenter: ReviewerCommenter? = null,
    var reviewPerHourInDay: ReviewPerHourInDay? = null,
    var biggestChange: PullRequestChange? = null,
    var smallestChange: PullRequestChange? = null,
    var issueStat: IssueStat? = null
)

@Serializable
data class PullRequestCount(
    val total: Int = 0,
    val merged: Int = 0,
    val closed: Int = 0,
    val autoMerged: Int = 0,
    val totalPerMonth: MutableMap<String, Int> = mutableMapOf() // month, totalNumber
)

@Serializable
data class MostActivePullRequest(
    val reviewComments: Int = 0,
    val comments: Int = 0,
    val pullRequest: PullRequest,
)

@Serializable
data class PullRequest(
    val author: String,
    val url: String,
    val title: String,
    val createdDate: String, // "2023-01-26T13:38:00Z"
    val closedDate: String?, // "2023-01-26T13:38:00Z"
    val daysAlive: Long
)

@Serializable
data class NightOwlPullRequest(
    val latestPullRequest: PullRequest,
    val total: Int = 0,
)

@Serializable
data class EarlyWormPullRequest(
    val earliest: PullRequest,
    val total: Int = 0
)

@Serializable
data class WeekendPullRequest(
    val total: Int = 0,
    val creators: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class CleanUpMaster(
    val totalDeletedLines: Int = 0,
    val deleters: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class AdditionMaster(
    val totalAddedLines: Int = 0,
    val adders: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class ReviewerApprover(
    val reviewers: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class ReviewerCommenter(
    val commenter: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class ReviewPerHourInDay(
    val reviewers: MutableMap<Int, Int> = mutableMapOf() // hour, number
)

@Serializable
data class PullRequestChange(
    val pullRequest: PullRequest,
    val added: Int = 0,
    val removed: Int = 0,
    val changedFiles: Int = 0
)

@Serializable
data class IssueStat(
    val total: Int = 0,
    val resolved: Int = 0,
    val openers: MutableMap<String, Int> = mutableMapOf(), // author, totalNumber
    val resolvers: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

