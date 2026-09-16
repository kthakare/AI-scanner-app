package com.kthakare.aiscanner.scan

import android.app.Activity
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning

object DocumentScannerFactory {
    fun create(): GmsDocumentScanner {
        val builder = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        builder.setResultFormats(
            GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
            GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
        )
        return GmsDocumentScanning.getClient(builder.build())
    }

    fun startScan(activity: Activity, scanner: GmsDocumentScanner = create()) =
        scanner.getStartScanIntent(activity)
}
