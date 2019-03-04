package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.Effect
import com.github.danherrera.chartpoc.ui.base.Event

sealed class ChartEvent : Event {

    sealed class ViewEvent : ChartEvent() {
        object Resumed : ViewEvent()
        data class ClickChart(val chartImplementation: ChartImplementation) : ViewEvent()
    }

    sealed class DomainEvent : ChartEvent() {
        object StartTrackingData : DomainEvent()
        data class LineDataUpdated(val xyCoordinates: List<Pair<Float, Float>>) : DomainEvent()
    }

    sealed class ViewModelEvent : ChartEvent() {
        object Created : ViewModelEvent()
    }

    sealed class ChartEffect : Effect {
        data class ShowToastWithResource(val message: Int) : ChartEffect()
        data class ShowToast(val message: String) : ChartEffect()
    }
}