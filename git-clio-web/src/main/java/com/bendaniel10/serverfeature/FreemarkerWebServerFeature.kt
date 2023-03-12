package com.bendaniel10.serverfeature

import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.freemarker.*

class FreemarkerWebServerFeature : WebServerFeature {
    override fun configure(application: Application) {
        application.install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }
    }
}