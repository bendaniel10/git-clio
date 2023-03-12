package com.bendaniel10.database.repo

import com.bendaniel10.database.table.ReportEntity
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

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
        val openPrs = report.pullRequests.mapLazy { it.closedAt == null }.count { it }
        val openPrsPercentage = calculateTwoDecimalPlacesPercentage(openPrs, prs)
        val merged = report.pullRequests.mapLazy { it.mergedAt != null }.count { it }
        val mergedPrsPercentage = calculateTwoDecimalPlacesPercentage(merged, prs)
        val autoMerged = report.pullRequests.mapLazy { it.autoMerge }.count { it }
        val autoMergedPrsPercentage = calculateTwoDecimalPlacesPercentage(autoMerged, prs)
        val issues = report.totalIssues
        val locAdditions = report.pullRequests.mapLazy { it.additions }.sum()
        val locDeletions = report.pullRequests.mapLazy { it.deletions }.sum()
        val filesChanged = report.pullRequests.mapLazy { it.changedFiles }.sum()
        val monthToPrsPair = report.pullRequests.mapLazy { it.createdAt.month.name }
            .groupBy { it }
            .map { it.key to it.value.count().toString() }
            .toMap()
            .let {
                ChartData(
                    "'PRs per Month'",
                    it.keys.joinToString { key -> "'$key'" },
                    it.values.joinToString()
                )
            }

        ViewReportDetails(
            reportName,
            prs,
            openPrs,
            openPrsPercentage,
            merged,
            mergedPrsPercentage,
            autoMerged,
            autoMergedPrsPercentage,
            issues,
            locAdditions,
            locDeletions,
            filesChanged,
            monthToPrsPair
        )
    }
}

data class ViewReportDetails(
    val reportName: String,
    val prs: Int,
    val openPrs: Int,
    val openPrsPercentage: Double,
    val merged: Int,
    val mergedPrsPercentage: Double,
    val autoMerged: Int,
    val autoMergedPrsPercentage: Double,
    val issues: Int,
    val locAdditions: Int,
    val locDeletions: Int,
    val filesChanged: Int,
    val monthToPrsPair: ChartData
)

data class ChartData(
    val title: String,
    val labels: String,
    val values: String,
)

private fun calculateTwoDecimalPlacesPercentage(numerator: Int, denominator: Int) =
    (((numerator.toFloat() / denominator) * 100).roundToInt() * 100.0) / 100.0