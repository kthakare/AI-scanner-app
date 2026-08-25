package com.kthakare.aiscanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_documents")
data class ScanDocument(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val pageCount: Int,
    val pdfPath: String,
    val thumbnailPath: String,
)
