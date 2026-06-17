package com.hheelo.countdown.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 把日志行异步追加到内部存储文件，带大小轮转。
 *
 * - 所有写入都在单个后台线程上串行执行，不阻塞调用方（UI）线程。
 * - 当前文件超过 [MAX_FILE_BYTES] 时轮转为 `app.log.1`，仅保留最近两份。
 */
internal class FileLogWriter(context: Context) {
    private val logDir = File(context.filesDir, "logs").apply { mkdirs() }
    private val currentFile = File(logDir, "app.log")
    private val rotatedFile = File(logDir, "app.log.1")
    private val writeExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "AppLogWriter").apply { isDaemon = true }
    }
    private val timestampFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    fun logDirectoryPath(): String = logDir.absolutePath

    fun append(line: String) {
        val timestamp = timestampFormat.format(Date())
        writeExecutor.execute {
            runCatching {
                rotateIfNeeded()
                currentFile.appendText("$timestamp $line\n")
            }.onFailure { Log.e("AppLog", "写入日志文件失败", it) }
        }
    }

    /**
     * 阻塞等待已入队的写入全部落盘，最多等待 [timeoutMs]。
     * 用于崩溃处理等需要确保日志不丢失的场景。
     */
    fun flushBlocking(timeoutMs: Long) {
        runCatching {
            writeExecutor.submit { }.get(timeoutMs, TimeUnit.MILLISECONDS)
        }
    }

    fun readAll(): String {
        val previous = rotatedFile.takeIf { it.exists() }?.readText().orEmpty()
        val current = currentFile.takeIf { it.exists() }?.readText().orEmpty()
        return previous + current
    }

    fun clear() {
        writeExecutor.execute {
            runCatching {
                currentFile.delete()
                rotatedFile.delete()
            }
        }
    }

    private fun rotateIfNeeded() {
        if (currentFile.length() < MAX_FILE_BYTES) return
        rotatedFile.delete()
        currentFile.renameTo(rotatedFile)
    }

    private companion object {
        const val MAX_FILE_BYTES = 256L * 1024
    }
}
