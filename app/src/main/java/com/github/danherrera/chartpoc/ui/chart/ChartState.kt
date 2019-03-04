package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.State

data class ChartState(
    val xyCoordinates: List<Pair<Float, Float>> = emptyList()
) : State