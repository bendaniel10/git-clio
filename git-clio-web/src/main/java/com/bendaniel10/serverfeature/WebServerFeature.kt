package com.bendaniel10.serverfeature

import io.ktor.server.application.*

interface WebServerFeature {
    fun configure(application: Application)
}

class CompositeWebServerFeature(
    freemarkerWebServerFeature: FreemarkerWebServerFeature,
    routingWebServerFeature: RoutingWebServerFeature
) : WebServerFeature {
    private val features = listOf(freemarkerWebServerFeature, routingWebServerFeature)
    override fun configure(application: Application) {
        features.forEach {
            it.configure(application)
        }
    }
}