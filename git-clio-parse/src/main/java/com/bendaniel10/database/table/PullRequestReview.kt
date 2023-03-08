package com.bendaniel10.database.table

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object PullRequestReviews : IntIdTable() {
    val state = varchar("state", 100)
    val submittedAt = datetime("submittedAt")
    val user = reference("user", Users)
    val pullRequest = reference("pullRequest", PullRequests)
}

class PullRequestReviewEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PullRequestReviewEntity>(PullRequestReviews)

    var state by PullRequestReviews.state
    var submittedAt by PullRequestReviews.submittedAt
    var user by UserEntity referencedOn PullRequestReviews.user
    var pullRequest by PullRequestEntity referencedOn PullRequestReviews.pullRequest
}
