package com.github.danherrera.chartpoc.ui.chart

import android.graphics.Color
import com.github.danherrera.chartpoc.ui.base.State

data class ChartState(
    val chartImplementation: ChartImplementation = ChartImplementation.HelloCharts,
    val line1: Line = Line(),
    val line2: Line = Line()
) : State {

    val pageTitle: String
    get() = when (chartImplementation) {
        ChartImplementation.MPAndroidChart -> "MPAndroidCharts LineChart"
        ChartImplementation.HelloCharts -> "HelloCharts LineChart"
    }
}

enum class ChartImplementation {
    MPAndroidChart, HelloCharts
}

data class Line(
    val color: Int = Color.BLACK,
    val coordinates: List<Pair<Float, Float>> = emptyList()
)