package com.hheelo.countdown

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hheelo.countdown.logging.LogExporter

@Composable
internal fun SavePanel(savedMessage: String?, tintHex: String, onSave: () -> Unit) {
    val extraColors = LocalCountdownColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = countdownColor(tintHex)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("保存并刷新小组件")
        }
        savedMessage?.let { Text(it, color = extraColors.success, fontSize = 13.sp) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Footer() {
    val context = LocalContext.current
    val extraColors = LocalCountdownColors.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        Text("节假日说明", fontWeight = FontWeight.SemiBold, color = extraColors.textPrimary)
        Text(
            "当前内置了 2026 年大陆法定节假日数据；自定义倒计时支持多条配置。后续年份可在 HolidayCalendar.kt 中继续追加。",
            color = extraColors.textSecondary,
            fontSize = 13.sp
        )
        versionName?.let {
            Text(
                "版本 v$it（长按导出日志）",
                color = extraColors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        val intent = LogExporter.shareIntent(context)
                        if (intent != null) {
                            context.startActivity(Intent.createChooser(intent, "分享日志"))
                        } else {
                            Toast.makeText(context, "暂无日志可导出", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )
        }
    }
}
