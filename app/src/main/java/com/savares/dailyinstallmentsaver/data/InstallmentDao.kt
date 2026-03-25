package com.savares.dailyinstallmentsaver.data

import androidx.room.*
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<InstallmentEntity>)

    @Update
    suspend fun update(installment: InstallmentEntity)

    @Query("SELECT * FROM installments ORDER BY id DESC")
    fun getAll(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments")
    suspend fun getAllList(): List<InstallmentEntity>

    @Delete
    suspend fun delete(installment: InstallmentEntity)

    @Query("DELETE FROM installments")
    suspend fun deleteAllInstallments()

    // Saving Logs
    @Insert
    suspend fun insertLog(log: SavingLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<SavingLogEntity>)

    @Query("SELECT * FROM saving_logs ORDER BY date DESC LIMIT 100")
    fun getAllLogs(): Flow<List<SavingLogEntity>>

    @Query("SELECT * FROM saving_logs")
    suspend fun getAllLogsList(): List<SavingLogEntity>

    @Query("DELETE FROM saving_logs")
    suspend fun deleteAllLogs()
    
    @Query("DELETE FROM saving_logs WHERE installmentName = :name")
    suspend fun deleteLogsByInstallmentName(name: String)
}
