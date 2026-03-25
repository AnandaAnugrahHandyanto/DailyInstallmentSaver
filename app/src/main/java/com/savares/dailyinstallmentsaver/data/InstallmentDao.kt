package com.savares.dailyinstallmentsaver.data

import androidx.room.*
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity)

    @Update
    suspend fun update(installment: InstallmentEntity)

    @Query("SELECT * FROM installments ORDER BY id DESC")
    fun getAll(): Flow<List<InstallmentEntity>>

    @Delete
    suspend fun delete(installment: InstallmentEntity)

    // Saving Logs
    @Insert
    suspend fun insertLog(log: SavingLogEntity)

    @Query("SELECT * FROM saving_logs ORDER BY date DESC LIMIT 50")
    fun getAllLogs(): Flow<List<SavingLogEntity>>
}
