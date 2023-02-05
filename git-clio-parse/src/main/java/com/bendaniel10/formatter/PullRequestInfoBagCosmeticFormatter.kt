package com.bendaniel10.formatter

import com.bendaniel10.InfoBag
import java.text.SimpleDateFormat

internal object PullRequestInfoBagCosmeticFormatter : InfoBagCosmeticFormatter {
    override fun format(infoBag: InfoBag) {
        with(infoBag) {
            pullRequestCount = pullRequestCount?.copy(
                totalPerMonth = pullRequestCount?.totalPerMonth?.toSortedMap { key1, key2 ->
                    SimpleDateFormat("MMMM").parse(key1).compareTo(SimpleDateFormat("MMMM").parse(key2))
                } ?: mutableMapOf()
            )

            weekendPullRequest = weekendPullRequest?.copy(
                creators = weekendPullRequest?.creators?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )

            cleanUpMaster = cleanUpMaster?.copy(
                deleters = cleanUpMaster?.deleters?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )

            additionMaster = additionMaster?.copy(
                adders = additionMaster?.adders?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )

            reviewerApprover = reviewerApprover?.copy(
                reviewers = reviewerApprover?.reviewers?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )

            reviewerCommenter = reviewerCommenter?.copy(
                commenter = reviewerCommenter?.commenter?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )

            reviewPerHourInDay = reviewPerHourInDay?.copy(
                reviewers = reviewPerHourInDay?.reviewers?.toList()?.sortedByDescending { (key, _) -> key }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )
        }
    }
}