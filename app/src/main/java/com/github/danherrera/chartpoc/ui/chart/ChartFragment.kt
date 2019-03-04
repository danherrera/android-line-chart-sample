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
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue

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
//            xAxis.axisMaximum = 500f
//            xAxis.axisMinimum = 0f
//            axisLeft.axisMaximum = 50_000f
//            axisLeft.axisMinimum = -80_000f
//            axisRight.isEnabled = false
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
                mpLineChart.data = LineData(
                    LineDataSet(state.xyCoordinates.map { Entry(it.first, it.second) }, "MPAndroidChart").apply {
                        color = Color.BLACK
                    }
                )
                mpLineChart.invalidate()
            }
            ChartImplementation.HelloCharts -> {
                helloLineChart.visibility = View.VISIBLE

                try {
                    val lines = mutableListOf<Line>()
                    var previousCoordinates: Pair<Float, Float>? = null
                    state.xyCoordinates.forEach { (x, y) ->
                        previousCoordinates?.let { (pX, pY) ->
                            lines.add(
                                Line(
                                    listOf(
                                        PointValue(pX, pY),
                                        PointValue(x, y)
                                    )
                                )
                            )
                        }
                        previousCoordinates = x to y
                    }

                    helloLineChart.lineChartData = LineChartData(lines).apply {
                        axisXBottom = Axis.generateAxisFromCollection(
                            listOf(0f, 100f, 200f, 300f, 400f, 500f),
                            listOf("0", "100", "200", "300", "400", "500")
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