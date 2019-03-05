package com.github.danherrera.chartpoc.ui.chart

import android.graphics.Color
import com.github.danherrera.chartpoc.ui.base.State

data class ChartState(
    val chartImplementation: ChartImplementation = ChartImplementation.MPAndroidChart,
    val lines: List<Line> = emptyList(),
    val selectedValue: SelectedCoordinate? = null,
    val lineChartViewPort: ViewPort = ViewPort()
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

data class ViewPort(
    val upperX: Float = 10f,
    val lowerX: Float = 0f,
    val upperY: Float = 1f,
    val lowerY: Float = -1f
) {
    val centerX = (upperX + lowerX)/2f
    val centerY = (upperY + lowerY)/2f
}

data class SelectedCoordinate(val color: Int, val x: Float, val y: Float)

data class Line(
    val name: String = "",
    val color: Int = Color.BLACK,
    val coordinates: List<Pair<Float, Float>> = emptyList()
)