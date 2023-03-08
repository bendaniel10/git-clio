package com.bendaniel10.database.table

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Issues : IntIdTable() {
    val url = varchar("url", 200)
    val title = varchar("title", 600)
    val number = integer("number")
    val createdAt = datetime("createdAt")
    val closedAt = datetime("closedAt").nullable()
    val state = varchar("state", 50)
    val user = reference("user", Users)
    val report = reference("report", Reports)
}

class IssueEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<IssueEntity>(Issues)

    var url by Issues.url
    var title by Issues.title
    var number by Issues.number
    var createdAt by Issues.createdAt
    var closedAt by Issues.closedAt
    var state by Issues.state
    var user by UserEntity referencedOn Issues.user
    var report by ReportEntity referencedOn Issues.report
}
