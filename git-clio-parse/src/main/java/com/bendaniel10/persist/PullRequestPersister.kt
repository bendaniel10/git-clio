package com.bendaniel10.persist

import com.bendaniel10.FetchSdkResponse
import com.bendaniel10.database.table.PullRequestEntity
import com.bendaniel10.database.table.PullRequestReviewEntity
import com.bendaniel10.database.table.ReportEntity
import com.bendaniel10.toLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction

interface PullRequestPersister {
    fun persist(pullRequest: FetchSdkResponse.PullRequest, reportEntity: ReportEntity)
}

class PullRequestPersisterImpl : PullRequestPersister {
    override fun persist(pullRequest: FetchSdkResponse.PullRequest, reportEntity: ReportEntity) {
        transaction {
            val userEntity = getOrCreateUserByLogin(pullRequest.fetchPullRequestItem.user.login)
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
                    user = getOrCreateUserByLogin(pullRequest.fetchPullRequestItem.user.login)
                    this.pullRequest = pullRequestEntity
                    user = getOrCreateUserByLogin(reviewResponse.user?.login ?: DELETED_USER)
                }
            }
        }
    }
}