package com.bendaniel10.database.table

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object PullRequests : IntIdTable() {
    val url = varchar("url", 200)
    val title = varchar("title", 600)
    val number = integer("number")
    val createdAt = datetime("createdAt")
    val closedAt = datetime("closedAt").nullable()
    val mergedAt = datetime("mergedAt").nullable()
    val state = varchar("state", 50)
    val comments = integer("comments")
    val reviewComments = integer("reviewComments")
    val commits = integer("commits")
    val additions = integer("additions")
    val deletions = integer("deletions")
    val changedFiles = integer("changedFiles")
    val autoMerge = bool("autoMerge")
    val user = reference("user", Users)
    val report = reference("report", Reports)
}

class PullRequestEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PullRequestEntity>(PullRequests)
    var url by PullRequests.url
    var title by PullRequests.title
    var number by PullRequests.number
    var createdAt by PullRequests.createdAt
    var closedAt by PullRequests.closedAt
    var mergedAt by PullRequests.mergedAt
    var state by PullRequests.state
    var comments by PullRequests.comments
    var reviewComments by PullRequests.reviewComments
    var commits by PullRequests.commits
    var additions by PullRequests.additions
    var deletions by PullRequests.deletions
    var changedFiles by PullRequests.changedFiles
    var autoMerge by PullRequests.autoMerge
    var user by UserEntity referencedOn PullRequests.user
    var report by ReportEntity referencedOn PullRequests.report
}

