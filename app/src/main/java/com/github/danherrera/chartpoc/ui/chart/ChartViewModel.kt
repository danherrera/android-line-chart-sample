package com.github.danherrera.chartpoc.ui.chart

import android.graphics.Color
import android.util.Log
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
                    var line1X = 0f
                    var line1Y = 0f
                    val line1Coordinates = mutableListOf<Pair<Float, Float>>(
//                        line1X to line1Y
                    )
                    var line2X = 0f
                    var line2Y = 0f
                    val line2Coordinates = mutableListOf<Pair<Float, Float>>(
//                        line2X to line2Y
                    )
                    while (true) {
                        line1Y -= 0.1f
                        line1X = Math.sin(0.95 * line1Y.toDouble()).toFloat()
                        line1Coordinates.add(line1X to line1Y)

                        line2Y -= 0.1f
                        line2X = Math.cos(0.5 * line1Y.toDouble()).toFloat()
                        line2Coordinates.add(line2X to line2Y)

                        delay(1000L)

                        withContext(Dispatchers.Main) {
                            next(
                                ChartEvent.DomainEvent.LineDataUpdated(
                                    Line(Color.RED, line1Coordinates),
                                    Line(Color.BLUE, line2Coordinates)
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

    fun loggingMiddleware(event: ChartEvent, next: (ChartEvent) -> ChartState): ChartState {
        Log.d(this::class.java.simpleName, "==> $event")
        return next(event)
    }

    init {
        applyMiddleware(::loggingMiddleware)
        applyMiddleware(::chartDataMiddleware)
        applyMiddleware(::trackDataMiddleware)
        sendEvent(ChartEvent.ViewModelEvent.Created)
    }

    override fun reducer(state: ChartState, event: ChartEvent): ChartState {
        return when (event) {
            is ChartEvent.DomainEvent.LineDataUpdated -> state.copy(
                line1 = event.line1,
                line2 = event.line2
            )
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