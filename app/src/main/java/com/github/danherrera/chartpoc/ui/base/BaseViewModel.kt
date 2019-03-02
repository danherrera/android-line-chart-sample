package com.github.danherrera.chartpoc.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface Action
interface State
interface Effect

class EffectHolder<out E : Effect>(private val content: E) {
    var consumed = false
        private set

    fun consume(): E? {
        return if (consumed) {
            null
        } else {
            consumed = true
            content
        }
    }
}

interface View<S : State> {
    fun setState(state: S)
}

interface ViewWithEffect<S : State, E : Effect> : View<S> {
    fun onEffect(effect: E)
}

typealias Middleware<I, S> = (intent: I, next: (intent: I) -> S) -> S

abstract class BaseViewModel<I : Action, S : State>(
    initialState: () -> S
) : ViewModel(), CoroutineScope {
    private val viewModelJob = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + viewModelJob

    private val intentChannel = Channel<I>(Channel.UNLIMITED)

    private val stateMutableLiveData = MutableLiveData<S>()
    val stateLiveData: LiveData<S> = stateMutableLiveData

    val currentState
        get() = stateLiveData.value!!

    private val effectMutableLiveData = MutableLiveData<EffectHolder<Effect>>()
    val effectLiveData: LiveData<EffectHolder<Effect>> = effectMutableLiveData

    private val defaultMiddleware: Middleware<I, S> = { intent, next -> next(intent) }
    private val middlewares: MutableList<Middleware<I, S>> = mutableListOf(defaultMiddleware)

    init {
        stateMutableLiveData.value = initialState()

        launch {
            val reducedMiddleware = middlewares.reduce { p, n ->
                val new: Middleware<I, S> = { intent, next ->
                    n(intent) { nextIntent ->
                        p(nextIntent) { middlewareIntent ->
                            next(middlewareIntent)
                        }
                    }
                }
                new
            }

            intentChannel.consumeEach { intent ->
                reducedMiddleware(intent) { middlewareIntent ->
                    if (middlewareIntent is Effect) {
                        effectMutableLiveData.postValue(EffectHolder(middlewareIntent))
                    }
                    reducer(currentState, middlewareIntent)
                        .also { stateMutableLiveData.value = it }
                }
            }
        }
    }

    protected fun applyMiddleware(middleware: Middleware<I, S>) {
        middlewares.add(middleware)
    }

    abstract fun reducer(state: S, intent: I): S

    fun sendIntent(intent: I) {
        launch {
            intentChannel.send(intent)
        }
    }

    override fun onCleared() {
        viewModelJob.cancel()
        middlewares.clear()
        super.onCleared()
    }
}