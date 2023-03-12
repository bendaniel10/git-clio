package com.bendaniel10.routing.get

import com.bendaniel10.ParseSdk
import com.bendaniel10.routing.RoutingHandler
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ViewIndexPage : RoutingHandler, KoinComponent {
    private val parseSdk: ParseSdk by inject()
    override fun path() = "/"

    override suspend fun handle(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val reports = parseSdk.fetchAllReports().map {
            ViewReport(it.id.value, it.name, it.reportStatus.name, it.totalIssues, it.totalPullRequests)
        }
        pipelineContext.call.respond(FreeMarkerContent("index.ftl", mapOf("reports" to reports)))
    }
}

data class ViewReport(
    val id: Int,
    val name: String,
    val status: String,
    val issues: Int,
    val pullRequests: Int
)