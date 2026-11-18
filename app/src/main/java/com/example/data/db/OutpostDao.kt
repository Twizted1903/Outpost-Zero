package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutpostDao {
    @Query("SELECT * FROM operators ORDER BY slotIndex ASC, createdAt ASC")
    fun observeOperators(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators ORDER BY slotIndex ASC, createdAt ASC")
    suspend fun getOperatorsOnce(): List<OperatorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOperator(entity: OperatorEntity)

    @Query("DELETE FROM operators WHERE id = :id")
    suspend fun deleteOperator(id: String)

    @Query("SELECT * FROM base_stash WHERE id = 1")
    fun observeBaseStash(): Flow<BaseStashEntity?>

    @Query("SELECT * FROM base_stash WHERE id = 1")
    suspend fun getBaseStashOnce(): BaseStashEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBaseStash(entity: BaseStashEntity)

    @Query("SELECT * FROM collected_cds ORDER BY cdId ASC")
    fun observeCollectedCds(): Flow<List<CollectedCdEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollectedCd(entity: CollectedCdEntity)

    @Query("DELETE FROM collected_cds WHERE cdId = :cdId")
    suspend fun deleteCollectedCd(cdId: Int)
}
