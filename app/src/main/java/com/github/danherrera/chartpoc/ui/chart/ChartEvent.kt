package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.Effect
import com.github.danherrera.chartpoc.ui.base.Event

sealed class ChartEvent : Event {

    sealed class ViewEvent : ChartEvent() {
        object Resumed : ViewEvent()

        sealed class LineChartEvent : ViewEvent() {
            data class ClickChart(val chartImplementation: ChartImplementation) : ViewEvent()
            object NothingSelected : LineChartEvent()
            data class ValueSelected(val lineIndex: Int, val x: Float, val y: Float) : LineChartEvent()
            data class Translate(val dx: Float, val dy: Float) : LineChartEvent()
        }
    }

    sealed class DomainEvent : ChartEvent() {
        object StartTrackingData : DomainEvent()
        data class LineDataUpdated(val lines: List<Line>) : DomainEvent()
    }

    sealed class ViewModelEvent : ChartEvent() {
        object Created : ViewModelEvent()
    }

    sealed class ChartEffect : ChartEvent(), Effect {
        data class ShowToastWithResource(val message: Int) : ChartEffect()
        data class ShowToast(val message: String) : ChartEffect()
    }
}