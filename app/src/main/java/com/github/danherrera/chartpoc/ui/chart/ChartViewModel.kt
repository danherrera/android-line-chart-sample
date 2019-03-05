package com.github.danherrera.chartpoc.ui.chart

import android.graphics.Color
import android.util.Log
import com.github.danherrera.chartpoc.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChartViewModel : BaseViewModel<ChartEvent, ChartState>({ ChartState() }) {

    private fun chartDataMiddleware(intent: ChartEvent, next: (ChartEvent) -> ChartState): ChartState {
        if (intent is ChartEvent.DomainEvent.StartTrackingData) {
            launch {
                withContext(Dispatchers.IO) {
                    var line1X = 0f
                    var line1Y = 0f
                    val line1Coordinates = mutableListOf<Pair<Float, Float>>()
                    var line2X = 0f
                    var line2Y = 0f
                    val line2Coordinates = mutableListOf<Pair<Float, Float>>()
                    var line3X = 0f
                    var line3Y = 0f
                    val line3Coordinates = mutableListOf<Pair<Float, Float>>()
                    while (true) {
                        line1X += 0.1f
                        line1Y = Math.sin(0.95 * line1X.toDouble()).toFloat() - 1f
                        line1Coordinates.add(line1X to line1Y)

                        line2X += 0.1f
                        line2Y = Math.cos(0.5 * line2X.toDouble()).toFloat()
                        line2Coordinates.add(line2X to line2Y)

                        line3X += 0.1f
                        line3Y = Math.sin(0.5 * line3X.toDouble()).toFloat() + 2f
                        line3Coordinates.add(line3X to line3Y)

//                        delay(1000L)

                        withContext(Dispatchers.Main) {
                            next(
                                ChartEvent.DomainEvent.LineDataUpdated(
                                    listOf(
                                        Line("Line 1", Color.RED, line1Coordinates),
                                        Line("Line 2", Color.BLUE, line2Coordinates),
                                        Line("Line 3", Color.GREEN, line3Coordinates)
                                    )
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
            is ChartEvent.DomainEvent.LineDataUpdated -> state.copy(lines = event.lines)
            is ChartEvent.ViewEvent.LineChartEvent.ValueSelected -> state.copy(
                selectedValue = SelectedCoordinate(
                    color = state.lines[event.lineIndex].color,
                    x = event.x,
                    y = event.y
                )
            )
            is ChartEvent.ViewEvent.LineChartEvent.NothingSelected -> state.copy(
                selectedValue = null
            )
//            is ChartEvent.ViewEvent.ClickChart -> state.copy(
//                chartImplementation = when (event.chartImplementation) {
//                    ChartImplementation.MPAndroidChart -> ChartImplementation.HelloCharts
//                    ChartImplementation.HelloCharts -> ChartImplementation.MPAndroidChart
//                }
//            )
            else -> state
        }
    }
}