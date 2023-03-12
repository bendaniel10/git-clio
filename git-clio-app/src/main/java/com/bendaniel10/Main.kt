package com.bendaniel10

import com.bendaniel10.di.FetchModule
import com.bendaniel10.di.ParseModule
import com.bendaniel10.di.WebModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

object Main : KoinComponent {
    private val webSdk: WebSdk by inject()
    private val parseSdk: ParseSdk by inject()
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            modules(
                FetchModule.get(),
                ParseModule.get(),
                WebModule.get()
            )
        }
        parseSdk.onStart()
        webSdk.startWebServer()
    }
}