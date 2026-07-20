package com.hheelo.countdown

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownWidgetTest {
    @Test
    fun sortedForWidgetShowsNearestDateBeforeCustomPriority() {
        val farCustom = card(
            title = "远期自定义",
            days = 30,
            eventId = "far-custom"
        )
        val nearDefault = card(
            title = "周末",
            days = 2,
            eventId = null
        )
        val midPinnedCustom = card(
            title = "置顶自定义",
            days = 7,
            eventId = "pinned-custom",
            isPinned = true
        )

        val sorted = listOf(farCustom, midPinnedCustom, nearDefault).sortedForWidget()

        assertEquals(listOf("周末", "置顶自定义", "远期自定义"), sorted.map { it.title })
    }

    @Test
    fun sortedForWidgetUsesPinnedAndCustomPriorityOnlyForSameDate() {
        val default = card(title = "周末", days = 3, eventId = null)
        val custom = card(title = "自定义", days = 3, eventId = "custom")
        val pinned = card(title = "置顶自定义", days = 3, eventId = "pinned", isPinned = true)

        val sorted = listOf(default, custom, pinned).sortedForWidget()

        assertEquals(listOf("置顶自定义", "自定义", "周末"), sorted.map { it.title })
    }

    @Test
    fun sortedForWidgetPlacesUnavailableCardsLast() {
        val unavailable = card(title = "暂无节假日", days = 0, eventId = null).copy(
            status = CountdownCardStatus.UNAVAILABLE
        )
        val future = card(title = "未来事件", days = 30, eventId = "future")

        val sorted = listOf(unavailable, future).sortedForWidget()

        assertEquals(listOf("未来事件", "暂无节假日"), sorted.map { it.title })
    }

    private fun card(
        title: String,
        days: Long,
        eventId: String?,
        isPinned: Boolean = false
    ): CountdownCard {
        return CountdownCard(
            title = title,
            subtitle = "",
            days = days,
            iconName = "",
            tintHex = CountdownColorHex.Brand,
            deepLink = AppDeepLink.HomeUrl,
            eventId = eventId,
            isPinned = isPinned
        )
    }
}
