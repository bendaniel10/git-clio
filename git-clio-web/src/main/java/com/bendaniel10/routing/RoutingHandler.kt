package com.bendaniel10.routing

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

interface RoutingHandler {
    fun path(): String
    suspend fun handle(pipelineContext: PipelineContext<Unit, ApplicationCall>)
}