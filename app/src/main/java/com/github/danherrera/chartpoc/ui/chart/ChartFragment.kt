package com.github.danherrera.chartpoc.ui.chart

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
import kotlinx.android.synthetic.main.fragment_chart.*

class ChartFragment : Fragment(), ViewWithEffect<ChartState, ChartAction.ChartEffect> {

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
        viewModel.sendIntent(ChartAction.UiAction.Resumed)
    }

    override fun setState(state: ChartState) {
        lineChart.data = state.lineData
        lineChart.invalidate()
    }

    override fun onEffect(effect: ChartAction.ChartEffect) {
        when (effect) {
            is ChartAction.ChartEffect.ShowToastWithResource -> {
                Toast.makeText(context!!, effect.message, Toast.LENGTH_SHORT).show()
            }
            is ChartAction.ChartEffect.ShowToast -> {
                Toast.makeText(context!!, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}