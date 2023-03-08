package com.bendaniel10.database.table

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object IssueEvents : IntIdTable() {
    val user = reference("user", Users)
    val issue = reference("issue", Issues)
    val event = varchar("event", 100)
}

class IssueEventEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<IssueEventEntity>(IssueEvents)
    var event by IssueEvents.event
    var user by UserEntity referencedOn IssueEvents.user
    var issue by IssueEntity referencedOn IssueEvents.issue
}
