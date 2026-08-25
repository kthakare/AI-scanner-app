package com.kthakare.aiscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_documents ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ScanDocument>>

    @Query("SELECT * FROM scan_documents WHERE id = :id")
    fun observeById(id: String): Flow<ScanDocument?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: ScanDocument)

    @Query("UPDATE scan_documents SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)

    @Query("DELETE FROM scan_documents WHERE id = :id")
    suspend fun deleteById(id: String)
}
