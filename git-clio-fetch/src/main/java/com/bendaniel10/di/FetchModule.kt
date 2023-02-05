package com.bendaniel10.di

import com.bendaniel10.FetchRestApi
import com.bendaniel10.FetchRestApiImpl
import com.bendaniel10.FetchSdk
import com.bendaniel10.FetchSdkImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object FetchModule {

    fun get(githubUsername: String, githubPersonalAccessToken: String) = module {
        singleOf(::FetchRestApiImpl) { bind<FetchRestApi>() }
        singleOf(::FetchSdkImpl) { bind<FetchSdk>() }
        single {
            HttpClient(CIO) {
                engine {
                    threadsCount = 10
                    pipelining = true
                    endpoint {
                        pipelineMaxSize = 100
                    }
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Auth) {
                    basic {
                        sendWithoutRequest { true }
                        credentials {
                            BasicAuthCredentials(
                                githubUsername,
                                githubPersonalAccessToken
                            )
                        }
                    }
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }
}
