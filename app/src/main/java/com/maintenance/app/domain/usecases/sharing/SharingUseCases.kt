package com.maintenance.app.domain.usecases.sharing

import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.ShareManager
import javax.inject.Inject

/**
 * Use case for sharing a record via WhatsApp.
 */
class ShareRecordViaWhatsAppUseCase @Inject constructor(
    private val shareManager: ShareManager
) : UseCase<ShareRecordViaWhatsAppUseCase.Params, Boolean>() {

    override suspend fun execute(parameters: Params): Boolean {
        return shareManager.shareRecordViaWhatsApp(parameters.record)
    }

    data class Params(val record: Record)
}

/**
 * Use case for sharing a maintenance via WhatsApp.
 */
class ShareMaintenanceViaWhatsAppUseCase @Inject constructor(
    private val shareManager: ShareManager
) : UseCase<ShareMaintenanceViaWhatsAppUseCase.Params, Boolean>() {

    override suspend fun execute(parameters: Params): Boolean {
        return shareManager.shareMaintenanceViaWhatsApp(parameters.maintenance, parameters.recordName)
    }

    data class Params(val maintenance: Maintenance, val recordName: String)
}

/**
 * Use case for sharing a record via generic share sheet.
 */
class ShareRecordGenericUseCase @Inject constructor(
    private val shareManager: ShareManager
) : UseCase<ShareRecordGenericUseCase.Params, Boolean>() {

    override suspend fun execute(parameters: Params): Boolean {
        return shareManager.shareRecordGeneric(parameters.record)
    }

    data class Params(val record: Record)
}

/**
 * Use case for sharing a maintenance via generic share sheet.
 */
class ShareMaintenanceGenericUseCase @Inject constructor(
    private val shareManager: ShareManager
) : UseCase<ShareMaintenanceGenericUseCase.Params, Boolean>() {

    override suspend fun execute(parameters: Params): Boolean {
        return shareManager.shareMaintenanceGeneric(parameters.maintenance, parameters.recordName)
    }

    data class Params(val maintenance: Maintenance, val recordName: String)
}

/**
 * Use case for checking if WhatsApp is installed.
 */
class CheckWhatsAppInstalledUseCase @Inject constructor(
    private val shareManager: ShareManager
) : UseCase<Unit, Boolean>() {

    override suspend fun execute(parameters: Unit): Boolean {
        return shareManager.isWhatsAppInstalled()
    }
}
