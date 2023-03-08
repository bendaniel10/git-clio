package com.bendaniel10.persist

import com.bendaniel10.database.table.UserEntity
import com.bendaniel10.database.table.Users

internal const val DELETED_USER = "deleted.user"

internal fun getOrCreateUserByLogin(login: String) =
    UserEntity.find { Users.login eq login }.firstOrNull() ?: UserEntity.new {
        this.login = login
    }