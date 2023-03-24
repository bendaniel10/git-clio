package com.bendaniel10.database.repo

import com.bendaniel10.DaysBetween
import com.bendaniel10.database.table.PullRequestEntity
import com.bendaniel10.database.table.ReportEntity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.math.ceil

interface ViewReportDetailsRepo {
    fun fetchPrOverviewById(reportId: Int): ViewPROverviewDetails
    fun fetchReportNameById(reportId: Int): String
    fun fetchPrsByMonthById(reportId: Int): ViewPRsByMonthDetails
    fun fetchPRStatusById(reportId: Int): ViewPRStatusDetails
    fun fetchTopPRCreatorsById(reportId: Int): ViewTopPRCreatorsDetails
    fun fetchMergedVsAutoMergedById(reportId: Int): ViewManualVsAutoMergedPRDetails
    fun fetchPrChangesOverviewById(reportId: Int): ViewPRChangesOverview
    fun fetchPrCCommitsOverviewById(reportId: Int): ViewPRCommitsOverview

    fun fetchViewPRsCommentsById(reportId: Int): ViewPRsCommentsDetails
    fun fetchPRsMergeDurationOverviewById(reportId: Int): ViewPRsMergeDurationDetails
}

internal class ViewReportDetailsRepoImpl : ViewReportDetailsRepo, KoinComponent {
    private val logger = LoggerFactory.getLogger(ViewReportDetailsRepoImpl::class.java)

    override fun fetchReportNameById(reportId: Int) = transaction {
        ReportEntity.findById(reportId)!!.name
    }

    override fun fetchPrsByMonthById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val prsPerMonth = report.pullRequests.mapLazy { it.createdAt.month }
            .groupBy { it }
            .toSortedMap { month1, month2 ->
                month1.ordinal.compareTo(month2.ordinal)
            }
            .map { it.key.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to it.value.count().toString() }
            .toMap()
            .let {
                SingleLineChartData(
                    "'PRs count'",
                    it.keys.joinToString { key -> "'$key'" },
                    it.values.joinToString()
                )
            }
        ViewPRsByMonthDetails(prsPerMonth)
    }

    override fun fetchPRStatusById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val merged = report.pullRequests.mapLazy { it.mergedAt != null }.count { it }
        val openPrs = report.pullRequests.mapLazy { it.closedAt == null }.count { it }
        val dismissedPrs = report.pullRequests.mapLazy { it.closedAt != null && it.mergedAt == null }.count { it }
        val openVsMergedVsDismissedPrs = PieChartData(
            "'Open vs Merged vs Dismissed'",
            "'Open', 'Merged', 'Dismissed'",
            "$openPrs, $merged, $dismissedPrs",
            "'rgb(255, 99, 132)', 'rgb(54, 162, 235)', 'rgb(255, 205, 86)'"
        )
        ViewPRStatusDetails(
            openPrs,
            merged,
            dismissedPrs,
            openVsMergedVsDismissedPrs
        )
    }

    override fun fetchTopPRCreatorsById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val topPRCreators = report.pullRequests.mapLazy { it.user.login }
            .groupBy { it }
            .map { it.key to it.value.count() }
            .toList()
            .sortedByDescending { (_, value) -> value }
            .mapIndexed { index, pair ->
                LeaderboardData(index + 1, pair.first, pair.second.toString())
            }
        ViewTopPRCreatorsDetails(
            topPRCreators
        )
    }

    override fun fetchMergedVsAutoMergedById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val merged = report.pullRequests.mapLazy { it.mergedAt != null }.count { it }
        val autoMerged = report.pullRequests.mapLazy { it.autoMerge }.count { it }
        val manualMerge = merged - autoMerged
        val manualVsAutoMergedPrs = PieChartData(
            "'Manual vs Auto-Merged PRs'",
            "'Manual Merge', 'Auto-merge'",
            "$manualMerge, $autoMerged",
            "'rgb(255, 99, 132)', 'rgb(54, 162, 235)'"
        )

        ViewManualVsAutoMergedPRDetails(
            merged,
            autoMerged,
            manualMerge,
            manualVsAutoMergedPrs
        )
    }

    override fun fetchPrChangesOverviewById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val additions = report.pullRequests.sumOf { it.additions }
        val deletions = report.pullRequests.sumOf { it.deletions }
        val changedFiles = report.pullRequests.sumOf { it.changedFiles }
        logger.debug("Add: $additions, Del: $deletions, CF: $changedFiles")
        data class MonthChanges(val month: Month, val additions: Int, val deletions: Int, val changedFiles: Int)

        val changesPerMonth = report.pullRequests.mapLazy {
            MonthChanges(
                it.createdAt.month,
                it.additions,
                it.deletions,
                it.changedFiles
            )
        }
            .groupBy { it.month }
            .toSortedMap { month1, month2 ->
                month1.ordinal.compareTo(month2.ordinal)
            }
            .map { monthToMonthChanges ->
                MonthChanges(
                    monthToMonthChanges.key,
                    monthToMonthChanges.value.sumOf { it.additions },
                    monthToMonthChanges.value.sumOf { it.deletions }.unaryMinus(),
                    monthToMonthChanges.value.sumOf { it.changedFiles }
                )
            }
            .let { monthChanges ->
                DoubleLineChartData(
                    "'PR Changes'",
                    monthChanges.map { it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                        .joinToString { key -> "'$key'" },
                    LabelToValues("'Additions'", monthChanges.map { it.additions }.joinToString()),
                    LabelToValues("'Deletions'", monthChanges.map { it.deletions }.joinToString()),
                )
            }
        ViewPRChangesOverview(
            additions,
            deletions,
            changedFiles,
            changesPerMonth
        )
    }

    override fun fetchPRsMergeDurationOverviewById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val averageDurationHours = report.pullRequests.mapLazy {
            it.createdAt to it.mergedAt
        }
            .filter { it.second != null }
            .map { Duration.between(it.first, it.second).toMinutes() }
            .let { minutesDuration ->
                ((minutesDuration.sum() / 60f) / minutesDuration.count()).let { "%.2f".format(it) }
            }

        val (shortestDurationHours, longestDurationHours) = report.pullRequests.mapLazy {
            it.createdAt to it.mergedAt
        }
            .filter { it.second != null }
            .map { createdToMerged ->
                Duration.between(createdToMerged.first, createdToMerged.second).toMinutes()
            }
            .let { minutesDuration ->
                (minutesDuration.minOf { it } / 60f).let { "%.2f".format(it) } to (minutesDuration.maxOf { it } / 60f).let {
                    "%.2f".format(
                        it
                    )
                }
            }

        val durationByCount = buildDistributionChartData(report, "'Merge duration distribution'") {
            if (it.mergedAt == null) {
                null
            } else {
                ceil((Duration.between(it.createdAt, it.mergedAt).toMinutes() / 60f)).toInt()
            }
        }
        ViewPRsMergeDurationDetails(
            averageDurationHours,
            shortestDurationHours,
            longestDurationHours,
            durationByCount
        )
    }

    override fun fetchPrCCommitsOverviewById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val commits = report.pullRequests.sumOf { it.commits }
        val prs = report.totalPullRequests
        val averageCommitPerPR = (commits.toFloat() / prs).let { "%.2f".format(it) }
        val commitByCountDist = buildDistributionChartData(report, "'Commits : PR'") { it.commits }

        ViewPRCommitsOverview(
            commits,
            averageCommitPerPR,
            commitByCountDist
        )
    }

    override fun fetchViewPRsCommentsById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val reviewComments = report.pullRequests.sumOf { it.reviewComments }
        val comments = report.pullRequests.sumOf { it.comments }
        val prs = report.totalPullRequests
        val averageCommentPerPR = (comments.toFloat() / prs).let { "%.2f".format(it) }
        val averageReviewCommentPerPR = (reviewComments.toFloat() / prs).let { "%.2f".format(it) }
        val commentsByCountDist = buildDistributionChartData(report, "'Comments : PR'") { it.comments }
        val reviewCommentsByCountDist =
            buildDistributionChartData(report, "'Review Comments : PR'") { it.reviewComments }

        ViewPRsCommentsDetails(
            comments,
            averageCommentPerPR,
            reviewComments,
            averageReviewCommentPerPR,
            commentsByCountDist,
            reviewCommentsByCountDist
        )
    }

    private fun buildDistributionChartData(
        report: ReportEntity,
        title: String,
        mapSelector: (PullRequestEntity) -> Int?
    ) =
        report.pullRequests.mapLazy { mapSelector.invoke(it) }
            .asSequence()
            .filterNotNull()
            .groupBy { it }
            .map { entry ->
                entry.value.count() to entry.key
            }
            .toList()
            .sortedBy { (_, comment) -> comment }
            .toList()
            .let { commentsToCountPair ->
                SingleLineChartData(
                    title,
                    commentsToCountPair.map { it.second }.joinToString { key -> "'$key'" },
                    commentsToCountPair.map { it.first }.joinToString(),
                )
            }


    override fun fetchPrOverviewById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val prs = report.totalPullRequests
        val averagePrPerDay = (prs.toFloat() / DaysBetween.twoInstants(
            report.analyticsStartDate.toKotlinLocalDate().atStartOfDayIn(TimeZone.UTC),
            report.analyticsEndDate.toKotlinLocalDate().atStartOfDayIn(TimeZone.UTC),
        )).let { "%.2f".format(it) }
        val openPrs = report.pullRequests.mapLazy { it.closedAt == null }.count { it }
        val openPrsPercentage = calculateTwoDecimalPlacesPercentage(openPrs, prs)
        val merged = report.pullRequests.mapLazy { it.mergedAt != null }.count { it }
        val mergedPrsPercentage = calculateTwoDecimalPlacesPercentage(merged, prs)
        val dismissedPrs = report.pullRequests.mapLazy { it.closedAt != null && it.mergedAt == null }.count { it }
        val dismissedPrsPercentage = calculateTwoDecimalPlacesPercentage(dismissedPrs, prs)
        val autoMerged = report.pullRequests.mapLazy { it.autoMerge }.count { it }
        val autoMergedPercentage = calculateTwoDecimalPlacesPercentage(autoMerged, merged)
        val comments = report.pullRequests.mapLazy { it.comments + it.reviewComments }.sum()
        val averageCommentPerPR = (comments.toFloat() / prs).let { "%.2f".format(it) }

        ViewPROverviewDetails(
            prs,
            averagePrPerDay,
            openPrs,
            openPrsPercentage,
            merged,
            mergedPrsPercentage,
            dismissedPrs,
            dismissedPrsPercentage,
            autoMerged,
            autoMergedPercentage,
            comments,
            averageCommentPerPR
        )
    }
}

data class ViewPROverviewDetails(
    val prs: Int,
    val averagePrPerDay: String,
    val openPrs: Int,
    val openPrsPercentage: String,
    val merged: Int,
    val mergedPrsPercentage: String,
    val dismissedPrs: Int,
    val dismissedPrsPercentage: String,
    val autoMerged: Int,
    val autoMergedPercentage: String,
    val comments: Int,
    val averageCommentPerPR: String,
)

data class ViewPRsCommentsDetails(
    val comments: Int,
    val averageCommentPerPr: String,
    val reviewComments: Int,
    val averageReviewCommentPerPr: String,
    val commentsByCountDist: SingleLineChartData,
    val reviewCommentsByCountDist: SingleLineChartData,
)

data class ViewPRsByMonthDetails(
    val prsPerMonth: SingleLineChartData,
)

data class ViewPRStatusDetails(
    val openPrs: Int,
    val merged: Int,
    val dismissedPrs: Int,
    val openVsMergedVsDismissedPrs: PieChartData,
)

data class ViewTopPRCreatorsDetails(
    val topPRCreators: List<LeaderboardData>,
)

data class ViewManualVsAutoMergedPRDetails(
    val merged: Int,
    val autoMerged: Int,
    val manualMerge: Int,
    val manualVsAutoMergedPrs: PieChartData,
)

data class SingleLineChartData(
    val title: String,
    val labels: String,
    val values: String,
)

data class DoubleLineChartData(
    val title: String,
    val labels: String,
    val first: LabelToValues,
    val second: LabelToValues,
)

data class LabelToValues(val label: String, val values: String)

data class PieChartData(
    // title: 'My First Dataset'
    val title: String,
    // labels: [
    //  'Red',
    //  'Blue',
    //  'Yellow'
    //]
    val labels: String,
    // values: [300, 50, 100],
    val values: String,
    // backgroundColor: [
    //  'rgb(255, 99, 132)',
    //  'rgb(54, 162, 235)',
    //  'rgb(255, 205, 86)'
    //]
    val backgroundColors: String
)

data class LeaderboardData(
    val position: Int,
    val name: String,
    val value: String
)

data class ViewPRChangesOverview(
    val additions: Int,
    val deletions: Int,
    val filesChanged: Int,
    val changesPerMonth: DoubleLineChartData,
)

data class ViewPRCommitsOverview(
    val commits: Int,
    val averageCommitPerPR: String,
    val commitByCountDist: SingleLineChartData
)

data class ViewPRsMergeDurationDetails(
    val averageDurationHours: String,
    val shortestDurationHours: String,
    val longestDurationHours: String,
    val durationByCount: SingleLineChartData
)

private fun calculateTwoDecimalPlacesPercentage(numerator: Int, denominator: Int) =
    ((numerator.toFloat() / denominator) * 100).let { "%.2f".format(it) }