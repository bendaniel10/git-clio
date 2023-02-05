package com.bendaniel10.formatter

import com.bendaniel10.InfoBag

internal object IssueInfoBagCosmeticFormatter : InfoBagCosmeticFormatter {
    override fun format(infoBag: InfoBag) {
        with(infoBag) {
            issueStat = issueStat?.copy(
                openers = issueStat?.openers?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )
            issueStat = issueStat?.copy(
                resolvers = issueStat?.resolvers?.toList()?.sortedByDescending { (_, value) -> value }?.toMap()
                    ?.toMutableMap() ?: mutableMapOf()
            )
        }
    }
}
