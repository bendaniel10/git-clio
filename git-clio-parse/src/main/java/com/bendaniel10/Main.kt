package com.bendaniel10

import com.bendaniel10.di.FetchModule
import com.bendaniel10.parser.CompositeIssueParser
import com.bendaniel10.parser.CompositePullRequestParser
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.io.File
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


fun main() {
    Main.run()
}

@OptIn(ExperimentalTime::class)
object Main : KoinComponent {
    private val fetchSdk: FetchSdkImpl by inject()
    fun run() {
        measureTime {
            runBlocking {
                val clioParseProjectProperties = ClioParseProjectProperties.fromPropertiesFile(File("local.properties"))
                startKoin {
                    modules(
                        FetchModule.get(
                            clioParseProjectProperties.githubUsername,
                            clioParseProjectProperties.githubPersonalAccessToken
                        )
                    )
                }

                val json = Json { encodeDefaults = true }
                val infoBag = InfoBag()

                launch {
                    fetchSdk.response().collect {
                        when (it) {
                            is FetchSdkResponse.Issue -> CompositeIssueParser.parse(it, infoBag)
                            FetchSdkResponse.NoResponse -> {
                                println("No response")
                            }

                            is FetchSdkResponse.PullRequest -> CompositePullRequestParser.parse(it, infoBag)
                            FetchSdkResponse.Completed -> {
                                with(File("github_report.json")) {
                                    if (exists().not()) {
                                        @Suppress("BlockingMethodInNonBlockingContext") createNewFile()
                                    }
                                    writeText(json.encodeToString(infoBag))
                                    println("File can be found here: ${this.absolutePath}")
                                }
                                cancel()
                            }
                        }
                    }
                }
                launch {
                    fetchSdk.start(
                        FetchSdkStartParams(
                            clioParseProjectProperties.githubOrganization,
                            clioParseProjectProperties.githubRepository,
                            clioParseProjectProperties.analyticsStartDate,
                            clioParseProjectProperties.analyticsEndDate,
                        )
                    )
                }
            }
        }.also {
            println("It took ${it.inWholeMinutes} minutes to run this analysis")
        }
    }
}

data class ClioParseProjectProperties(
    val githubUsername: String,
    val githubPersonalAccessToken: String,
    val githubOrganization: String,
    val githubRepository: String,
    val analyticsStartDate: String,
    val analyticsEndDate: String
) {
    companion object {
        fun fromPropertiesFile(propertiesFile: File) = with(propertiesFile) {
            if (exists().not()) throw IllegalStateException("Please create add a local.properties file to the root of the project")
            val properties = Properties().apply { load(inputStream()) }
            ClioParseProjectProperties(
                properties.getProperty("githubUsername"),
                properties.getProperty("githubPersonalAccessToken"),
                properties.getProperty("githubOrganization"),
                properties.getProperty("githubRepository"),
                properties.getProperty("analyticsStartDate"),
                properties.getProperty("analyticsEndDate"),
            )
        }
    }
}
