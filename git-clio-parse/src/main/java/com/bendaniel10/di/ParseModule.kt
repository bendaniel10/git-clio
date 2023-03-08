package com.bendaniel10.di

import com.bendaniel10.formatter.CompositeCosmeticFormatter
import com.bendaniel10.formatter.InfoBagCosmeticFormatter
import com.bendaniel10.parser.CompositeIssueParser
import com.bendaniel10.parser.CompositePullRequestParser
import com.bendaniel10.parser.IssueParser
import com.bendaniel10.parser.PullRequestParser
import com.bendaniel10.persist.IssuePersister
import com.bendaniel10.persist.IssuePersisterImpl
import com.bendaniel10.persist.PullRequestPersister
import com.bendaniel10.persist.PullRequestPersisterImpl
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

object ParseModule {
    fun get(
        databaseName: String = "gitclio",
        databaseUsername: String = "gitclio",
        databasePassword: String = "gitclio",
    ) = module {
        single<PullRequestParser> { CompositePullRequestParser }
        single<IssueParser> { CompositeIssueParser }
        single<InfoBagCosmeticFormatter> { CompositeCosmeticFormatter }
        single<PullRequestPersister> { PullRequestPersisterImpl() }
        single<IssuePersister> { IssuePersisterImpl() }
        single {
            Database.connect(
                "jdbc:postgresql://database:5432/$databaseName",
                user = databaseUsername,
                password = databasePassword
            )
        }
    }
}
