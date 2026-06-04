package com.hheelo.countdown

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(160.dp, 110.dp),
            DpSize(220.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(220.dp, 220.dp),
            DpSize(300.dp, 220.dp),
            DpSize(360.dp, 260.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = CountdownStore(context)
        val snapshot = CountdownCalculator.makeSnapshot(store)
        provideContent {
            val size = LocalSize.current
            val maxCards = visibleCardCount(size)
            if (maxCards == 1) {
                val featured = snapshot.cards.minByOrNull { it.days } ?: snapshot.cards.first()
                SmallWidget(featured)
            } else {
                OverviewWidget(snapshot.cards, maxCards)
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
        Spacer(GlanceModifier.height(32.dp))
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
    val columns = columnsFor(maxCards)
    val cardHeight = if (maxCards <= 3) 118.dp else 104.dp
    val cardWidth = when (columns) {
        2 -> 96.dp
        else -> 88.dp
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(parseWidgetColor("#111827")))
            .padding(if (maxCards <= 2) 12.dp else 14.dp)
    ) {
        if (maxCards > 2) {
            Text(
                text = "倒计时总览",
                style = TextStyle(color = ColorProvider(parseWidgetColor("#EBFFFFFF")), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(GlanceModifier.height(10.dp))
        }

        cards.take(maxCards).chunked(columns).forEach { rowCards ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                rowCards.forEach { card ->
                    WidgetCard(card, modifier = GlanceModifier.width(cardWidth).height(cardHeight))
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

private fun visibleCardCount(size: DpSize): Int {
    return when {
        size.width < 200.dp -> 1
        size.height < 180.dp && size.width < 280.dp -> 2
        size.height < 180.dp -> 3
        size.width < 280.dp -> 4
        else -> 6
    }
}

private fun columnsFor(maxCards: Int): Int {
    return when {
        maxCards <= 2 -> 2
        maxCards == 4 -> 2
        else -> 3
    }
}

private fun parseWidgetColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}
