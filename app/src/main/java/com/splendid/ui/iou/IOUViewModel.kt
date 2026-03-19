package com.splendid.ui.iou

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.splendid.data.local.database.SplendidDatabase
import com.splendid.domain.model.IOUType
import com.splendid.data.repository.IOURepository
import com.splendid.domain.model.IOU
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IOUUiState(
    val pendingIOUs: List<IOU> = emptyList(),
    val settledIOUs: List<IOU> = emptyList(),
    val netBalance: Double = 0.0,
    val isLoading: Boolean = false
)

class IOUViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SplendidDatabase.getDatabase(application)
    private val iouRepository = IOURepository(database.iouDao())
    
    private val _uiState = MutableStateFlow(IOUUiState())
    val uiState: StateFlow<IOUUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                iouRepository.getPendingIOUs(),
                iouRepository.getSettledIOUs()
            ) { pending, settled ->
                IOUUiState(
                    pendingIOUs = pending,
                    settledIOUs = settled,
                    netBalance = calculateNetBalance(pending),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    private fun calculateNetBalance(pendingIOUs: List<IOU>): Double {
        return pendingIOUs.sumOf { iou ->
            when (iou.type) {
                IOUType.WILL_RECEIVE -> iou.amount  // Money coming to you
                IOUType.I_OWE -> -iou.amount      // Money you owe
            }
        }
    }
    
    fun addIOU(iou: IOU) {
        viewModelScope.launch {
            iouRepository.addIOU(iou)
        }
    }
    
    fun settleIOU(iou: IOU) {
        viewModelScope.launch {
            iouRepository.settleIOU(iou.id, System.currentTimeMillis())
        }
    }
    
    fun deleteIOU(iou: IOU) {
        viewModelScope.launch {
            iouRepository.deleteIOU(iou)
        }
    }
}
