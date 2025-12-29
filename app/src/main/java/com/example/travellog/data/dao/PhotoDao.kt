package com.example.travellog.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travellog.data.entities.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PhotoEntity>): List<Long>

    @Update
    suspend fun update(photo: PhotoEntity)

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deleteById(photoId: Long)

    @Query("SELECT * FROM photos ORDER BY captured_date DESC")
    fun getAll(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    fun getById(photoId: Long): Flow<PhotoEntity?>

    @Query("SELECT * FROM photos WHERE state_code = :stateCode ORDER BY captured_date DESC")
    fun getByState(stateCode: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE state_name = :stateName ORDER BY captured_date DESC")
    fun getByStateName(stateName: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE city_name = :cityName ORDER BY captured_date DESC")
    fun getByCity(cityName: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE state_code = :stateCode AND city_name = :cityName ORDER BY captured_date DESC")
    fun getByStateAndCity(stateCode: String, cityName: String): Flow<List<PhotoEntity>>

    @Query("SELECT DISTINCT state_code, state_name FROM photos ORDER BY state_name ASC")
    fun getAllStates(): Flow<List<PhotoEntity>>

    @Query("SELECT DISTINCT city_name FROM photos WHERE state_code = :stateCode ORDER BY city_name ASC")
    fun getCitiesByState(stateCode: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM photos")
    fun getPhotoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE state_code = :stateCode")
    fun getPhotoCountByState(stateCode: String): Flow<Int>
}
