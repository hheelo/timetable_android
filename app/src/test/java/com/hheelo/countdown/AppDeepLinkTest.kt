package com.hheelo.countdown

import android.net.FakeUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppDeepLinkTest {
    @Test
    fun eventUrlNormalizesIdToLowercase() {
        assertEquals("timetable://event/abc-123", AppDeepLink.eventUrl("ABC-123"))
    }

    @Test
    fun eventIdFromReturnsFirstEventPathSegment() {
        val uri = FakeUri(
            fakeScheme = "timetable",
            fakeHost = "event",
            fakePathSegments = listOf("abc-123")
        )

        assertEquals("abc-123", AppDeepLink.eventIdFrom(uri))
    }

    @Test
    fun eventIdFromRejectsUnsupportedUris() {
        assertNull(AppDeepLink.eventIdFrom(null))
        assertNull(
            AppDeepLink.eventIdFrom(
                FakeUri(
                    fakeScheme = "https",
                    fakeHost = "event",
                    fakePathSegments = listOf("abc-123")
                )
            )
        )
        assertNull(
            AppDeepLink.eventIdFrom(
                FakeUri(
                    fakeScheme = "timetable",
                    fakeHost = "home",
                    fakePathSegments = listOf("abc-123")
                )
            )
        )
        assertNull(
            AppDeepLink.eventIdFrom(
                FakeUri(
                    fakeScheme = "timetable",
                    fakeHost = "event"
                )
            )
        )
    }
}
