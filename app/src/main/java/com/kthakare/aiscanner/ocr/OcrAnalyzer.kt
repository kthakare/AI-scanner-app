package com.kthakare.aiscanner.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
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
        val pageTexts = pages.mapIndexed { index, file ->
            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            val text = recognizer.process(image).await().text.trim()
            index + 1 to text
        }
        val fields = pageTexts.flatMap { (pageNumber, text) ->
            OcrKeyValueParser.parse(text, pageNumber)
        }
        val rawText = pageTexts.joinToString("\n\n") { (pageNumber, text) ->
            "Page $pageNumber\n${text.ifBlank { "(no text found)" }}"
        }
        OcrExtraction(fields = fields, rawText = rawText)
    }
}
