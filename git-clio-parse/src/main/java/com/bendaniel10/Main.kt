package com.bendaniel10

import com.bendaniel10.di.FetchModule
import com.bendaniel10.di.ParseModule
import com.bendaniel10.formatter.InfoBagCosmeticFormatter
import com.bendaniel10.parser.IssueParser
import com.bendaniel10.parser.PullRequestParser
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
    private val issueParser: IssueParser by inject()
    private val pullRequestParser: PullRequestParser by inject()
    private val infoBagCosmeticFormatter: InfoBagCosmeticFormatter by inject()
    private val processedIssues = mutableSetOf<Int>()
    private val processedPullRequests = mutableSetOf<Int>()
    private var expectedTotalPullRequests = 0
    private var expectedTotalIssues = 0

    fun run() {
        val clioParseProjectProperties = ClioParseProjectProperties.fromPropertiesFile(File("local.properties"))
        measureTime {
            runBlocking {
                startKoin {
                    modules(
                        FetchModule.get(
                            clioParseProjectProperties.githubUsername,
                            clioParseProjectProperties.githubPersonalAccessToken
                        ),
                        ParseModule.get()
                    )
                }

                val json = Json { encodeDefaults = true }
                val infoBag = InfoBag()

                launch {
                    fetchSdk.response().collect { fetchSdkResponse ->
                        when (fetchSdkResponse) {
                            is FetchSdkResponse.Issue -> issueParser.parse(fetchSdkResponse, infoBag)
                                .also {
                                    processedIssues.add(fetchSdkResponse.fetchIssuesItem.number)
                                    print("\rProcessed issue: ${processedIssues.size}/$expectedTotalIssues")
                                }

                            is FetchSdkResponse.PullRequest -> pullRequestParser.parse(fetchSdkResponse, infoBag)
                                .also {
                                    processedPullRequests.add(fetchSdkResponse.fetchPullRequestItem.number)
                                    print("\rProcessed pull requests: ${processedPullRequests.size}/$expectedTotalPullRequests")
                                }

                            FetchSdkResponse.Completed -> {
                                infoBagCosmeticFormatter.format(infoBag)
                                with(File("github_report.json")) {
                                    if (exists().not()) {
                                        @Suppress("BlockingMethodInNonBlockingContext") createNewFile()
                                    }
                                    writeText(json.encodeToString(infoBag))
                                    println("File can be found here: ${this.absolutePath}")
                                }
                                cancel()
                            }

                            is FetchSdkResponse.ExpectedIssuesTotal -> expectedTotalIssues = fetchSdkResponse.expected
                            is FetchSdkResponse.ExpectedPullRequestTotal -> expectedTotalPullRequests =
                                fetchSdkResponse.expected
                        }
                    }
                }
                launch {
                    fetchSdk.start(
                        fetchSdkStartParams = FetchSdkStartParams(
                            clioParseProjectProperties.githubOrganization,
                            clioParseProjectProperties.githubRepository,
                            clioParseProjectProperties.analyticsStartDate,
                            clioParseProjectProperties.analyticsEndDate,
                        ),
                        coroutineScope = this
                    )
                }
            }
        }.also {
            println("Analysis for ${clioParseProjectProperties.githubOrganization}/${clioParseProjectProperties.githubRepository} [${clioParseProjectProperties.analyticsStartDate} - ${clioParseProjectProperties.analyticsEndDate}]")
            println("It took ${it.inWholeMinutes} minutes (${it.inWholeSeconds} seconds) to run this analysis")
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
