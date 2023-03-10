package com.bendaniel10.di

import com.bendaniel10.FetchRestApi
import com.bendaniel10.FetchRestApiImpl
import com.bendaniel10.FetchSdk
import com.bendaniel10.FetchSdkImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object FetchModule {

    fun get() = module {
        singleOf(::FetchRestApiImpl) { bind<FetchRestApi>() }
        singleOf(::FetchSdkImpl) { bind<FetchSdk>() }
        single {
            HttpClient(CIO) {
                engine {
                    pipelining = true
                    threadsCount = 10
                    endpoint {
                        pipelineMaxSize = 100
                        connectTimeout = 10_000
                    }
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }
}
