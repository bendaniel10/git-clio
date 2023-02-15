package com.bendaniel10

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class InfoBag(
    var pullRequestCount: PullRequestCount? = null,
    var mostActivePullRequest: MostActivePullRequest? = null,
    var longestLivedPullRequest: PullRequest? = null,
    var nightOwlPullRequest: NightOwlPullRequest? = null,
    var earlyBirdPullRequest: EarlyBirdPullRequest? = null,
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
    val averageHoursToMerge: Double = 0.0,
    val totalPerMonth: MutableMap<String, Int> = mutableMapOf(), // month, totalNumber
    val totalPerUser: MutableMap<String, Int> = mutableMapOf(), // user, totalNumber
)

@Serializable
data class MostActivePullRequest(
    val reviewComments: Int = 0,
    val comments: Int = 0,
    val averageCommentsPerPr: Double = 0.0, // review comments + comments bundled together
    val pullRequest: PullRequest,
)

@Serializable
data class PullRequest(
    val author: String,
    val url: String,
    val title: String,
    val createdDate: LocalDateTime, // "2023-01-26T13:38:00Z"
    val closedDate: LocalDateTime?, // "2023-01-26T13:38:00Z"
    val daysAlive: Int
)

@Serializable
data class NightOwlPullRequest(
    val latestPullRequest: PullRequest,
    val total: Int = 0,
)

@Serializable
data class EarlyBirdPullRequest(
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
    val total: Int = 0,
    val reviewers: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class ReviewerCommenter(
    val total: Int = 0,
    val commenter: MutableMap<String, Int> = mutableMapOf() // author, totalNumber
)

@Serializable
data class ReviewPerHourInDay(
    val reviewers: MutableMap<Int, Int> = defaultHourTotalNumberMap() // hour, number
)

private fun defaultHourTotalNumberMap() = mutableMapOf<Int, Int>().apply {
    for (i in 0..23) {
        put(i, 0)
    }
}

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

