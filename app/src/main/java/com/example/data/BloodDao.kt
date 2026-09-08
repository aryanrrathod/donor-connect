package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodDao {
    // Donors
    @Query("SELECT * FROM donors ORDER BY distanceKm ASC")
    fun getAllDonors(): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE bloodGroup = :bloodGroup ORDER BY distanceKm ASC")
    fun getDonorsByBloodGroup(bloodGroup: String): Flow<List<DonorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonors(donors: List<DonorEntity>)

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    // Emergency Requests
    @Query("SELECT * FROM emergency_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<EmergencyRequestEntity>>

    @Query("SELECT * FROM emergency_requests WHERE id = :id")
    fun getRequestById(id: Long): Flow<EmergencyRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: EmergencyRequestEntity): Long

    @Query("UPDATE emergency_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String)

    @Update
    suspend fun updateRequest(request: EmergencyRequestEntity)

    @Query("DELETE FROM emergency_requests WHERE id = :id")
    suspend fun deleteRequest(id: Long)

    // Facilities
    @Query("SELECT * FROM facilities ORDER BY distanceKm ASC")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities WHERE type = :type ORDER BY distanceKm ASC")
    fun getFacilitiesByType(type: String): Flow<List<FacilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<FacilityEntity>)

    // Blood Stock
    @Query("SELECT * FROM blood_stock")
    fun getAllStock(): Flow<List<BloodStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stockList: List<BloodStockEntity>)

    // Donation Records
    @Query("SELECT * FROM donation_history ORDER BY id DESC")
    fun getDonationHistory(): Flow<List<DonationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonationRecord(record: DonationRecordEntity): Long
}
