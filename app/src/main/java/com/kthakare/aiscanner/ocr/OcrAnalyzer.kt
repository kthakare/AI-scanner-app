package com.kthakare.aiscanner.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OcrAnalyzer(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(pages: List<File>): OcrExtraction = withContext(Dispatchers.Default) {
        if (pages.isEmpty()) {
            return@withContext OcrExtraction(fields = emptyList(), rawText = "")
        }
        val layouts = pages.mapIndexed { index, file ->
            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            val visionText = recognizer.process(image).await()
            val pageNumber = index + 1
            val pageText = linesFrom(visionText).ifBlank { visionText.text.trim() }
            InvoiceLayoutParser.parse(pageText, pageNumber) to (pageNumber to pageText)
        }
        val pageLayouts = layouts.map { it.first }
        val pageTexts = layouts.map { it.second }
        val rawText = pageTexts.joinToString("\n\n") { (pageNumber, text) ->
            "Page $pageNumber\n${text.ifBlank { "(no text found)" }}"
        }
        OcrExtraction(
            fields = pageLayouts.flatMap { it.fields },
            rawText = rawText,
            headerFields = pageLayouts.flatMap { it.headerFields },
            tables = pageLayouts.mapNotNull { it.table }.filter { it.rows.isNotEmpty() },
            footerFields = pageLayouts.flatMap { it.footerFields },
        )
    }

    private fun linesFrom(visionText: Text): String =
        visionText.textBlocks
            .flatMap { it.lines }
            .map { it.text.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
}
