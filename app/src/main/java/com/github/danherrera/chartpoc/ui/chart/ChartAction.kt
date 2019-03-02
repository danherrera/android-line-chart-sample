package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.Effect
import com.github.danherrera.chartpoc.ui.base.Action
import com.github.mikephil.charting.data.LineData

sealed class ChartAction : Action {

    sealed class UiAction : ChartAction() {
        object Resumed : UiAction()
    }

    sealed class DomainAction : ChartAction() {
        object StartTrackingData : DomainAction()
        data class LineDataUpdated(val lineData: LineData) : DomainAction()
    }

    sealed class ViewModelAction : ChartAction() {
        object Created : ViewModelAction()
    }

    sealed class ChartEffect : ChartAction(), Effect {
        data class ShowToastWithResource(val message: Int) : ChartEffect()
        data class ShowToast(val message: String) : ChartEffect()
    }
}