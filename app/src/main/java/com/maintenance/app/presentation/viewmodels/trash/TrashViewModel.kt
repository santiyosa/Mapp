package com.maintenance.app.presentation.viewmodels.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.records.GetDeletedRecordsUseCase
import com.maintenance.app.domain.usecases.records.PermanentlyDeleteRecordUseCase
import com.maintenance.app.domain.usecases.records.RestoreRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.OptIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * ViewModel for trash/deleted records management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getDeletedRecordsUseCase: GetDeletedRecordsUseCase,
    private val restoreRecordUseCase: RestoreRecordUseCase,
    private val permanentlyDeleteRecordUseCase: PermanentlyDeleteRecordUseCase
) : ViewModel() {

    val deletedRecords: StateFlow<List<Record>> = flowOf(Unit)
        .flatMapLatest {
            getDeletedRecordsUseCase()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Restore a deleted record.
     */
    fun restoreRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                restoreRecordUseCase(RestoreRecordUseCase.Params(recordId))
            } catch (exception: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Permanently delete a record.
     */
    fun permanentlyDeleteRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                permanentlyDeleteRecordUseCase(PermanentlyDeleteRecordUseCase.Params(recordId))
            } catch (exception: Exception) {
                // Handle error
            }
        }
    }
}
