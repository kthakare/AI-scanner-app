package com.kthakare.aiscanner

import android.content.Context
import com.kthakare.aiscanner.data.ScanDatabase
import com.kthakare.aiscanner.data.ScanRepository
import com.kthakare.aiscanner.ocr.OcrAnalyzer

class AppContainer(context: Context) {
    private val database = ScanDatabase.create(context)
    val repository = ScanRepository(
        context = context.applicationContext,
        dao = database.scanDao(),
    )
    val ocrAnalyzer = OcrAnalyzer(context.applicationContext)
}
