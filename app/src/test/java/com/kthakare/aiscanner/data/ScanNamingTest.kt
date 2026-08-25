package com.kthakare.aiscanner.data

import java.io.File
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanNamingTest {
    @Test
    fun defaultTitleUsesUtcTimestamp() {
        val millis = 1_777_075_200_000L // 2026-04-25 12:00:00 UTC
        val title = ScanNaming.defaultTitle(
            epochMillis = millis,
            locale = Locale.US,
            timeZone = TimeZone.getTimeZone("UTC"),
        )
        assertEquals("Scan Apr 25, 2026 12:00 PM", title)
    }

    @Test
    fun sanitizeTitleTrimsAndFallsBack() {
        assertEquals("Receipt", ScanNaming.sanitizeTitle("  Receipt  "))
        assertEquals("Untitled scan", ScanNaming.sanitizeTitle("   "))
    }
}

class ScanPathsTest {
    @Test
    fun pageFilesMatchPageCount() {
        val filesDir = File("/tmp/ai-scanner-files")
        val document = ScanDocument(
            id = "abc",
            title = "Test",
            createdAt = 0L,
            pageCount = 3,
            pdfPath = "/tmp/ai-scanner-files/scans/abc/document.pdf",
            thumbnailPath = "/tmp/ai-scanner-files/scans/abc/page_0.jpg",
        )
        val pages = ScanPaths.pageFiles(filesDir, document)
        assertEquals(3, pages.size)
        assertEquals(File(filesDir, "scans/abc/page_1.jpg"), pages[1])
        assertTrue(ScanPaths.pdfFile(ScanPaths.scanDirectory(filesDir, "abc")).path.endsWith("document.pdf"))
    }
}
