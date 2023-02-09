package com.bendaniel10

import java.time.OffsetDateTime
import java.time.ZoneId

fun String.toLocalDateTime() = OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()