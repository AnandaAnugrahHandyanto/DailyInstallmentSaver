package com.savares.dailyinstallmentsaver.util

import android.content.Context
import android.net.Uri
import com.savares.dailyinstallmentsaver.data.InstallmentDao
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

@Serializable
data class BackupData(
    val installments: List<InstallmentEntity>,
    val logs: List<SavingLogEntity>
)

class BackupManager(private val dao: InstallmentDao) {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportData(context: Context, uri: Uri): Boolean {
        return try {
            val data = BackupData(
                installments = dao.getAllList(),
                logs = dao.getAllLogsList()
            )
            val jsonString = json.encodeToString(data)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importData(context: Context, uri: Uri): Boolean {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            } ?: return false
            
            val data = json.decodeFromString<BackupData>(content)
            
            dao.deleteAllInstallments()
            dao.deleteAllLogs()
            
            dao.insertAll(data.installments)
            dao.insertAllLogs(data.logs)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
