package com.bendaniel10.routing.get

import com.bendaniel10.database.repo.ViewReportDetailsRepo
import com.bendaniel10.routing.RoutingHandler
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ViewReportDetailsPage : RoutingHandler, KoinComponent {

    private val viewReportDetailsRepo: ViewReportDetailsRepo by inject()
    override fun path() = "/view_report_details"

    override suspend fun handle(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        pipelineContext.call.respond(
            FreeMarkerContent(
                "view_report_details.ftl",
                mapOf(
                    "details" to viewReportDetailsRepo.fetchViewReportDetailsById(
                        pipelineContext.call.request.queryParameters["report_id"]!!.toInt()
                    )
                )
            )
        )
    }
}