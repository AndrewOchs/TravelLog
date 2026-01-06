package com.example.travellog.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travellog.data.entities.PhotoEntity
import com.example.travellog.data.models.CityPhotoCount
import com.example.travellog.data.models.StateInfo
import com.example.travellog.data.models.StatePhotoCount
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

    @Query("SELECT * FROM photos WHERE TRIM(city_name) = :cityName ORDER BY captured_date DESC")
    fun getByCity(cityName: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE state_code = :stateCode AND city_name = :cityName ORDER BY captured_date DESC")
    fun getByStateAndCity(stateCode: String, cityName: String): Flow<List<PhotoEntity>>

    // Get distinct states (for navigation/filtering)
    @Query("SELECT DISTINCT state_code, state_name FROM photos ORDER BY state_name ASC")
    fun getAllStates(): Flow<List<StateInfo>>

    // Get cities within a state
    @Query("SELECT DISTINCT city_name FROM photos WHERE state_code = :stateCode ORDER BY city_name ASC")
    fun getCitiesByState(stateCode: String): Flow<List<String>>

    // Simple count queries
    @Query("SELECT COUNT(*) FROM photos")
    fun getPhotoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE state_code = :stateCode")
    fun getPhotoCountByState(stateCode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE city_name = :cityName")
    fun getPhotoCountByCity(cityName: String): Flow<Int>

    // Aggregated counts with grouping (for statistics/dashboard)
    @Query("SELECT state_code, state_name, COUNT(*) as photo_count FROM photos GROUP BY state_code, state_name ORDER BY photo_count DESC")
    fun getStatePhotoCounts(): Flow<List<StatePhotoCount>>

    @Query("SELECT state_code, city_name, COUNT(*) as photo_count FROM photos WHERE state_code = :stateCode GROUP BY city_name ORDER BY photo_count DESC")
    fun getCityPhotoCountsByState(stateCode: String): Flow<List<CityPhotoCount>>

    @Query("SELECT state_code, city_name, COUNT(*) as photo_count FROM photos GROUP BY state_code, city_name ORDER BY photo_count DESC")
    fun getAllCityPhotoCounts(): Flow<List<CityPhotoCount>>
}
