package com.bendaniel10.di

import com.bendaniel10.ParseSdk
import com.bendaniel10.ParseSdkImpl
import com.bendaniel10.database.repo.ViewReportDetailsRepo
import com.bendaniel10.database.repo.ViewReportDetailsRepoImpl
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
        databaseHost: String = "database",
        databasePort: Int = 5432
    ) = module {
        single<PullRequestParser> { CompositePullRequestParser }
        single<IssueParser> { CompositeIssueParser }
        single<InfoBagCosmeticFormatter> { CompositeCosmeticFormatter }
        single<PullRequestPersister> { PullRequestPersisterImpl() }
        single<IssuePersister> { IssuePersisterImpl() }
        single<ParseSdk> { ParseSdkImpl() }
        single<ViewReportDetailsRepo> { ViewReportDetailsRepoImpl() }
        single {
            Database.connect(
                "jdbc:postgresql://$databaseHost:$databasePort/$databaseName",
                user = databaseUsername,
                password = databasePassword
            )
        }
    }
}
