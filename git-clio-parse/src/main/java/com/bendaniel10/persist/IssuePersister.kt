package com.bendaniel10.persist

import com.bendaniel10.FetchSdkResponse
import com.bendaniel10.database.table.IssueEntity
import com.bendaniel10.database.table.IssueEventEntity
import com.bendaniel10.database.table.ReportEntity
import com.bendaniel10.toLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction

interface IssuePersister {
    fun persist(issue: FetchSdkResponse.Issue, reportEntity: ReportEntity)
}

class IssuePersisterImpl : IssuePersister {
    override fun persist(issue: FetchSdkResponse.Issue, reportEntity: ReportEntity) {
        transaction {
            val issueEntity = IssueEntity.new {
                url = issue.fetchIssuesItem.url
                title = issue.fetchIssuesItem.title
                number = issue.fetchIssuesItem.number
                createdAt = issue.fetchIssuesItem.createdAt.toLocalDateTime().toJavaLocalDateTime()
                closedAt = issue.fetchIssuesItem.closedAt?.toLocalDateTime()?.toJavaLocalDateTime()
                state = issue.fetchIssuesItem.state
                user = getOrCreateUserByLogin(issue.fetchIssuesItem.user.login)
                report = reportEntity
            }

            issue.fetchIssueEvent.forEach { issueEvent ->
                IssueEventEntity.new {
                    user = getOrCreateUserByLogin(issueEvent.actor?.login ?: DELETED_USER)
                    event = issueEvent.event
                    this.issue = issueEntity
                }
            }
        }
    }
}
