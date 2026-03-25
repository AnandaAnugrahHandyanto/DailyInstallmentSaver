package com.savares.dailyinstallmentsaver.data

import androidx.room.*
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
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
}
