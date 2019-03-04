package com.github.danherrera.chartpoc.ui.chart

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.danherrera.chartpoc.R
import com.github.danherrera.chartpoc.ui.base.ViewWithEffect
import com.github.danherrera.chartpoc.ui.base.bindClick
import com.github.danherrera.chartpoc.ui.base.bindState
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_chart.*
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.model.Line

class ChartFragment : Fragment(), ViewWithEffect<ChartState, ChartEvent.ChartEffect> {

    lateinit var viewModel: ChartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        viewModel = ViewModelProviders.of(this).get(ChartViewModel::class.java)
        viewModel.bindState(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mpLineChart.apply {
            isScaleYEnabled = true
            isScaleXEnabled = true
            isDragEnabled = true
            setPinchZoom(true)
            xAxis.axisMaximum = 1f
            xAxis.axisMinimum = -1f
            axisLeft.axisMaximum = 0f
            axisLeft.axisMinimum = -150f
            axisRight.isEnabled = false
        }

        viewModel.bindClick(mpLineChart, ChartEvent.ViewEvent.ClickChart(ChartImplementation.MPAndroidChart))
        viewModel.bindClick(helloLineChart, ChartEvent.ViewEvent.ClickChart(ChartImplementation.HelloCharts))
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendEvent(ChartEvent.ViewEvent.Resumed)
    }

    override fun setState(state: ChartState) {
        mpLineChart.visibility = View.GONE
        helloLineChart.visibility = View.GONE

        activity?.title = state.pageTitle

        when (state.chartImplementation) {
            ChartImplementation.MPAndroidChart -> {
                mpLineChart.visibility = View.VISIBLE
                try {
                    mpLineChart.data = LineData(
                        LineDataSet(state.line1.coordinates.map { Entry(it.first, it.second) }, "Line 1").apply {
                            color = state.line1.color
                            highLightColor = state.line1.color
                            lineWidth = 5f
                        },
                        LineDataSet(state.line2.coordinates.map { Entry(it.first, it.second) }, "Line 2").apply {
                            color = state.line2.color
                            highLightColor = state.line2.color
                            lineWidth = 5f
                        }
                    )
                    mpLineChart.notifyDataSetChanged()
                    mpLineChart.invalidate()
                } catch (e: Exception) {
                    //ignore
                }
            }
            ChartImplementation.HelloCharts -> {
                helloLineChart.visibility = View.VISIBLE

                try {
                    val line1Lines = mutableListOf<Line>()
                    var previousCoordinates: Pair<Float, Float>? = null
                    state.line1.coordinates.forEach { (x, y) ->
                        previousCoordinates?.let { (pX, pY) ->
                            line1Lines.add(
                                Line(
                                    listOf(
                                        PointValue(pX, pY),
                                        PointValue(x, y)
                                    )
                                ).apply {
                                    color = state.line1.color
                                }
                            )
                        }
                        previousCoordinates = x to y
                    }

                    val line2Lines = mutableListOf<Line>()
                    previousCoordinates = null
                    state.line2.coordinates.forEach { (x, y) ->
                        previousCoordinates?.let { (pX, pY) ->
                            line1Lines.add(
                                Line(
                                    listOf(
                                        PointValue(pX, pY),
                                        PointValue(x, y)
                                    )
                                ).apply {
                                    color = state.line2.color
                                }
                            )
                        }
                        previousCoordinates = x to y
                    }

                    helloLineChart.lineChartData = LineChartData(line1Lines).apply {
                        val xAxisValues = listOf(-1f, -0.5f, 0f, 0.5f, 1.0f)
                        axisXBottom = Axis.generateAxisFromCollection(
                            xAxisValues,
                            xAxisValues.map { it.toString() }
                        )
                        val yAxisValues = (0..60_000 step 4_000).map { it.toFloat() }
                        axisYLeft = Axis.generateAxisFromCollection(
                            yAxisValues,
                            yAxisValues.map { it.toString() }
                        )
                    }
                    helloLineChart.invalidate()
                } catch (cme: ConcurrentModificationException) {
                    //ignore: show last displayed state
                }
            }
        }

    }

    override fun onEffect(effect: ChartEvent.ChartEffect) {
        when (effect) {
            is ChartEvent.ChartEffect.ShowToastWithResource -> {
                Toast.makeText(context!!, effect.message, Toast.LENGTH_SHORT).show()
            }
            is ChartEvent.ChartEffect.ShowToast -> {
                Toast.makeText(context!!, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}