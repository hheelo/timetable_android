package com.hheelo.countdown.logging

/**
 * 捕获未处理的异常并写入日志，随后交回系统默认处理器（保持原有崩溃行为）。
 *
 * 让崩溃信息和堆栈持久化到日志文件，便于事后定位导致闪退的原因。
 */
internal class CrashHandler private constructor(
    private val delegate: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            AppLog.e(TAG, "未捕获异常，线程: ${thread.name}", throwable)
            AppLog.flush()
        }
        delegate?.uncaughtException(thread, throwable)
    }

    companion object {
        private const val TAG = "CrashHandler"

        fun install() {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            if (current is CrashHandler) return
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(current))
        }
    }
}
