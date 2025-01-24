package com.example.weatherapp.presentation.extensions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

fun ComponentContext.componentScope(): CoroutineScope {
    return CoroutineScope(
        Dispatchers.Main.immediate + SupervisorJob()
    ).apply {
        lifecycle.doOnDestroy { cancel() }
    }
}

fun Float.tempToFormattedString(): String = "${roundToInt()}°C"

fun Calendar.formattedFullDate(): String {
    val simpleDateFormat = SimpleDateFormat("EEEE | d MMM y", Locale.getDefault())
    return simpleDateFormat.format(time)
}

fun Calendar.formattedShortDayOfWeek(): String {
    val simpleDateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    return simpleDateFormat.format(time)
}