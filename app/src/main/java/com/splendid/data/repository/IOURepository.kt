package com.splendid.data.repository

import com.splendid.data.local.dao.IOUDao
import com.splendid.data.local.entity.IOUEntity
import com.splendid.domain.model.IOU
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IOURepository(private val iouDao: IOUDao) {
    
    fun getPendingIOUs(): Flow<List<IOU>> {
        return iouDao.getPendingIOUs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getSettledIOUs(): Flow<List<IOU>> {
        return iouDao.getSettledIOUs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun addIOU(iou: IOU): Long {
        return iouDao.insert(iou.toEntity())
    }
    
    suspend fun updateIOU(iou: IOU) {
        iouDao.update(iou.toEntity())
    }
    
    suspend fun deleteIOU(iou: IOU) {
        iouDao.delete(iou.toEntity())
    }
    
    suspend fun settleIOU(id: Long, settledDate: Long) {
        iouDao.settleIOU(id, settledDate)
    }
    
    suspend fun getNetBalance(): Double {
        return iouDao.getNetBalance() ?: 0.0
    }
    
    private fun IOUEntity.toDomainModel() = IOU(
        id = id,
        title = title,
        amount = amount,
        type = type,
        isSettled = isSettled,
        date = date,
        settledDate = settledDate
    )
    
    private fun IOU.toEntity() = IOUEntity(
        id = id,
        title = title,
        amount = amount,
        type = type,
        isSettled = isSettled,
        date = date,
        settledDate = settledDate
    )
}
