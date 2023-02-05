package com.bendaniel10.formatter

import com.bendaniel10.InfoBag

interface InfoBagCosmeticFormatter {
    fun format(infoBag: InfoBag)
}

object CompositeCosmeticFormatter : InfoBagCosmeticFormatter {
    private val composite = listOf(IssueInfoBagCosmeticFormatter, PullRequestInfoBagCosmeticFormatter)
    override fun format(infoBag: InfoBag) {
        composite.forEach {
            it.format(infoBag)
        }
    }
}
