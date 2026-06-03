package com.hheelo.countdown

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}

class CountdownWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = CountdownStore(context)
        val snapshot = CountdownCalculator.makeSnapshot(store)
        provideContent {
            val size = LocalSize.current
            if (size.width < 220.dp) {
                val featured = snapshot.cards.minByOrNull { it.days } ?: snapshot.cards.first()
                SmallWidget(featured)
            } else {
                OverviewWidget(snapshot.cards, maxCards = if (size.height > 220.dp) 6 else 3)
            }
        }
    }
}

@Composable
private fun SmallWidget(card: CountdownCard) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(parseWidgetColor("#111827")))
            .padding(16.dp)
            .clickable(actionStartActivity(deepLinkIntent(card.deepLink))),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = card.title,
            style = TextStyle(color = ColorProvider(parseWidgetColor(card.tintHex)), fontSize = 13.sp, fontWeight = FontWeight.Medium),
            maxLines = 1
        )
        Spacer(GlanceModifier.defaultWeight())
        Text(
            text = card.days.toString(),
            style = TextStyle(color = ColorProvider(Color.White), fontSize = 40.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = card.subtitle,
            style = TextStyle(color = ColorProvider(parseWidgetColor("#B8FFFFFF")), fontSize = 11.sp),
            maxLines = 2
        )
    }
}

@Composable
private fun OverviewWidget(cards: List<CountdownCard>, maxCards: Int) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(parseWidgetColor("#111827")))
            .padding(14.dp)
    ) {
        Text(
            text = "倒计时总览",
            style = TextStyle(color = ColorProvider(parseWidgetColor("#EBFFFFFF")), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        )
        Spacer(GlanceModifier.height(10.dp))

        cards.take(maxCards).chunked(3).forEach { rowCards ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                rowCards.forEach { card ->
                    WidgetCard(card, modifier = GlanceModifier.defaultWeight().fillMaxHeight())
                    Spacer(GlanceModifier.width(8.dp))
                }
            }
            Spacer(GlanceModifier.height(8.dp))
        }
    }
}

@Composable
private fun WidgetCard(card: CountdownCard, modifier: GlanceModifier = GlanceModifier) {
    Column(
        modifier = modifier
            .background(ColorProvider(parseWidgetColor("#14FFFFFF")))
            .padding(10.dp)
            .clickable(actionStartActivity(deepLinkIntent(card.deepLink))),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Image(
            provider = ImageProvider(iconResource(card.iconName)),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp),
            colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(parseWidgetColor(card.tintHex)))
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = card.title,
            style = TextStyle(color = ColorProvider(parseWidgetColor("#E6FFFFFF")), fontSize = 12.sp, fontWeight = FontWeight.Medium),
            maxLines = 1
        )
        Text(
            text = card.days.toString(),
            style = TextStyle(color = ColorProvider(Color.White), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = card.subtitle,
            style = TextStyle(color = ColorProvider(parseWidgetColor("#9EFFFFFF")), fontSize = 10.sp),
            maxLines = 2
        )
    }
}

private fun deepLinkIntent(url: String): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        setPackage("com.hheelo.countdown")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
}

private fun iconResource(name: String): Int {
    return when (name) {
        "weekend" -> R.drawable.ic_widget_weekend
        "pin" -> R.drawable.ic_widget_pin
        "holiday" -> R.drawable.ic_widget_holiday
        else -> R.drawable.ic_widget_calendar
    }
}

private fun parseWidgetColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}
