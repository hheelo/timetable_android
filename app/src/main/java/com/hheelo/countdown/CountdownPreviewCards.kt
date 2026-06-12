package com.hheelo.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "把重要日子放到桌面上",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = countdownColor(CountdownColorHex.TextPrimary)
        )
        Text(
            "默认显示周末、最近节假日和多个自定义目标日，支持桌面小组件与置顶排序。",
            color = countdownColor(CountdownColorHex.TextSecondary),
            fontSize = 14.sp
        )
    }
}

@Composable
internal fun PreviewCards(cards: List<CountdownCard>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.forEach { card ->
            CountdownCardRow(card)
        }
    }
}

@Composable
private fun CountdownCardRow(card: CountdownCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.78f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(card.iconName, countdownColor(card.tintHex))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(card.subtitle, color = countdownColor(CountdownColorHex.TextSecondary), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(card.days.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("天", color = countdownColor(CountdownColorHex.TextSecondary), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun IconBubble(iconName: String, tint: Color) {
    val icon = when (iconName) {
        "weekend" -> Icons.Filled.WbSunny
        "holiday" -> Icons.Filled.Event
        "pin" -> Icons.Filled.PushPin
        else -> Icons.Filled.CalendarMonth
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}
