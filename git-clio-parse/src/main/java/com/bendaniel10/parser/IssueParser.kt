package com.bendaniel10.parser

import com.bendaniel10.FetchSdkResponse
import com.bendaniel10.InfoBag
import com.bendaniel10.IssueStat

interface IssueParser {
    fun parse(issue: FetchSdkResponse.Issue, infoBag: InfoBag)
}

object CompositeIssueParser : IssueParser {
    private val composite = listOf(IssueStatParser)
    private val processed = mutableSetOf<String>()

    override fun parse(issue: FetchSdkResponse.Issue, infoBag: InfoBag) {
        if (processed.contains(issue.fetchIssuesItem.number.toString())) {
            println("Already processed ${issue.fetchIssuesItem.number}")
            return
        }
        composite.forEach {
            it.parse(issue, infoBag)
        }
        processed.add(issue.fetchIssuesItem.number.toString())
    }
}

object IssueStatParser : IssueParser {
    override fun parse(issue: FetchSdkResponse.Issue, infoBag: InfoBag) {
        val fetchIssuesItem = issue.fetchIssuesItem
        val fetchIssueEvent = issue.fetchIssueEvent

        val issueStat = infoBag.issueStat ?: IssueStat()

        infoBag.issueStat = issueStat.copy(
            total = issueStat.total.plus(1),
            resolved = if (fetchIssuesItem.state == "closed") issueStat.resolved.plus(1) else issueStat.resolved,
            openers = issueStat.openers.apply {
                put(
                    fetchIssuesItem.user.login,
                    getOrDefault(fetchIssuesItem.user.login, 0).plus(1)
                )
            },
            resolvers = issueStat.resolvers.apply {
                fetchIssueEvent.forEach {
                    if (it.event.equals("closed", true)) {
                        put(
                            it.actor.login,
                            getOrDefault(it.actor.login, 0).plus(1)
                        )
                    }
                }
            }
        )
    }
}
