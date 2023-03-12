package com.bendaniel10.routing.get

import com.bendaniel10.routing.RoutingHandler
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.koin.core.component.KoinComponent

class ViewCreateReportPage : RoutingHandler, KoinComponent {
    override fun path() = "/create_report"

    override suspend fun handle(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        pipelineContext.call.respond(FreeMarkerContent("create_report.ftl", emptyMap<String, String>()))
    }
}