package com.kthakare.aiscanner.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScanRepository(
    private val context: Context,
    private val dao: ScanDao,
    private val filesDir: File = context.filesDir,
) {
    fun observeAll(): Flow<List<ScanDocument>> = dao.observeAll()

    fun observeById(id: String): Flow<ScanDocument?> = dao.observeById(id)

    suspend fun importScan(pageUris: List<Uri>, pdfUri: Uri?): ScanDocument = withContext(Dispatchers.IO) {
        require(pageUris.isNotEmpty()) { "A scan must include at least one page." }

        val id = UUID.randomUUID().toString()
        val directory = ScanPaths.scanDirectory(filesDir, id).apply { mkdirs() }

        pageUris.forEachIndexed { index, uri ->
            copyUri(uri, ScanPaths.pageFile(directory, index))
        }

        val pdfFile = ScanPaths.pdfFile(directory)
        if (pdfUri != null) {
            copyUri(pdfUri, pdfFile)
        }

        val createdAt = System.currentTimeMillis()
        val document = ScanDocument(
            id = id,
            title = ScanNaming.defaultTitle(createdAt, timeZone = TimeZone.getDefault()),
            createdAt = createdAt,
            pageCount = pageUris.size,
            pdfPath = pdfFile.absolutePath,
            thumbnailPath = ScanPaths.pageFile(directory, 0).absolutePath,
        )
        dao.insert(document)
        document
    }

    suspend fun rename(id: String, title: String) {
        dao.updateTitle(id, ScanNaming.sanitizeTitle(title))
    }

    suspend fun delete(document: ScanDocument) = withContext(Dispatchers.IO) {
        ScanPaths.scanDirectory(filesDir, document.id).deleteRecursively()
        dao.deleteById(document.id)
    }

    fun pageFiles(document: ScanDocument): List<File> = ScanPaths.pageFiles(filesDir, document)

    private fun copyUri(uri: Uri, destination: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to read scanned file: $uri")
    }
}
