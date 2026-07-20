package com.hheelo.countdown

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PreviewCardsTest {

    @get:Rule
    val composeRule = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun header_displaysTitle() {
        composeRule.setContent {
            CountdownTheme { Header() }
        }

        composeRule.onNodeWithText("把重要日子放到桌面上").assertIsDisplayed()
    }

    @Test
    fun header_displaysSubtitle() {
        composeRule.setContent {
            CountdownTheme { Header() }
        }

        composeRule.onNodeWithText(
            "默认显示周末、最近节假日和多个自定义目标日，支持桌面小组件与置顶排序。"
        ).assertIsDisplayed()
    }

    @Test
    fun previewCards_displaysWeekendCard() {
        val cards = CountdownCalculator.makeDefaultCards(context)

        composeRule.setContent {
            CountdownTheme { PreviewCards(cards) }
        }

        composeRule.onNodeWithText("周末").assertIsDisplayed()
    }

    @Test
    fun previewCards_displaysHolidayCard() {
        val cards = CountdownCalculator.makeDefaultCards(context)

        composeRule.setContent {
            CountdownTheme { PreviewCards(cards) }
        }

        // The holiday card will show either a holiday name or "节假日" depending on date
        val holidayCard = cards[1]
        composeRule.onNodeWithText(holidayCard.title).assertIsDisplayed()
    }

    @Test
    fun previewCards_displaysCustomEventCard() {
        val now = LocalDate.now()
        val customEvent = CountdownEvent(
            id = "test-id",
            title = "期末考试",
            targetDate = now.plusDays(30).toString()
        )
        val cards = CountdownCalculator.makePreviewSnapshot(context, listOf(customEvent), now).cards

        composeRule.setContent {
            CountdownTheme { PreviewCards(cards) }
        }

        composeRule.onNodeWithText("期末考试").assertIsDisplayed()
    }

    @Test
    fun previewCards_displaysDayCount() {
        val cards = CountdownCalculator.makeDefaultCards(context)

        composeRule.setContent {
            CountdownTheme { PreviewCards(cards) }
        }

        val cardsWithDayUnit = cards.count { it.status != CountdownCardStatus.UNAVAILABLE }
        composeRule
            .onAllNodesWithText("天", useUnmergedTree = true)
            .assertCountEquals(cardsWithDayUnit)
    }
}
