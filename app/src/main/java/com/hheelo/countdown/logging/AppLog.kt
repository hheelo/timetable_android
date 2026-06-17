package com.hheelo.countdown.logging

import android.content.Context
import android.util.Log

/**
 * 应用统一日志门面。
 *
 * 同时把日志写到 Logcat 以及应用内部存储的日志文件，方便在用户机器上出现问题时
 * 取回日志定位（已安装的发布包通常无法直接抓取 Logcat）。
 *
 * 调用 [init] 之前所有方法依然可用：只会输出到 Logcat，不会写文件，因此在单元测试
 * 或 [init] 之前的极早期阶段调用都是安全的。
 */
object AppLog {
    private const val DEFAULT_TAG = "Countdown"

    @Volatile
    private var fileWriter: FileLogWriter? = null

    /** 在 Application 启动时调用一次，开启文件日志。 */
    fun init(context: Context) {
        if (fileWriter != null) return
        synchronized(this) {
            if (fileWriter != null) return
            fileWriter = runCatching { FileLogWriter(context.applicationContext) }
                .onFailure { Log.e(DEFAULT_TAG, "初始化文件日志失败", it) }
                .getOrNull()
        }
        i(DEFAULT_TAG, "AppLog 已初始化，日志目录: ${fileWriter?.logDirectoryPath() ?: "(仅 Logcat)"}")
    }

    fun v(tag: String, message: String, throwable: Throwable? = null) = log(Level.VERBOSE, tag, message, throwable)
    fun d(tag: String, message: String, throwable: Throwable? = null) = log(Level.DEBUG, tag, message, throwable)
    fun i(tag: String, message: String, throwable: Throwable? = null) = log(Level.INFO, tag, message, throwable)
    fun w(tag: String, message: String, throwable: Throwable? = null) = log(Level.WARN, tag, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log(Level.ERROR, tag, message, throwable)

    private fun log(level: Level, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            Level.VERBOSE -> Log.v(tag, message, throwable)
            Level.DEBUG -> Log.d(tag, message, throwable)
            Level.INFO -> Log.i(tag, message, throwable)
            Level.WARN -> Log.w(tag, message, throwable)
            Level.ERROR -> Log.e(tag, message, throwable)
        }

        val writer = fileWriter ?: return
        val line = buildString {
            append(level.label).append('/').append(tag).append(": ").append(message)
            if (throwable != null) {
                append('\n').append(Log.getStackTraceString(throwable).trimEnd())
            }
        }
        writer.append(line)
    }

    /** 阻塞等待已入队日志全部落盘（崩溃处理时调用，避免日志丢失）。 */
    fun flush(timeoutMs: Long = 1000) {
        fileWriter?.flushBlocking(timeoutMs)
    }

    /** 读取当前已落盘的全部日志文本，可用于在应用内展示或分享给开发者。 */
    fun dump(): String = fileWriter?.readAll() ?: "(文件日志未启用)"

    /** 清空已落盘的日志文件。 */
    fun clear() {
        fileWriter?.clear()
        i(DEFAULT_TAG, "日志已被清空")
    }

    private enum class Level(val label: Char) {
        VERBOSE('V'),
        DEBUG('D'),
        INFO('I'),
        WARN('W'),
        ERROR('E')
    }
}
