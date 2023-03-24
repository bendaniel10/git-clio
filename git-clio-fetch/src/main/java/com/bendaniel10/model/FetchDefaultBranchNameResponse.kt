package com.bendaniel10.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchDefaultBranchNameResponse (
    @SerialName("default_branch")
    val defaultBranch: String
)
