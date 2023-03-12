package com.bendaniel10

import com.bendaniel10.serverfeature.WebServerFeature
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

interface WebSdk {
    fun startWebServer(port: Int = 2546)
}

internal class WebSdkImpl : WebSdk, KoinComponent {

    private val logger = LoggerFactory.getLogger(WebSdkImpl::class.java)

    private val feature by inject<WebServerFeature>()
    override fun startWebServer(port: Int) {
        embeddedServer(Netty, port = port) {
            feature.configure(this)
        }.start(wait = true)
        logger.info("Starting web UI on http://gitclio:$port")
    }
}