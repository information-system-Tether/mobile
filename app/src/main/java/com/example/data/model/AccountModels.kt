package com.example.data.model

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    ERROR,
    OFFLINE
}

data class UserAccount(
    val userId: String = "usr_harp_mtw",
    val email: String = "harp.mtw@gmail.com",
    val displayName: String = "Harp M.",
    val isLoggedIn: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val autoSyncEnabled: Boolean = true,
    val syncedRecordsCount: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
) {
    val initials: String
        get() {
            val parts = displayName.trim().split(" ")
            return when {
                parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
                email.isNotEmpty() -> email.take(2).uppercase()
                else -> "АК"
            }
        }
}

data class CloudBackupInfo(
    val userId: String,
    val backupTimestamp: Long,
    val totalFoodEntries: Int,
    val totalWaterLogs: Int,
    val totalCustomProducts: Int,
    val storageSizeKb: Float
)
