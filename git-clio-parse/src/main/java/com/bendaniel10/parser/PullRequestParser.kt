package com.bendaniel10.parser

import com.bendaniel10.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val DELETED_USER = "deleted.user"

interface PullRequestParser {
    fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag)
}

object CompositePullRequestParser : PullRequestParser {
    private val processed = mutableSetOf<String>()

    private val composite = listOf(
        PullRequestCountParser,
        MostActivePullRequestParser,
        LongestLivedPullRequestParser,
        NightOwlPullRequestParser,
        EarlyWormPullRequestParser,
        WeekendPullRequestParser,
        CleanUpMasterParser,
        AdditionMasterParser,
        ReviewerApproverParser,
        ReviewerCommentsParser,
        ReviewPerHourInDayParser,
        BiggestChangeParser,
        SmallestChangeParser,
    )

    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        if (processed.contains(pullRequest.fetchPullRequestItem.number.toString())) {
            println("Already processed ${pullRequest.fetchPullRequestItem.number}")
            return
        }
        composite.forEach {
            it.parse(pullRequest, infoBag)
        }
        processed.add(pullRequest.fetchPullRequestItem.number.toString())
    }
}

private object PullRequestCountParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val pullRequestCount = infoBag.pullRequestCount ?: PullRequestCount()
        val newTotal = pullRequestCount.total.plus(1)
        val newMerged = if (fetchPullRequestByIdResponse.mergedAt != null) {
            pullRequestCount.merged.plus(1)
        } else {
            pullRequestCount.merged
        }
        val newClosed =
            if (fetchPullRequestByIdResponse.mergedAt == null && fetchPullRequestByIdResponse.closedAt != null) {
                pullRequestCount.closed.plus(1)
            } else {
                pullRequestCount.closed
            }
        val newAutoMerged = if (fetchPullRequestByIdResponse.autoMerge != null) {
            pullRequestCount.autoMerged.plus(1)
        } else {
            pullRequestCount.autoMerged
        }
        val createdMonth = fetchPullRequestByIdResponse.createdAt.toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("MMMM"))
        val updatedTotalPerMonth = pullRequestCount.totalPerMonth.apply {
            put(createdMonth, getOrDefault(createdMonth, 0).plus(1))
        }

        infoBag.pullRequestCount = pullRequestCount.copy(
            total = newTotal,
            merged = newMerged,
            closed = newClosed,
            autoMerged = newAutoMerged,
            totalPerMonth = updatedTotalPerMonth
        )
    }
}

private object MostActivePullRequestParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val totalExistingComments =
            (infoBag.mostActivePullRequest?.reviewComments ?: 0).plus(infoBag.mostActivePullRequest?.comments ?: 0)
        val toCompareTotalComments = fetchPullRequestByIdResponse.comments + fetchPullRequestByIdResponse.reviewComments

        if (toCompareTotalComments > totalExistingComments) {
            infoBag.mostActivePullRequest = MostActivePullRequest(
                reviewComments = fetchPullRequestByIdResponse.reviewComments,
                comments = fetchPullRequestByIdResponse.comments,
                pullRequest = PullRequest(
                    pullRequest.fetchPullRequestItem.user.login,
                    pullRequest.fetchPullRequestByIdResponse.htmlUrl,
                    pullRequest.fetchPullRequestItem.title,
                    fetchPullRequestByIdResponse.createdAt,
                    fetchPullRequestByIdResponse.closedAt,
                    Duration.between(
                        fetchPullRequestByIdResponse.createdAt.toLocalDateTime(),
                        (fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()) ?: LocalDateTime.MAX
                    ).toDays()
                )
            )
        }
    }
}

private object LongestLivedPullRequestParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val toCompareCreatedDate = fetchPullRequestByIdResponse.createdAt.toLocalDateTime()
        val toCompareClosedDate =
            if (fetchPullRequestByIdResponse.closedAt == null) LocalDateTime.MAX else fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()
        val toCompareHoursBetween = Duration.between(toCompareCreatedDate, toCompareClosedDate).toHours()
        val existingHoursBetween = if (infoBag.longestLivedPullRequest == null) 0 else Duration.between(
            infoBag.longestLivedPullRequest!!.createdDate.toLocalDateTime(),
            if (infoBag.longestLivedPullRequest!!.closedDate == null) LocalDateTime.MAX else infoBag.longestLivedPullRequest!!.closedDate?.toLocalDateTime()
        ).toHours()

        if (toCompareHoursBetween > existingHoursBetween) {
            infoBag.longestLivedPullRequest = PullRequest(
                pullRequest.fetchPullRequestItem.user.login,
                pullRequest.fetchPullRequestByIdResponse.htmlUrl,
                pullRequest.fetchPullRequestItem.title,
                fetchPullRequestByIdResponse.createdAt,
                fetchPullRequestByIdResponse.closedAt,
                daysAlive = Duration.between(
                    fetchPullRequestByIdResponse.createdAt.toLocalDateTime(),
                    (fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()) ?: LocalDateTime.MAX
                ).toDays()
            )
        }
    }
}

private object NightOwlPullRequestParser : PullRequestParser {
    private const val NIGHT_OWL_STARTS_AT = 20

    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val toCompareCreatedHour = fetchPullRequestByIdResponse.createdAt.toLocalDateTime().hour
        val existingLatestHour = infoBag.nightOwlPullRequest?.latestPullRequest?.createdDate?.toLocalDateTime()?.hour
        val existingTotal = infoBag.nightOwlPullRequest?.total ?: 0
        if (toCompareCreatedHour >= NIGHT_OWL_STARTS_AT) {
            val newLatestPullRequest = if (existingLatestHour == null || existingLatestHour < toCompareCreatedHour) {
                PullRequest(
                    pullRequest.fetchPullRequestItem.user.login,
                    pullRequest.fetchPullRequestByIdResponse.htmlUrl,
                    pullRequest.fetchPullRequestItem.title,
                    fetchPullRequestByIdResponse.createdAt,
                    fetchPullRequestByIdResponse.closedAt,
                    Duration.between(
                        fetchPullRequestByIdResponse.createdAt.toLocalDateTime(),
                        (fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()) ?: LocalDateTime.MAX
                    ).toDays()
                )
            } else {
                infoBag.nightOwlPullRequest!!.latestPullRequest
            }
            infoBag.nightOwlPullRequest = NightOwlPullRequest(
                total = existingTotal.plus(1),
                latestPullRequest = newLatestPullRequest
            )
        }
    }
}

private object EarlyWormPullRequestParser : PullRequestParser {
    private const val EARLY_WORM_STARTS_AT = 6

    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val toCompareCreatedHour = fetchPullRequestByIdResponse.createdAt.toLocalDateTime().hour
        val existingEarliestHour = infoBag.earlyWormPullRequest?.earliest?.createdDate?.toLocalDateTime()?.hour
        val existingTotal = infoBag.earlyWormPullRequest?.total ?: 0
        if (toCompareCreatedHour <= EARLY_WORM_STARTS_AT) {
            val newEarliest = if (existingEarliestHour == null || toCompareCreatedHour < existingEarliestHour) {
                PullRequest(
                    pullRequest.fetchPullRequestItem.user.login,
                    pullRequest.fetchPullRequestByIdResponse.htmlUrl,
                    pullRequest.fetchPullRequestItem.title,
                    fetchPullRequestByIdResponse.createdAt,
                    fetchPullRequestByIdResponse.closedAt,
                    Duration.between(
                        fetchPullRequestByIdResponse.createdAt.toLocalDateTime(),
                        (fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()) ?: LocalDateTime.MAX
                    ).toDays()
                )
            } else {
                infoBag.earlyWormPullRequest!!.earliest
            }
            infoBag.earlyWormPullRequest = EarlyWormPullRequest(
                total = existingTotal.plus(1),
                earliest = newEarliest
            )
        }
    }
}

private object WeekendPullRequestParser : PullRequestParser {
    private const val CLOSE_OF_BUSINESS_HOUR = 18

    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        if (isWeekend(fetchPullRequestByIdResponse.createdAt.toLocalDateTime())) {
            infoBag.weekendPullRequest = WeekendPullRequest(
                total = (infoBag.weekendPullRequest?.total ?: 0).plus(1),
                creators = infoBag.weekendPullRequest?.creators?.apply {
                    put(
                        pullRequest.fetchPullRequestItem.user.login,
                        getOrDefault(pullRequest.fetchPullRequestItem.user.login, 0).plus(1)
                    )
                } ?: mutableMapOf()
            )
        }

    }

    private fun isWeekend(date: LocalDateTime) = when (date.dayOfWeek) {
        DayOfWeek.FRIDAY -> date.hour >= CLOSE_OF_BUSINESS_HOUR
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> true
        else -> false
    }
}

private object CleanUpMasterParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val cleanUpMaster = infoBag.cleanUpMaster ?: CleanUpMaster()

        infoBag.cleanUpMaster = cleanUpMaster.copy(
            totalDeletedLines = cleanUpMaster.totalDeletedLines.plus(fetchPullRequestByIdResponse.deletions),
            deleters = cleanUpMaster.deleters.apply {
                put(
                    pullRequest.fetchPullRequestItem.user.login,
                    getOrDefault(
                        pullRequest.fetchPullRequestItem.user.login,
                        0
                    ).plus(fetchPullRequestByIdResponse.deletions)
                )
            }
        )
    }
}

private object AdditionMasterParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val additionMaster = infoBag.additionMaster ?: AdditionMaster()

        infoBag.additionMaster = additionMaster.copy(
            totalAddedLines = additionMaster.totalAddedLines.plus(fetchPullRequestByIdResponse.additions),
            adders = additionMaster.adders.apply {
                put(
                    pullRequest.fetchPullRequestItem.user.login,
                    getOrDefault(
                        pullRequest.fetchPullRequestItem.user.login,
                        0
                    ).plus(fetchPullRequestByIdResponse.additions)
                )
            }
        )
    }
}

private object ReviewerApproverParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestReviewResponse = pullRequest.fetchPullRequestReviewResponse
        val reviewerApprover = infoBag.reviewerApprover ?: ReviewerApprover()

        infoBag.reviewerApprover = reviewerApprover.copy(
            reviewers = reviewerApprover.reviewers.apply {
                fetchPullRequestReviewResponse.forEach {
                    if (it.state.equals("APPROVED", true)) {
                        put(
                            it.user?.login ?: DELETED_USER,
                            getOrDefault(it.user?.login ?: DELETED_USER, 0).plus(1)
                        )
                    }
                }
            }
        )
    }
}

private object ReviewerCommentsParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestReviewResponse = pullRequest.fetchPullRequestReviewResponse
        val reviewerCommenter = infoBag.reviewerCommenter ?: ReviewerCommenter()

        infoBag.reviewerCommenter = reviewerCommenter.copy(
            commenter = reviewerCommenter.commenter.apply {
                fetchPullRequestReviewResponse.forEach {
                    if (it.state.equals("COMMENTED", true)) {
                        put(
                            it.user?.login ?: DELETED_USER,
                            getOrDefault(it.user?.login ?: DELETED_USER, 0).plus(1)
                        )
                    }
                }
            }
        )
    }
}

private object ReviewPerHourInDayParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val reviewPerHourInDay = infoBag.reviewPerHourInDay ?: ReviewPerHourInDay()
        val fetchPullRequestReviewResponse = pullRequest.fetchPullRequestReviewResponse

        infoBag.reviewPerHourInDay = reviewPerHourInDay.copy(
            reviewers = reviewPerHourInDay.reviewers.apply {
                fetchPullRequestReviewResponse.forEach {
                    if (it.state.equals("APPROVED", true)) {
                        put(
                            it.submittedAt.toLocalDateTime().hour,
                            getOrDefault(it.submittedAt.toLocalDateTime().hour, 0).plus(1)
                        )
                    }
                }
            }
        )
    }
}

private object BiggestChangeParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val existingChangeSum = (infoBag.biggestChange?.changedFiles ?: 0) + (infoBag.biggestChange?.added ?: 0) +
                (infoBag.biggestChange?.removed ?: 0)
        val toCompareChangeSum = fetchPullRequestByIdResponse.changedFiles + fetchPullRequestByIdResponse.additions +
                fetchPullRequestByIdResponse.deletions

        if (toCompareChangeSum > existingChangeSum) {
            infoBag.biggestChange = PullRequestChange(
                pullRequest = PullRequest(
                    pullRequest.fetchPullRequestItem.user.login,
                    pullRequest.fetchPullRequestByIdResponse.htmlUrl,
                    pullRequest.fetchPullRequestItem.title,
                    fetchPullRequestByIdResponse.createdAt,
                    fetchPullRequestByIdResponse.closedAt,
                    Duration.between(
                        fetchPullRequestByIdResponse.createdAt.toLocalDateTime(),
                        (fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()) ?: LocalDateTime.MAX
                    ).toDays()
                ),
                added = fetchPullRequestByIdResponse.additions,
                removed = fetchPullRequestByIdResponse.deletions,
                changedFiles = fetchPullRequestByIdResponse.changedFiles
            )
        }
    }
}

private object SmallestChangeParser : PullRequestParser {
    override fun parse(pullRequest: FetchSdkResponse.PullRequest, infoBag: InfoBag) {
        val fetchPullRequestByIdResponse = pullRequest.fetchPullRequestByIdResponse
        val existingChangeSum = (infoBag.smallestChange?.changedFiles ?: 0) + (infoBag.smallestChange?.added ?: 0) +
                (infoBag.smallestChange?.removed ?: 0)
        val toCompareChangeSum = fetchPullRequestByIdResponse.changedFiles + fetchPullRequestByIdResponse.additions +
                fetchPullRequestByIdResponse.deletions

        if (infoBag.smallestChange == null || toCompareChangeSum < existingChangeSum) {
            infoBag.smallestChange = PullRequestChange(
                pullRequest = PullRequest(
                    pullRequest.fetchPullRequestItem.user.login,
                    pullRequest.fetchPullRequestByIdResponse.htmlUrl,
                    pullRequest.fetchPullRequestItem.title,
                    fetchPullRequestByIdResponse.createdAt,
                    fetchPullRequestByIdResponse.closedAt,
                    Duration.between(
                        fetchPullRequestByIdResponse.createdAt.toLocalDateTime(),
                        (fetchPullRequestByIdResponse.closedAt?.toLocalDateTime()) ?: LocalDateTime.MAX
                    ).toDays()
                ),
                added = fetchPullRequestByIdResponse.additions,
                removed = fetchPullRequestByIdResponse.deletions,
                changedFiles = fetchPullRequestByIdResponse.changedFiles
            )
        }
    }
}
