package com.hheelo.countdown

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hheelo.countdown.logging.AppLog
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action !in widgetRefreshActions) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppLog.i(TAG, "收到广播 ${intent.action}，开始刷新小组件")
                glanceAppWidget.updateAll(appContext)
            } catch (throwable: Throwable) {
                AppLog.w(TAG, "刷新小组件失败，action=${intent.action}", throwable)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "CountdownWidgetReceiver"

        val widgetRefreshActions = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}

class CountdownWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(160.dp, 110.dp),
            DpSize(220.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(360.dp, 110.dp),
            DpSize(480.dp, 110.dp),
            DpSize(640.dp, 110.dp),
            DpSize(220.dp, 220.dp),
            DpSize(300.dp, 220.dp),
            DpSize(360.dp, 260.dp),
            DpSize(480.dp, 260.dp),
            DpSize(640.dp, 260.dp),
            DpSize(220.dp, 340.dp),
            DpSize(300.dp, 340.dp),
            DpSize(360.dp, 360.dp),
            DpSize(480.dp, 360.dp),
            DpSize(640.dp, 360.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = CountdownStore(context)
        val snapshot = CountdownCalculator.makeWidgetSnapshot(context, store)
        AppLog.d(TAG, "渲染小组件，卡片数=${snapshot.cards.size}")
        provideContent {
            val size = LocalSize.current
            val maxCards = visibleCardCount(size)
            val widgetCards = snapshot.cards.sortedForWidget()
            if (maxCards == 1) {
                val featured = widgetCards.firstOrNull() ?: snapshot.cards.first()
                SmallWidget(featured, size)
            } else {
                OverviewWidget(widgetCards, maxCards, size)
            }
        }
    }

    private companion object {
        const val TAG = "CountdownWidget"
    }
}

@Composable
private fun SmallWidget(card: CountdownCard, size: DpSize) {
    val compact = size.height < 140.dp
    val padding = if (compact) 12.dp else 16.dp
    val topSpacer = when {
        size.height < 130.dp -> 10.dp
        size.height < 180.dp -> 20.dp
        else -> 32.dp
    }
    val context = LocalContext.current
    val accessibilityText = if (card.days == 0L) {
        context.getString(R.string.accessibility_today, card.title)
    } else {
        context.getString(R.string.accessibility_days_remaining, card.title, card.days)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(countdownColor(CountdownColorHex.WidgetBackground)))
            .padding(padding)
            .clickable(actionStartActivity(deepLinkIntent(card.deepLink)))
            .semantics {
                contentDescription = accessibilityText
            },
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = card.title,
            style = TextStyle(
                color = ColorProvider(countdownColor(card.tintHex)),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Spacer(GlanceModifier.height(topSpacer))
        Text(
            text = card.days.toString(),
            style = TextStyle(color = ColorProvider(Color.White), fontSize = if (compact) 34.sp else 40.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = card.subtitle,
            style = TextStyle(
                color = ColorProvider(countdownColor(CountdownColorHex.WidgetSmallSubtitle)),
                fontSize = 11.sp
            ),
            maxLines = if (compact) 1 else 2
        )
    }
}

@Composable
private fun OverviewWidget(cards: List<CountdownCard>, maxCards: Int, size: DpSize) {
    val visibleCards = cards.take(maxCards)
    val columns = columnsFor(size.width).coerceAtMost(maxCards)
    val rows = rowCount(visibleCards.size, columns)
    val padding = overviewPadding(size)
    val cardGap = 8.dp
    val contentWidth = size.width.value - padding.value * 2
    val contentHeight = size.height.value - padding.value * 2
    val cardWidth = ((contentWidth - cardGap.value * (columns - 1)) / columns).coerceAtLeast(74f).dp
    val cardHeight = ((contentHeight - cardGap.value * (rows - 1)) / rows).coerceAtLeast(80f).dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(countdownColor(CountdownColorHex.WidgetBackground)))
            .padding(padding)
    ) {
        visibleCards.chunked(columns).forEachIndexed { rowIndex, rowCards ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                rowCards.forEachIndexed { index, card ->
                    WidgetCard(
                        card = card,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        modifier = GlanceModifier.width(cardWidth).height(cardHeight)
                    )
                    if (index < rowCards.lastIndex) {
                        Spacer(GlanceModifier.width(cardGap))
                    }
                }
            }
            if (rowIndex < rows - 1) {
                Spacer(GlanceModifier.height(cardGap))
            }
        }
    }
}

@Composable
private fun WidgetCard(
    card: CountdownCard,
    cardWidth: Dp,
    cardHeight: Dp,
    modifier: GlanceModifier = GlanceModifier
) {
    val compact = cardHeight < 104.dp
    val spacious = cardHeight >= 124.dp
    val titleLines = if (cardWidth >= 108.dp || spacious) 2 else 1
    val subtitleLines = when {
        cardHeight >= 122.dp -> 2
        cardHeight >= 104.dp -> 1
        else -> 0
    }
    val context = LocalContext.current
    val accessibilityText = if (card.days == 0L) {
        context.getString(R.string.accessibility_today, card.title)
    } else {
        context.getString(R.string.accessibility_days_remaining, card.title, card.days)
    }

    Column(
        modifier = modifier
            .background(ColorProvider(countdownColor(CountdownColorHex.WidgetCardBackground)))
            .padding(if (compact) 8.dp else 10.dp)
            .clickable(actionStartActivity(deepLinkIntent(card.deepLink)))
            .semantics {
                contentDescription = accessibilityText
            },
        verticalAlignment = Alignment.Vertical.Top
    ) {
        if (!compact) {
            Image(
                provider = ImageProvider(iconResource(card.iconName)),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp),
                colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(countdownColor(card.tintHex)))
            )
            Spacer(GlanceModifier.height(6.dp))
        }
        Text(
            text = card.title,
            style = TextStyle(
                color = ColorProvider(countdownColor(CountdownColorHex.WidgetTitle)),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = titleLines
        )
        Text(
            text = card.days.toString(),
            style = TextStyle(color = ColorProvider(Color.White), fontSize = if (compact) 24.sp else 28.sp, fontWeight = FontWeight.Bold)
        )
        if (subtitleLines > 0) {
            Text(
                text = card.subtitle,
                style = TextStyle(
                    color = ColorProvider(countdownColor(CountdownColorHex.WidgetSubtitle)),
                    fontSize = 10.sp
                ),
                maxLines = subtitleLines
            )
        }
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
    if (size.width < 200.dp) return 1

    val columns = columnsFor(size.width)
    val padding = overviewPadding(size)
    val cardGap = 8.dp
    val availableHeight = size.height.value - padding.value * 2
    val minimumCardHeight = if (size.height < 180.dp) 82f else 112f
    val rowsThatFit = ((availableHeight + cardGap.value) / (minimumCardHeight + cardGap.value)).toInt()
    val maxRows = ((6 + columns - 1) / columns).coerceAtLeast(1)
    val rows = rowsThatFit.coerceIn(1, maxRows)
    return (columns * rows).coerceIn(2, 6)
}

private fun columnsFor(width: Dp): Int {
    return when {
        width < 280.dp -> 2
        width < 560.dp -> 3
        else -> 4
    }
}

private fun rowCount(itemCount: Int, columns: Int): Int {
    return (itemCount + columns - 1) / columns
}

private fun overviewPadding(size: DpSize): Dp {
    return if (size.height < 180.dp || size.width < 260.dp) 12.dp else 14.dp
}

internal fun List<CountdownCard>.sortedForWidget(): List<CountdownCard> {
    return sortedWith(
        compareBy<CountdownCard> { it.days }
            .thenBy { it.widgetDisplayPriority() }
            .thenBy { it.title }
    )
}

private fun CountdownCard.widgetDisplayPriority(): Int {
    return when {
        isPinnedCustomCard() -> 0
        isCustomCard() -> 1
        else -> 2
    }
}

private fun CountdownCard.isPinnedCustomCard(): Boolean {
    return isCustomCard() && isPinned
}

private fun CountdownCard.isCustomCard(): Boolean {
    return eventId != null
}
