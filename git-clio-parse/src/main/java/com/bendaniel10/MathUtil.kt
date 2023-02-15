package com.bendaniel10

fun rollingAverage(currentAverage: Double, currentItemsSize: Int, newValue: Int) =
    (currentItemsSize * currentAverage + newValue) / (currentItemsSize + 1)