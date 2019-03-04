package com.github.danherrera.chartpoc.ui.chart

import com.github.danherrera.chartpoc.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

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

                        delay(Random.nextLong(1, 2000))

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
            else -> state
        }
    }
}