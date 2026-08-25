package com.kthakare.aiscanner.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ScanNaming {
    fun defaultTitle(
        epochMillis: Long,
        locale: Locale = Locale.US,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String {
        val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", locale)
        formatter.timeZone = timeZone
        return "Scan ${formatter.format(Date(epochMillis))}"
    }

    fun sanitizeTitle(raw: String): String = raw.trim().ifBlank { "Untitled scan" }
}

object ScanPaths {
    fun scanDirectory(filesDir: File, scanId: String): File = File(filesDir, "scans/$scanId")

    fun pageFile(scanDirectory: File, index: Int): File = File(scanDirectory, "page_$index.jpg")

    fun pdfFile(scanDirectory: File): File = File(scanDirectory, "document.pdf")

    fun pageFiles(filesDir: File, document: ScanDocument): List<File> {
        val directory = scanDirectory(filesDir, document.id)
        return (0 until document.pageCount).map { pageFile(directory, it) }
    }
}
