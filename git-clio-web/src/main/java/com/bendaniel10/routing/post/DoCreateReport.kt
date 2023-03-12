package com.bendaniel10.routing.post

import com.bendaniel10.ParseSdk
import com.bendaniel10.ReportProperties
import com.bendaniel10.routing.RoutingHandler
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.toKotlinLocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DoCreateReport : RoutingHandler, KoinComponent {
    private val parseSdk: ParseSdk by inject()
    override fun path() = "/create_report"

    override suspend fun handle(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val params = pipelineContext.call.receiveParameters()

        parseSdk.newParseReport(
            ReportProperties(
                params["report_name"]!!,
                params["github_username"]!!,
                params["github_pat"]!!,
                params["github_org"]!!,
                params["github_repo"]!!,
                LocalDate.parse(params["analytics_start"]!!, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .toKotlinLocalDate(),
                LocalDate.parse(params["analytics_end"]!!, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .toKotlinLocalDate(),
            )
        )

        pipelineContext.call.respondRedirect("/")
    }
}