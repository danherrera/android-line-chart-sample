package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.State

data class ChartState(
    val chartImplementation: ChartImplementation = ChartImplementation.MPAndroidChart,
    val xyCoordinates: List<Pair<Float, Float>> = emptyList()
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