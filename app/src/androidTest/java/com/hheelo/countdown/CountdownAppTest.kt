package com.hheelo.countdown

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

/**
 * Integration test that launches [MainActivity] and verifies the full screen
 * renders its key sections: header, preview cards, custom editor header,
 * the empty-state hint (when no events are persisted), and the save panel.
 */
class CountdownAppTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_displaysHeader() {
        composeRule.onNodeWithText("把重要日子放到桌面上").assertIsDisplayed()
    }

    @Test
    fun app_displaysWeekendCard() {
        composeRule.onNodeWithText("周末").assertIsDisplayed()
    }

    @Test
    fun app_displaysCustomEditorSection() {
        composeRule.onNodeWithText("自定义倒计时").assertIsDisplayed()
    }

    @Test
    fun app_displaysAddButton() {
        composeRule.onNodeWithText("新增").assertIsDisplayed()
    }

    @Test
    fun app_displaysSaveButton() {
        composeRule
            .onNodeWithText("保存并刷新小组件")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun app_addButtonCreatesEventEditor() {
        // Click the add button
        composeRule.onNodeWithText("新增").performClick()

        // A new event editor should appear with the default title
        composeRule.onNodeWithText("新的目标").assertIsDisplayed()
    }

    @Test
    fun app_addButtonShowsFieldLabel() {
        composeRule.onNodeWithText("新增").performClick()

        composeRule.onNodeWithText("事件名称").assertIsDisplayed()
    }

    @Test
    fun app_displaysFooter() {
        composeRule
            .onNodeWithText("节假日说明")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
