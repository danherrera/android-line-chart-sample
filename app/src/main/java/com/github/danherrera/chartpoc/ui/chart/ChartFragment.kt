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
import com.github.danherrera.chartpoc.ui.base.bindState
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_chart.*

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
        lineChart.apply {
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

    }

    override fun onResume() {
        super.onResume()
        viewModel.sendEvent(ChartEvent.ViewEvent.Resumed)
    }

    override fun setState(state: ChartState) {
        lineChart.data = LineData(
            LineDataSet(state.xyCoordinates.map { Entry(it.first, it.second) }, "MPAndroidChart").apply {
                color = Color.BLACK
            }
        )
        lineChart.invalidate()
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