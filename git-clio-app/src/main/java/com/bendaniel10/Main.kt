package com.bendaniel10

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println(SimpleDateFormat("MMMM").parse("January"))
        println(
            OffsetDateTime.parse("2022-01-01T00:00:00+01:00").format(DateTimeFormatter.ofPattern("yyyy-MMMM-dd HH:ss"))
        )
        println(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MMMM-dd HH")))
    }
}