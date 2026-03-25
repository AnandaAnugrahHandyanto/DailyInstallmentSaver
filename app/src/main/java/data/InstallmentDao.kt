package com.savares.dailyinstallmentsaver.data

import androidx.room.*
import com.savares.dailyinstallmentsaver.model.InstallmentEntity

@Dao
interface InstallmentDao {

    @Insert
    suspend fun insert(installment: InstallmentEntity)

    @Query("SELECT * FROM installments")
    suspend fun getAll(): List<InstallmentEntity>

    @Delete
    suspend fun delete(installment: InstallmentEntity)
}
