package com.bendaniel10.database.repo

import com.bendaniel10.DaysBetween
import com.bendaniel10.database.table.ReportEntity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.time.format.TextStyle
import java.util.*

interface ViewReportDetailsRepo {
    fun fetchPrOverviewById(reportId: Int): ViewPROverviewDetails
    fun fetchReportNameById(reportId: Int): String
    fun fetchPrsByMonthById(reportId: Int): ViewPRsByMonthDetails
    fun fetchPRStatusById(reportId: Int): ViewPRStatusDetails
    fun fetchTopPRCreatorsById(reportId: Int): ViewTopPRCreatorsDetails
    fun fetchMergedVsAutoMergedById(reportId: Int): ViewManualVsAutoMergedPRDetails
}

internal class ViewReportDetailsRepoImpl : ViewReportDetailsRepo, KoinComponent {
    private val logger = LoggerFactory.getLogger(ViewReportDetailsRepoImpl::class.java)

    override fun fetchReportNameById(reportId: Int) = transaction {
        ReportEntity.findById(reportId)!!.name
    }

    override fun fetchPrsByMonthById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        val monthToPrsPair = report.pullRequests.mapLazy { it.createdAt.month }
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
        ViewPRsByMonthDetails(monthToPrsPair)
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

    override fun fetchPrOverviewById(reportId: Int) = transaction {
        val report = ReportEntity.findById(reportId)!!
        logger.debug("Getting PR overview for ${report.name}")
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

data class ViewPRsByMonthDetails(
    val monthToPrsPair: SingleLineChartData,
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

private fun calculateTwoDecimalPlacesPercentage(numerator: Int, denominator: Int) =
    ((numerator.toFloat() / denominator) * 100).let { "%.2f".format(it) }