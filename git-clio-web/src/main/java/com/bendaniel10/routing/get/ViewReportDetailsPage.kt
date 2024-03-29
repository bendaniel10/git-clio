package com.bendaniel10.routing.get

import com.bendaniel10.database.repo.ViewReportDetailsRepo
import com.bendaniel10.routing.RoutingHandler
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val REPORT_DETAILS_URL = "/reportdetails/view_report_details.ftl"

class ViewReportDetailsPage : RoutingHandler, KoinComponent {

    private val viewReportDetailsRepo: ViewReportDetailsRepo by inject()
    override fun path() = "/view_report_details"

    override suspend fun handle(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val reportId = pipelineContext.call.request.queryParameters["report_id"]!!.toInt()
        val content = when (val category = pipelineContext.call.request.queryParameters["pr_category"]) {
            null, "pr_overview" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPrOverviewById(reportId),
                        "reportId" to reportId,
                        "prCategory" to (category ?: "pr_overview")
                    )
                )
            }

            "prs_by_month" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPrsByMonthById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }

            "pr_status" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPRStatusById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }

            "pr_creators" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchTopPRCreatorsById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }

            "pr_auto_merge_status" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchMergedVsAutoMergedById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }
            "pr_changes_overview" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPrChangesOverviewById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }
            "pr_comments_overview" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchViewPRsCommentsById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }
            "pr_commits_overview" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPrCCommitsOverviewById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }
            "pr_merge_duration" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPRsMergeDurationOverviewById(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }
            "pr_created_by_time_of_day" -> {
                FreeMarkerContent(
                    REPORT_DETAILS_URL,
                    mapOf(
                        "reportName" to viewReportDetailsRepo.fetchReportNameById(reportId),
                        "details" to viewReportDetailsRepo.fetchPRCreatedByTimeOfDayByReportId(reportId),
                        "reportId" to reportId,
                        "prCategory" to category
                    )
                )
            }

            else -> {
                null
            }
        }

        if (content != null) {
            pipelineContext.call.respond(content)
        } else {
            pipelineContext.call.respondRedirect("/")
        }
    }
}