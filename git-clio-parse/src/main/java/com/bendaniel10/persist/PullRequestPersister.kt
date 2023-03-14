package com.bendaniel10.persist

import com.bendaniel10.FetchSdkResponse
import com.bendaniel10.database.table.PullRequestEntity
import com.bendaniel10.database.table.PullRequestReviewEntity
import com.bendaniel10.database.table.PullRequests
import com.bendaniel10.database.table.ReportEntity
import com.bendaniel10.toLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

interface PullRequestPersister {
    fun persist(pullRequest: FetchSdkResponse.PullRequest, reportEntity: ReportEntity)
}

class PullRequestPersisterImpl : PullRequestPersister {
    private val logger = LoggerFactory.getLogger(PullRequestPersisterImpl::class.java)

    override fun persist(pullRequest: FetchSdkResponse.PullRequest, reportEntity: ReportEntity) {
        transaction {
            if (PullRequestEntity.find {
                    PullRequests.number eq pullRequest.fetchPullRequestItem.number and PullRequests.report.eq(
                        reportEntity.id
                    )
                }.firstOrNull() != null) {
                logger.debug("Already saved pull request ${pullRequest.fetchPullRequestItem.number}, skipping")
                return@transaction
            }
            val userEntity = getOrCreateUserByLogin(
                pullRequest.fetchPullRequestItem.user.login,
                pullRequest.fetchPullRequestItem.user.avatarUrl
            )
            val pullRequestEntity = PullRequestEntity.new {
                url = pullRequest.fetchPullRequestItem.url
                title = pullRequest.fetchPullRequestItem.title
                number = pullRequest.fetchPullRequestItem.number
                createdAt = pullRequest.fetchPullRequestItem.createdAt.toLocalDateTime().toJavaLocalDateTime()
                closedAt = pullRequest.fetchPullRequestItem.closedAt?.toLocalDateTime()?.toJavaLocalDateTime()
                mergedAt = pullRequest.fetchPullRequestByIdResponse.mergedAt?.toLocalDateTime()?.toJavaLocalDateTime()
                state = pullRequest.fetchPullRequestByIdResponse.state
                comments = pullRequest.fetchPullRequestByIdResponse.comments
                reviewComments = pullRequest.fetchPullRequestByIdResponse.reviewComments
                commits = pullRequest.fetchPullRequestByIdResponse.commits
                additions = pullRequest.fetchPullRequestByIdResponse.additions
                deletions = pullRequest.fetchPullRequestByIdResponse.deletions
                changedFiles = pullRequest.fetchPullRequestByIdResponse.changedFiles
                autoMerge = pullRequest.fetchPullRequestByIdResponse.autoMerge != null
                user = userEntity
                report = reportEntity
            }

            pullRequest.fetchPullRequestReviewResponse.forEach { reviewResponse ->
                PullRequestReviewEntity.new {
                    state = reviewResponse.state
                    submittedAt = reviewResponse.submittedAt.toLocalDateTime().toJavaLocalDateTime()
                    user = getOrCreateUserByLogin(
                        pullRequest.fetchPullRequestItem.user.login,
                        pullRequest.fetchPullRequestItem.user.avatarUrl
                    )
                    this.pullRequest = pullRequestEntity
                    user = getOrCreateUserByLogin(
                        reviewResponse.user?.login ?: DELETED_USER,
                        reviewResponse.user?.avatarUrl ?: DELETED_USER_AVATAR,
                    )
                }
            }
        }
    }
}