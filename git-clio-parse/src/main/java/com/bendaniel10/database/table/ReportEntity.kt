package com.bendaniel10.database.table

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object Reports : IntIdTable() {
    val gitHubUsername = varchar("gitHubUsername", 120)
    val gitHubAccessToken = varchar("gitHubAccessToken", 120)
    val githubOrganization = varchar("githubOrganization", 120)
    val githubRepository = varchar("githubRepository", 120)
    val name = varchar("name", 120)
    val totalIssues = integer("totalIssues").default(0)
    val totalPullRequests = integer("totalPullRequests").default(0)
    val reportStatus = enumerationByName<ReportStatus>("reportStatus", 50)
    val created = datetime("created")
    val analyticsStartDate = date("analyticsStartDate")
    val analyticsEndDate = date("analyticsEndDate")
    val processingStartedAt = datetime("processingStartedAt").nullable()
    val processingCompletedAt = datetime("processingCompletedAt").nullable()
}

class ReportEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ReportEntity>(Reports)

    var gitHubUsername by Reports.gitHubUsername
    var gitHubAccessToken by Reports.gitHubAccessToken
    var githubRepository by Reports.githubRepository
    var githubOrganization by Reports.githubOrganization
    var totalIssues by Reports.totalIssues
    var totalPullRequests by Reports.totalPullRequests
    var name by Reports.name
    var reportStatus by Reports.reportStatus
    var created by Reports.created
    var analyticsStartDate by Reports.analyticsStartDate
    var analyticsEndDate by Reports.analyticsEndDate
    var processingStartedAt by Reports.processingStartedAt
    var processingCompletedAt by Reports.processingCompletedAt
    val pullRequests by PullRequestEntity referrersOn PullRequests.report
}

enum class ReportStatus {
    PROCESSING,
    PROCESSED,
    WAITING_TO_BE_PROCESSED,
    NOT_PROCESSED
}