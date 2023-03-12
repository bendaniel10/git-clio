package com.bendaniel10.serverfeature

import com.bendaniel10.routing.RoutingHandler
import com.bendaniel10.routing.get.ViewCreateReportPage
import com.bendaniel10.routing.get.ViewIndexPage
import com.bendaniel10.routing.get.ViewReportDetailsPage
import com.bendaniel10.routing.post.DoCreateReport
import io.ktor.server.application.*
import io.ktor.server.routing.*

class RoutingWebServerFeature(
    private val viewIndexPage: ViewIndexPage,
    private val viewCreateReportPage: ViewCreateReportPage,
    private val viewReportDetailsPage: ViewReportDetailsPage,
    private val doCreateReport: DoCreateReport
) : WebServerFeature {
    private fun httpGetRoutes(): List<RoutingHandler> = listOf(
        viewIndexPage,
        viewCreateReportPage,
        viewReportDetailsPage,
    )

    private fun httpPostRoutes(): List<RoutingHandler> = listOf(
        doCreateReport,
    )
    override fun configure(application: Application) {
        application.install(Routing) {
            httpGetRoutes().forEach { getHandler ->
                get(getHandler.path()) {
                    getHandler.handle(this)
                }
            }

            httpPostRoutes().forEach { postHandler ->
                post(postHandler.path()) {
                    postHandler.handle(this)
                }
            }
        }
    }
}


