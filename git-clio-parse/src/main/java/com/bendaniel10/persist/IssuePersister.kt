package com.bendaniel10.persist

import com.bendaniel10.FetchSdkResponse
import com.bendaniel10.database.table.IssueEntity
import com.bendaniel10.database.table.IssueEventEntity
import com.bendaniel10.database.table.Issues
import com.bendaniel10.database.table.ReportEntity
import com.bendaniel10.toLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

interface IssuePersister {
    fun persist(issue: FetchSdkResponse.Issue, reportEntity: ReportEntity)
}

class IssuePersisterImpl : IssuePersister {
    private val logger = LoggerFactory.getLogger(IssuePersisterImpl::class.java)

    override fun persist(issue: FetchSdkResponse.Issue, reportEntity: ReportEntity) {
        transaction {
            if (IssueEntity.find {
                    Issues.number eq issue.fetchIssuesItem.number and Issues.report.eq(reportEntity.id)
                }.firstOrNull() != null) {
                logger.debug("Already saved issue ${issue.fetchIssuesItem.number}, skipping")
                return@transaction
            }
            val issueEntity = IssueEntity.new {
                url = issue.fetchIssuesItem.url
                title = issue.fetchIssuesItem.title
                number = issue.fetchIssuesItem.number
                createdAt = issue.fetchIssuesItem.createdAt.toLocalDateTime().toJavaLocalDateTime()
                closedAt = issue.fetchIssuesItem.closedAt?.toLocalDateTime()?.toJavaLocalDateTime()
                state = issue.fetchIssuesItem.state
                user = getOrCreateUserByLogin(issue.fetchIssuesItem.user.login, issue.fetchIssuesItem.user.avatarUrl)
                report = reportEntity
            }

            issue.fetchIssueEvent.forEach { issueEvent ->
                IssueEventEntity.new {
                    user = getOrCreateUserByLogin(
                        issueEvent.actor?.login ?: DELETED_USER,
                        issueEvent.actor?.avatarUrl ?: DELETED_USER_AVATAR,
                    )
                    event = issueEvent.event
                    this.issue = issueEntity
                }
            }
        }
    }
}
