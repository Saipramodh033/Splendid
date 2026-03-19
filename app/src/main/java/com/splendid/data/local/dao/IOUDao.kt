package com.splendid.data.local.dao

import androidx.room.*
import com.splendid.data.local.entity.IOUEntity
import com.splendid.domain.model.IOUType
import kotlinx.coroutines.flow.Flow

@Dao
interface IOUDao {
    
    @Insert
    suspend fun insert(iou: IOUEntity): Long
    
    @Update
    suspend fun update(iou: IOUEntity)
    
    @Delete
    suspend fun delete(iou: IOUEntity)
    
    @Query("SELECT * FROM iou WHERE isSettled = 0 ORDER BY date DESC")
    fun getPendingIOUs(): Flow<List<IOUEntity>>
    
    @Query("SELECT * FROM iou WHERE isSettled = 1 ORDER BY settledDate DESC")
    fun getSettledIOUs(): Flow<List<IOUEntity>>
    
    @Query("""
        SELECT 
            SUM(CASE 
                WHEN type = 'WILL_RECEIVE' THEN amount 
                WHEN type = 'I_OWE' THEN -amount 
                ELSE 0 
            END) as netBalance
        FROM iou 
        WHERE isSettled = 0
    """)
    suspend fun getNetBalance(): Double?
    
    @Query("UPDATE iou SET isSettled = 1, settledDate = :settledDate WHERE id = :id")
    suspend fun settleIOU(id: Long, settledDate: Long)
}
