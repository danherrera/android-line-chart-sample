package com.github.danherrera.chartpoc.ui.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.danherrera.chartpoc.R
import com.github.danherrera.chartpoc.ui.base.ViewWithEffect
import com.github.danherrera.chartpoc.ui.base.bindClick
import com.github.danherrera.chartpoc.ui.base.bindState
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
            legend.isEnabled = false
            setPinchZoom(true)
            axisRight.isEnabled = false
            description = Description().apply {
                isEnabled = false
            }
            xAxis.labelRotationAngle = -90f
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    viewModel.sendEvent(ChartEvent.ViewEvent.LineChartEvent.NothingSelected)
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    h?.let {
                        e?.run {
                            viewModel.sendEvent(
                                ChartEvent.ViewEvent.LineChartEvent.ValueSelected(
                                    it.dataSetIndex,
                                    x,
                                    y
                                )
                            )
                        }
                    }
                }
            })
            onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureEnd(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
                }

                override fun onChartSingleTapped(me: MotionEvent?) {
                }

                override fun onChartGestureStart(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                }

                override fun onChartLongPressed(me: MotionEvent?) {
                }

                override fun onChartDoubleTapped(me: MotionEvent?) {
                }

                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                }

            }
            // Fixed chart viewport to avoid autoscroll effect when updating data
            xAxis.axisMaximum = 1000f
            xAxis.axisMinimum = 0f
            axisLeft.axisMaximum = 15f
            axisLeft.axisMinimum = -15f
            isScaleXEnabled = true
            isScaleYEnabled = false
            zoom(9f, 1f, 0f, 0f)
        }

        viewModel.bindClick(
            mpLineChart,
            ChartEvent.ViewEvent.LineChartEvent.ClickChart(ChartImplementation.MPAndroidChart)
        )
        viewModel.bindClick(
            helloLineChart,
            ChartEvent.ViewEvent.LineChartEvent.ClickChart(ChartImplementation.HelloCharts)
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendEvent(ChartEvent.ViewEvent.Resumed)
    }

    override fun setState(state: ChartState) {
        mpLineChart.visibility = View.GONE
        helloLineChart.visibility = View.GONE

        activity?.title = state.pageTitle

        selectedValueFrame.visibility = state.selectedValue?.let { View.VISIBLE } ?: View.GONE
        state.selectedValue?.let { (color, x, y) ->
            selectedValueFrame.setBackgroundColor(color)
            selectedValueFrame.background.alpha = 190
            selectedXValueLabel.text = "X Value"
            selectedXValue.text = x.toString()
            selectedYValueLabel.text = "Y Value"
            selectedYValue.text = y.toString()
        }

        when (state.chartImplementation) {
            ChartImplementation.MPAndroidChart -> {
                mpLineChart.visibility = View.VISIBLE

                val chartHeight = mpLineChart.height
                val chartWidth = mpLineChart.width
                mpLineChart.rotation = 90f
                if (chartHeight > chartWidth) {
                    mpLineChart.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = chartHeight
                        height = chartWidth
                    }
                }

                try {
                    mpLineChart.data = LineData(state.lines.map(::mpLineDataSet))
                    mpLineChart.notifyDataSetChanged()
                    mpLineChart.invalidate()
                    // Use moveViewToX for tracking latest data (will require more complex state management to determine when to track data vs. let user interact with chart)
//                    mpLineChart.moveViewToX(Math.max(mpLineChart.data.xMax - 90f, 0f))
                } catch (cme: ConcurrentModificationException) {
                    //ignore: show last state
                }
            }
            ChartImplementation.HelloCharts -> {
                helloLineChart.visibility = View.VISIBLE

                try {
                    val line1Lines = mutableListOf<Line>()
                    var previousCoordinates: Pair<Float, Float>? = null
                    state.lines.forEach { line ->
                        line.coordinates.forEach { (x, y) ->
                            previousCoordinates?.let { (pX, pY) ->
                                line1Lines.add(
                                    Line(
                                        listOf(
                                            PointValue(pX, pY),
                                            PointValue(x, y)
                                        )
                                    ).apply {
                                        color = line.color
                                    }
                                )
                            }
                            previousCoordinates = x to y
                        }
                    }

                    helloLineChart.lineChartData = LineChartData(line1Lines).apply {
                        val xAxisValues = (0..100).map { it.toFloat() / 10f }
                        axisXBottom = Axis.generateAxisFromCollection(
                            xAxisValues,
                            xAxisValues.map { it.toString() }
                        )
                        val yAxisValues = (0..100).map { it.toFloat() / 10f }
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

    private fun mpLineDataSet(line: com.github.danherrera.chartpoc.ui.chart.Line): LineDataSet {
        return LineDataSet(line.coordinates.map { Entry(it.first, it.second) }, line.name).apply {
            color = line.color
            highLightColor = line.color
            lineWidth = 5f
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawValues(false)
        }
    }
}