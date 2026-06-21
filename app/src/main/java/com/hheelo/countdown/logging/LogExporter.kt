package com.hheelo.countdown.logging

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.hheelo.countdown.R
import java.io.File

/**
 * 把当前日志导出为单个文件，并生成系统分享 Intent，便于用户把日志发给开发者定位问题。
 */
object LogExporter {
    private const val TAG = "LogExporter"

    /**
     * 生成分享日志的 Intent；若没有任何日志内容则返回 null。
     */
    fun shareIntent(context: Context): Intent? {
        val content = AppLog.dump()
        if (content.isBlank()) {
            AppLog.w(TAG, "没有可导出的日志")
            return null
        }

        val exportDir = File(context.filesDir, "logs/export").apply { mkdirs() }
        val exportFile = File(exportDir, "countdown-log.txt")
        return runCatching {
            exportFile.writeText(content)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.log_share_subject))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }.onFailure { AppLog.e(TAG, "导出日志失败", it) }.getOrNull()
    }
}
