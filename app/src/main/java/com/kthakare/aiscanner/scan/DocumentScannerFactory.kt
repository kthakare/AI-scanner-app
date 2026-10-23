package com.kthakare.aiscanner.scan

import android.app.Activity
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner

object DocumentScannerFactory {
    fun create(): GmsDocumentScanner = DocumentScannerClients.create()

    fun startScan(activity: Activity, scanner: GmsDocumentScanner = create()) =
        scanner.getStartScanIntent(activity)
}
