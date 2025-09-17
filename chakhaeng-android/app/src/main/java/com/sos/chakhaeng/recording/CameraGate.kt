package com.sos.chakhaeng.recording

import java.util.concurrent.atomic.AtomicReference


/**
 * 프로세스 내 카메라 단일 소유 보장.
 * ownerTag: "ui" | "service" 등 호출자 식별자
 */
object CameraGate {
    private val ownerRef = AtomicReference<String?>(null)

    @JvmStatic
    fun acquire(ownerTag: String): Boolean = ownerRef.compareAndSet(null, ownerTag)

    @JvmStatic
    fun release(ownerTag: String) {
        ownerRef.compareAndSet(ownerTag, null)
    }

    @JvmStatic
    fun currentOwner(): String? = ownerRef.get()
}