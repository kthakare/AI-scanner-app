package com.kthakare.aiscanner.scan;

import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;

/**
 * Builds ML Kit scanner options in Java. Kotlin 2.0 (K2) cannot resolve
 * {@link GmsDocumentScannerOptions.Builder#build()} after the varargs
 * {@code setResultFormats} chain.
 */
final class DocumentScannerClients {
    private DocumentScannerClients() {}

    static GmsDocumentScanner create() {
        GmsDocumentScannerOptions options =
                new GmsDocumentScannerOptions.Builder()
                        .setGalleryImportAllowed(true)
                        .setPageLimit(10)
                        .setResultFormats(
                                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                                GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                        .build();
        return GmsDocumentScanning.getClient(options);
    }
}
