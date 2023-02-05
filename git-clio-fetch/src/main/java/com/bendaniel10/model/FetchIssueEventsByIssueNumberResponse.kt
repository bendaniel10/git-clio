package com.bendaniel10.model

import kotlinx.serialization.Serializable

typealias IssueEventsResponse = List<IssueEventsItem>

@Serializable
data class IssueEventsItem(
    val actor: IssueEventsItemActor,
    val event: String // assigned, renamed, closed (https://docs.github.com/en/developers/webhooks-and-events/events/issue-event-types#closed)
)
@Serializable
data class IssueEventsItemActor(
    val login: String
)
