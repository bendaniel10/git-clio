package com.bendaniel10.di

import com.bendaniel10.formatter.CompositeCosmeticFormatter
import com.bendaniel10.formatter.InfoBagCosmeticFormatter
import com.bendaniel10.parser.CompositeIssueParser
import com.bendaniel10.parser.CompositePullRequestParser
import com.bendaniel10.parser.IssueParser
import com.bendaniel10.parser.PullRequestParser
import org.koin.dsl.module

object ParseModule {
    fun get() = module {
        single<PullRequestParser> { CompositePullRequestParser }
        single<IssueParser> { CompositeIssueParser }
        single<InfoBagCosmeticFormatter> { CompositeCosmeticFormatter }
    }
}
