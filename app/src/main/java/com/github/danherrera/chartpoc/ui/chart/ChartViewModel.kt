package com.github.danherrera.chartpoc.ui.chart

import android.graphics.Color
import com.github.danherrera.chartpoc.ui.base.BaseViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ChartViewModel : BaseViewModel<ChartAction, ChartState>({ ChartState() }) {

    private fun chartDataMiddleware(intent: ChartAction, next: (ChartAction) -> ChartState): ChartState {
        if (intent is ChartAction.DomainAction.StartTrackingData) {
            launch {
                withContext(Dispatchers.IO) {
                    val entries = mutableListOf(
                        Entry(2f, 3f),
                        Entry(3f, 4f)
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

                        entries.add(Entry(x, y))

                        delay(Random.nextLong(1, 2000))

                        withContext(Dispatchers.Main) {
                            next(
                                ChartAction.DomainAction.LineDataUpdated(
                                    LineData(
                                        LineDataSet(entries, "My Chart").apply { color = Color.BLACK })
                                )
                            )
                        }
                    }
                }
            }
        }
        return next(intent)
    }

    private fun trackDataMiddleware(intent: ChartAction, next: (ChartAction) -> ChartState): ChartState {
        return next(intent).also {
            if (intent is ChartAction.ViewModelAction.Created) {
                next(ChartAction.DomainAction.StartTrackingData)
            }
        }
    }

    init {
        applyMiddleware(::chartDataMiddleware)
        applyMiddleware(::trackDataMiddleware)
        sendIntent(ChartAction.ViewModelAction.Created)
    }

    override fun reducer(state: ChartState, intent: ChartAction): ChartState {
        return when (intent) {
            is ChartAction.DomainAction.LineDataUpdated -> state.copy(lineData = intent.lineData)
            else -> state
        }
    }
}