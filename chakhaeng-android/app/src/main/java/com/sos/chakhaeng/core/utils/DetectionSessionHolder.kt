package com.sos.chakhaeng.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionSessionHolder @Inject constructor(

){
    private val _startInstant = MutableStateFlow<Instant?>(null)
    val startInstant: StateFlow<Instant?> = _startInstant

    fun start(now: Instant = Instant.now()) { _startInstant.value = now }
    fun clear() { _startInstant.value = null }
}