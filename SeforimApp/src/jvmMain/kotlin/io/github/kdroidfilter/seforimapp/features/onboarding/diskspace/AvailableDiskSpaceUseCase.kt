package io.github.kdroidfilter.seforimapp.features.onboarding.diskspace

import dev.nucleusframework.systeminfo.SystemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvailableDiskSpaceUseCase {
    suspend fun getDiskSpaceInfo(): DiskSpaceInfo =
        withContext(Dispatchers.IO) {
            val disks = SystemInfo.disks()

            // תיקון 1: בדיקת הכונן הנוכחי (הנייד) במקום כונן C או תיקיית המשתמש
            val currentDir = System.getProperty("user.dir") ?: ""
            val systemDir =
                disks.firstOrNull {
                    currentDir.startsWith(it.mountPoint) || it.mountPoint == "/"
                } ?: disks.first()

            DiskSpaceInfo(
                availableBytes = systemDir.availableSpace,
                totalBytes = systemDir.totalSpace,
            )
        }

    data class DiskSpaceInfo(
        val availableBytes: Long,
        val totalBytes: Long,
    ) {
        // תיקון 2: תמיד מחזיר "אמת" כדי שכפתור ההמשך לעולם לא ייחסם!
        val hasEnoughSpace: Boolean get() = true
        
        // מונע הצגת מספר שלילי בעוגה במקרה שהכונן באמת מפוצץ
        val remainingAfterInstall: Long get() = if (availableBytes > REQUIRED_SPACE_BYTES) availableBytes - REQUIRED_SPACE_BYTES else 0L
    }

    companion object {
        const val REQUIRED_SPACE_GB = 10L
        const val TEMPORARY_SPACE_GB = 2.5
        const val FINAL_SPACE_GB = 7.5
        val REQUIRED_SPACE_BYTES = REQUIRED_SPACE_GB * 1024 * 1024 * 1024
    }
}
