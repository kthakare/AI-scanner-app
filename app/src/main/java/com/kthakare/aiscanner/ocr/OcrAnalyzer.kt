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

    suspend fun recognize(pages: List<File>): String = withContext(Dispatchers.Default) {
        if (pages.isEmpty()) return@withContext ""
        pages.mapIndexed { index, file ->
            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            val text = recognizer.process(image).await().text.trim()
            buildString {
                append("Page ${index + 1}")
                append('\n')
                append(text.ifBlank { "(no text found)" })
            }
        }.joinToString("\n\n")
    }
}
