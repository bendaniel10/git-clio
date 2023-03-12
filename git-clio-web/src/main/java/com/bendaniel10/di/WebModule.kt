package com.bendaniel10.di

import com.bendaniel10.WebSdk
import com.bendaniel10.WebSdkImpl
import com.bendaniel10.routing.get.ViewCreateReportPage
import com.bendaniel10.routing.get.ViewIndexPage
import com.bendaniel10.routing.get.ViewReportDetailsPage
import com.bendaniel10.routing.post.DoCreateReport
import com.bendaniel10.serverfeature.CompositeWebServerFeature
import com.bendaniel10.serverfeature.FreemarkerWebServerFeature
import com.bendaniel10.serverfeature.RoutingWebServerFeature
import com.bendaniel10.serverfeature.WebServerFeature
import org.koin.dsl.module

object WebModule {
    fun get() = module {
        single<WebSdk> { WebSdkImpl() }
        single { FreemarkerWebServerFeature() }

        // GET routes
        single { ViewIndexPage() }
        single { ViewCreateReportPage() }
        single { ViewReportDetailsPage() }

        // POST routes
        single { DoCreateReport() }

        single { RoutingWebServerFeature(get(), get(), get(), get()) }
        single<WebServerFeature> { CompositeWebServerFeature(get(), get()) }
    }
}