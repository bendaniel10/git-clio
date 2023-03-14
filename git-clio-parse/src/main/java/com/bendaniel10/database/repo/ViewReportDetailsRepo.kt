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
    fun fetchViewReportDetailsById(reportId: Int): ViewReportDetails
}

internal class ViewReportDetailsRepoImpl : ViewReportDetailsRepo, KoinComponent {
    private val logger = LoggerFactory.getLogger(ViewReportDetailsRepoImpl::class.java)

    override fun fetchViewReportDetailsById(reportId: Int): ViewReportDetails = transaction {
        val report = ReportEntity.findById(reportId)!!
        logger.debug("Getting report details for ${report.name}")
        val reportName = report.name
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
        val manualVsAutoMergedPrs = PieChartData(
            "'Manual vs Auto-Merged PRs'",
            "'Manual Merge', 'Auto-merge'",
            "${merged - autoMerged}, $autoMerged",
            "'rgb(255, 99, 132)', 'rgb(54, 162, 235)'"
        )
        val openVsMergedVsDismissedPrs = PieChartData(
            "'Open vs Merged vs Dismissed'",
            "'Open', 'Merged', 'Dismissed'",
            "$openPrs, $merged, $dismissedPrs",
            "'rgb(255, 99, 132)', 'rgb(54, 162, 235)', 'rgb(255, 205, 86)'"
        )
        val issues = report.totalIssues
        val locAdditions = report.pullRequests.mapLazy { it.additions }.sum()
        val locDeletions = report.pullRequests.mapLazy { it.deletions }.sum()
        val filesChanged = report.pullRequests.mapLazy { it.changedFiles }.sum()
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
        val topPRCreators = report.pullRequests.mapLazy { it.user.login }
            .groupBy { it }
            .map { it.key to it.value.count() }
            .toList()
            .sortedByDescending { (_, value) -> value }
            .mapIndexed { index, pair ->
                LeaderboardData(index + 1, pair.first, pair.second.toString())
            }.take(8)

        ViewReportDetails(
            reportName,
            prs,
            averagePrPerDay,
            openPrs,
            openPrsPercentage,
            merged,
            mergedPrsPercentage,
            dismissedPrs,
            dismissedPrsPercentage,
            autoMerged,
            issues,
            locAdditions,
            locDeletions,
            filesChanged,
            monthToPrsPair,
            manualVsAutoMergedPrs,
            topPRCreators,
            openVsMergedVsDismissedPrs
        )
    }
}

data class ViewReportDetails(
    val reportName: String,
    val prs: Int,
    val averagePrPerDay: String,
    val openPrs: Int,
    val openPrsPercentage: String,
    val merged: Int,
    val mergedPrsPercentage: String,
    val dismissedPrs: Int,
    val dismissedPrsPercentage: String,
    val autoMerged: Int,
    val issues: Int,
    val locAdditions: Int,
    val locDeletions: Int,
    val filesChanged: Int,
    val monthToPrsPair: SingleLineChartData,
    val manualVsAutoMergedPrs: PieChartData,
    val topPRCreators: List<LeaderboardData>,
    val openVsMergedVsDismissedPrs: PieChartData
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