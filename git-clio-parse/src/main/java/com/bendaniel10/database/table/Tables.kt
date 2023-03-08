package com.bendaniel10.database.table

import org.jetbrains.exposed.sql.Table

object Tables {
    val list = listOf<Table>(
        Users,
        Reports,
        Issues,
        IssueEvents,
        PullRequests,
        PullRequestReviews,
    )
}