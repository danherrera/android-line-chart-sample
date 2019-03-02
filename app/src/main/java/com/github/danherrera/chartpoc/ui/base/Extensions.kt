package com.github.danherrera.chartpoc.ui.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

inline fun <reified E : Effect> BaseViewModel<*, *>.observeEffects(
    owner: LifecycleOwner,
    crossinline effects: (E) -> Unit
) {
    effectLiveData.observe(owner, Observer { holder ->
        holder?.consume()?.let {
            if (it is E) effects(it)
        }
    })
}

inline fun <reified S : State> BaseViewModel<*, S>.observeState(
    owner: LifecycleOwner,
    crossinline state: (S) -> Unit
) {
    stateLiveData.observe(owner, Observer { it?.let(state) })
}

fun <S : State, V> BaseViewModel<*, S>.bindState(
    view: V
) where V : View<S>, V : LifecycleOwner {
    stateLiveData.observe(view, Observer { it?.let(view::setState) })
}

inline fun <S : State, V, reified E : Effect> BaseViewModel<*, S>.bindState(
    view: V
) where V : ViewWithEffect<S, E>, V : LifecycleOwner {
    stateLiveData.observe(view, Observer { it?.let(view::setState) })
    observeEffects(view, view::onEffect)
}

fun <I : Action, S : State> BaseViewModel<I, S>.bindClick(view: android.view.View, clickIntent: I) {
    view.setOnClickListener { sendIntent(clickIntent) }
}

