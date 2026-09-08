package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BloodRepository(private val dao: BloodDao, private val scope: CoroutineScope) {

    init {
        // Guarantee data is populated even if DB was already existing or empty
        scope.launch(Dispatchers.IO) {
            val existing = dao.getAllDonors().first()
            if (existing.isEmpty()) {
                AppDatabase.populateDatabaseDirect(dao)
            }
        }
    }

    val allDonors: Flow<List<DonorEntity>> = dao.getAllDonors()
    val allRequests: Flow<List<EmergencyRequestEntity>> = dao.getAllRequests()
    val allFacilities: Flow<List<FacilityEntity>> = dao.getAllFacilities()
    val allStock: Flow<List<BloodStockEntity>> = dao.getAllStock()
    val donationHistory: Flow<List<DonationRecordEntity>> = dao.getDonationHistory()

    fun getFacilitiesByType(type: String): Flow<List<FacilityEntity>> =
        dao.getFacilitiesByType(type)

    fun getDonorsByBloodGroup(group: String): Flow<List<DonorEntity>> =
        dao.getDonorsByBloodGroup(group)

    suspend fun insertDonor(donor: DonorEntity): Long =
        dao.insertDonor(donor)

    suspend fun updateDonor(donor: DonorEntity) =
        dao.updateDonor(donor)

    suspend fun insertEmergencyRequest(request: EmergencyRequestEntity): Long =
        dao.insertRequest(request)

    fun getRequestById(id: Long): Flow<EmergencyRequestEntity?> =
        dao.getRequestById(id)

    suspend fun updateRequestStatus(id: Long, status: String) =
        dao.updateRequestStatus(id, status)

    suspend fun updateEmergencyRequest(request: EmergencyRequestEntity) =
        dao.updateRequest(request)

    suspend fun deleteEmergencyRequest(id: Long) =
        dao.deleteRequest(id)

    suspend fun addDonationRecord(record: DonationRecordEntity): Long =
        dao.insertDonationRecord(record)
}
