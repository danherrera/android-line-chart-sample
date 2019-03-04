package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChartViewModel : BaseViewModel<ChartEvent, ChartState>({ ChartState() }) {

    private fun chartDataMiddleware(intent: ChartEvent, next: (ChartEvent) -> ChartState): ChartState {
        if (intent is ChartEvent.DomainEvent.StartTrackingData) {
            launch {
                withContext(Dispatchers.IO) {
                    val coordinates = mutableListOf(
                        2f to 3f,
                        3f to 4f
                    )
                    var x = 3f
                    var y = 4f
                    while (true) {
                        x++
                        y += when (x) {
                            in 0f..250f -> x
                            in 250f..500f -> -x
                            else -> x
                        }

                        coordinates.add(x to y)

                        delay(200L)

                        withContext(Dispatchers.Main) {
                            next(
                                ChartEvent.DomainEvent.LineDataUpdated(
                                    coordinates
                                )
                            )
                        }
                    }
                }
            }
        }
        return next(intent)
    }

    private fun trackDataMiddleware(intent: ChartEvent, next: (ChartEvent) -> ChartState): ChartState {
        return next(intent).also {
            if (intent is ChartEvent.ViewModelEvent.Created) {
                next(ChartEvent.DomainEvent.StartTrackingData)
            }
        }
    }

    init {
        applyMiddleware(::chartDataMiddleware)
        applyMiddleware(::trackDataMiddleware)
        sendEvent(ChartEvent.ViewModelEvent.Created)
    }

    override fun reducer(state: ChartState, event: ChartEvent): ChartState {
        return when (event) {
            is ChartEvent.DomainEvent.LineDataUpdated -> state.copy(xyCoordinates = event.xyCoordinates)
            is ChartEvent.ViewEvent.ClickChart -> state.copy(
                chartImplementation = when (event.chartImplementation) {
                    ChartImplementation.MPAndroidChart -> ChartImplementation.HelloCharts
                    ChartImplementation.HelloCharts -> ChartImplementation.MPAndroidChart
                }
            )
            else -> state
        }
    }
}