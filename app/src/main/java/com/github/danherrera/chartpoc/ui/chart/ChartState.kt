package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.State
import com.github.mikephil.charting.data.LineData

data class ChartState(
    val lineData: LineData = LineData()
) : State