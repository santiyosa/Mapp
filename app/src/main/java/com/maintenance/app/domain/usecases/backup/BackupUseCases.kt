package com.maintenance.app.domain.usecases.backup

import com.maintenance.app.domain.model.BackupConfig
import com.maintenance.app.domain.model.BackupMetadata
import com.maintenance.app.domain.model.BackupSchedule
import com.maintenance.app.domain.repositories.BackupRepository
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.Result

/**
 * Use case for creating a backup.
 */
class CreateBackupUseCase(
    private val backupRepository: BackupRepository
) : UseCase<CreateBackupUseCase.Params, BackupConfig>() {

    data class Params(
        val backupName: String,
        val encryptionEnabled: Boolean = false
    )

    override suspend fun execute(params: Params): BackupConfig {
        val result = backupRepository.createBackup(
            backupName = params.backupName,
            encryptionEnabled = params.encryptionEnabled
        )
        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for restoring a backup.
 */
class RestoreBackupUseCase(
    private val backupRepository: BackupRepository
) : UseCase<RestoreBackupUseCase.Params, Unit>() {

    data class Params(val backupId: String)

    override suspend fun execute(params: Params): Unit {
        val result = backupRepository.restoreBackup(params.backupId)
        when (result) {
            is Result.Success -> {}
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for getting the list of backups.
 */
class GetBackupListUseCase(
    private val backupRepository: BackupRepository
) : UseCase<GetBackupListUseCase.Params, List<BackupMetadata>>() {

    data class Params(val dummy: Boolean = true)

    override suspend fun execute(params: Params): List<BackupMetadata> {
        val result = backupRepository.getBackupList()
        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for deleting a backup.
 */
class DeleteBackupUseCase(
    private val backupRepository: BackupRepository
) : UseCase<DeleteBackupUseCase.Params, Unit>() {

    data class Params(val backupId: String)

    override suspend fun execute(params: Params): Unit {
        val result = backupRepository.deleteBackup(params.backupId)
        when (result) {
            is Result.Success -> {}
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for updating the backup schedule.
 */
class UpdateBackupScheduleUseCase(
    private val backupRepository: BackupRepository
) : UseCase<UpdateBackupScheduleUseCase.Params, Unit>() {

    data class Params(val schedule: BackupSchedule)

    override suspend fun execute(params: Params): Unit {
        val result = backupRepository.updateBackupSchedule(params.schedule)
        when (result) {
            is Result.Success -> {}
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for getting the backup schedule.
 */
class GetBackupScheduleUseCase(
    private val backupRepository: BackupRepository
) : UseCase<GetBackupScheduleUseCase.Params, BackupSchedule>() {

    data class Params(val dummy: Boolean = true)

    override suspend fun execute(params: Params): BackupSchedule {
        val result = backupRepository.getBackupSchedule()
        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for checking Google Drive authentication.
 */
class CheckGoogleDriveAuthUseCase(
    private val backupRepository: BackupRepository
) : UseCase<CheckGoogleDriveAuthUseCase.Params, Boolean>() {

    data class Params(val dummy: Boolean = true)

    override suspend fun execute(params: Params): Boolean {
        val result = backupRepository.isGoogleDriveAuthenticated()
        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for getting available storage.
 */
class GetAvailableStorageUseCase(
    private val backupRepository: BackupRepository
) : UseCase<GetAvailableStorageUseCase.Params, Long>() {

    data class Params(val dummy: Boolean = true)

    override suspend fun execute(params: Params): Long {
        val result = backupRepository.getAvailableStorage()
        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}

/**
 * Use case for getting local database size.
 */
class GetLocalDatabaseSizeUseCase(
    private val backupRepository: BackupRepository
) : UseCase<GetLocalDatabaseSizeUseCase.Params, Long>() {

    data class Params(val dummy: Boolean = true)

    override suspend fun execute(params: Params): Long {
        val result = backupRepository.getLocalDatabaseSize()
        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }
}
