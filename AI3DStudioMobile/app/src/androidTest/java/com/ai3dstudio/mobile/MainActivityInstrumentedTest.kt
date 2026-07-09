package com.ai3dstudio.mobile

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun setupScreenShowsBaseUrlField() {
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.setup_base_url_label)
        ).assertExists()
    }
}
