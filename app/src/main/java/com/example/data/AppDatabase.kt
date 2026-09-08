package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DonorEntity::class,
        EmergencyRequestEntity::class,
        FacilityEntity::class,
        BloodStockEntity::class,
        DonationRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bloodDao(): BloodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_blood_donation.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDatabaseDirect(dao: BloodDao) {
            AppDatabaseCallback(CoroutineScope(Dispatchers.IO)).populateDatabase(dao)
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.bloodDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: BloodDao) {
                // Seed Donors matching the screenshot:
                // Rahul Sharma (O+ · 1.2 km, Aundh, Pune)
                // Amit Verma (O+ · 2.4 km, Baner, Pune)
                // Sneha Patil (O+ · 3.1 km, Wakad, Pune)
                // + additional varied donors
                val donors = listOf(
                    DonorEntity(
                        name = "Rahul Sharma",
                        bloodGroup = "O+",
                        distanceKm = 1.2,
                        area = "Aundh",
                        city = "Pune",
                        phone = "9876543210",
                        isAvailable = true,
                        totalDonations = 5,
                        lastDonationDate = "12 Jul 2026",
                        latOffset = 0.005f,
                        lngOffset = -0.004f
                    ),
                    DonorEntity(
                        name = "Amit Verma",
                        bloodGroup = "O+",
                        distanceKm = 2.4,
                        area = "Baner",
                        city = "Pune",
                        phone = "9823456789",
                        isAvailable = true,
                        totalDonations = 3,
                        lastDonationDate = "05 May 2026",
                        latOffset = -0.008f,
                        lngOffset = 0.007f
                    ),
                    DonorEntity(
                        name = "Sneha Patil",
                        bloodGroup = "O+",
                        distanceKm = 3.1,
                        area = "Wakad",
                        city = "Pune",
                        phone = "9765432109",
                        isAvailable = true,
                        totalDonations = 4,
                        lastDonationDate = "19 Jun 2026",
                        latOffset = 0.012f,
                        lngOffset = 0.009f
                    ),
                    DonorEntity(
                        name = "Priya Deshmukh",
                        bloodGroup = "A+",
                        distanceKm = 1.8,
                        area = "Kothrud",
                        city = "Pune",
                        phone = "9890123456",
                        isAvailable = true,
                        totalDonations = 6,
                        lastDonationDate = "22 Aug 2026",
                        latOffset = -0.003f,
                        lngOffset = -0.011f
                    ),
                    DonorEntity(
                        name = "Vikram Joshi",
                        bloodGroup = "B+",
                        distanceKm = 3.7,
                        area = "Shivaji Nagar",
                        city = "Pune",
                        phone = "9822334455",
                        isAvailable = true,
                        totalDonations = 2,
                        lastDonationDate = "02 Feb 2026",
                        latOffset = 0.007f,
                        lngOffset = -0.015f
                    ),
                    DonorEntity(
                        name = "Ananya Roy",
                        bloodGroup = "AB+",
                        distanceKm = 4.2,
                        area = "Viman Nagar",
                        city = "Pune",
                        phone = "9811223344",
                        isAvailable = true,
                        totalDonations = 4,
                        lastDonationDate = "14 Mar 2026",
                        latOffset = 0.015f,
                        lngOffset = -0.008f
                    ),
                    DonorEntity(
                        name = "Kunal Shinde",
                        bloodGroup = "O-",
                        distanceKm = 2.9,
                        area = "Hinjawadi",
                        city = "Pune",
                        phone = "9855667788",
                        isAvailable = true,
                        totalDonations = 8,
                        lastDonationDate = "10 Jan 2026",
                        latOffset = -0.014f,
                        lngOffset = 0.003f
                    ),
                    DonorEntity(
                        name = "Meera Nair",
                        bloodGroup = "A-",
                        distanceKm = 5.0,
                        area = "Hadapsar",
                        city = "Pune",
                        phone = "9877889900",
                        isAvailable = true,
                        totalDonations = 1,
                        lastDonationDate = "01 Apr 2026",
                        latOffset = 0.009f,
                        lngOffset = 0.018f
                    )
                )
                dao.insertDonors(donors)

                // Seed Emergency Requests
                val requests = listOf(
                    EmergencyRequestEntity(
                        patientName = "Raj Kumar",
                        bloodGroup = "O+",
                        unitsRequired = 2,
                        hospitalName = "Ruby Hall Clinic",
                        location = "Pune, Maharashtra",
                        urgency = "High",
                        contactNumber = "9876543210",
                        additionalNotes = "Urgent requirement for cardiac surgery unit (Ward 4B)",
                        status = "Urgent"
                    ),
                    EmergencyRequestEntity(
                        patientName = "Kavita Rathi",
                        bloodGroup = "B+",
                        unitsRequired = 3,
                        hospitalName = "Jehangir Hospital",
                        location = "Pune, Maharashtra",
                        urgency = "High",
                        contactNumber = "9823456780",
                        additionalNotes = "Accident emergency case in ICU",
                        status = "Urgent"
                    )
                )
                requests.forEach { dao.insertRequest(it) }

                // Seed Hospitals and Blood Banks (matching screenshot):
                // Ruby Hall Clinic (1.1 km · Pune)
                // Jehangir Hospital (2.3 km · Pune)
                // Sassoon Hospital (3.5 km · Pune)
                // Deenanath Mangeshkar Hospital (4.1 km · Pune)
                val facilities = listOf(
                    FacilityEntity(
                        name = "Ruby Hall Clinic",
                        type = "Hospital",
                        distanceKm = 1.1,
                        city = "Pune",
                        address = "40 Sassoon Road, Sangamvadi, Pune",
                        phone = "020-66455100",
                        isOpen24x7 = true,
                        availableStockSummary = "A+, B+, O+, AB+ in stock",
                        latOffset = 0.004f,
                        lngOffset = 0.003f
                    ),
                    FacilityEntity(
                        name = "Jehangir Hospital",
                        type = "Hospital",
                        distanceKm = 2.3,
                        city = "Pune",
                        address = "32 Sassoon Road, Central Station, Pune",
                        phone = "020-66811000",
                        isOpen24x7 = true,
                        availableStockSummary = "All groups available 24/7",
                        latOffset = -0.006f,
                        lngOffset = 0.005f
                    ),
                    FacilityEntity(
                        name = "Sassoon Hospital",
                        type = "Hospital",
                        distanceKm = 3.5,
                        city = "Pune",
                        address = "Near Pune Railway Station, Pune",
                        phone = "020-26128000",
                        isOpen24x7 = true,
                        availableStockSummary = "Government hospital blood repository",
                        latOffset = -0.008f,
                        lngOffset = -0.006f
                    ),
                    FacilityEntity(
                        name = "Deenanath Mangeshkar Hospital",
                        type = "Hospital",
                        distanceKm = 4.1,
                        city = "Pune",
                        address = "Erandwane, Near Mhatre Bridge, Pune",
                        phone = "020-40151000",
                        isOpen24x7 = true,
                        availableStockSummary = "A+, O+, B+ Available",
                        latOffset = 0.011f,
                        lngOffset = -0.012f
                    ),
                    FacilityEntity(
                        name = "Red Cross Blood Bank",
                        type = "Blood Bank",
                        distanceKm = 1.6,
                        city = "Pune",
                        address = "Rasta Peth, Somwar Peth, Pune",
                        phone = "020-26131444",
                        isOpen24x7 = true,
                        availableStockSummary = "Major Regional Central Blood Repository",
                        latOffset = -0.003f,
                        lngOffset = 0.002f
                    ),
                    FacilityEntity(
                        name = "Poona Serological Institute Blood Bank",
                        type = "Blood Bank",
                        distanceKm = 2.8,
                        city = "Pune",
                        address = "Deccan Gymkhana, Pune",
                        phone = "020-25674321",
                        isOpen24x7 = true,
                        availableStockSummary = "Platelets & Whole Blood storage",
                        latOffset = 0.007f,
                        lngOffset = -0.009f
                    ),
                    FacilityEntity(
                        name = "Jankalyan Blood Centre",
                        type = "Blood Bank",
                        distanceKm = 3.9,
                        city = "Pune",
                        address = "Sarasbaug Road, Navi Peth, Pune",
                        phone = "020-24449514",
                        isOpen24x7 = true,
                        availableStockSummary = "High stock of rare blood groups",
                        latOffset = -0.011f,
                        lngOffset = -0.005f
                    )
                )
                dao.insertFacilities(facilities)

                // Seed Blood Availability Matrix (matching the screenshot exactly):
                // A+: Available [Green]
                // A-: Low [Orange]
                // B+: Available [Green]
                // B-: Low [Orange]
                // AB+: Available [Green]
                // AB-: Not Available [Red]
                // O+: Available [Green]
                // O-: Low [Orange]
                val stocks = listOf(
                    BloodStockEntity("A+", 24, "Available", "Updated 10 mins ago"),
                    BloodStockEntity("A-", 4, "Low", "Updated 15 mins ago"),
                    BloodStockEntity("B+", 32, "Available", "Updated 5 mins ago"),
                    BloodStockEntity("B-", 3, "Low", "Updated 20 mins ago"),
                    BloodStockEntity("AB+", 18, "Available", "Updated 12 mins ago"),
                    BloodStockEntity("AB-", 0, "Not Available", "Updated 8 mins ago"),
                    BloodStockEntity("O+", 45, "Available", "Updated 2 mins ago"),
                    BloodStockEntity("O-", 2, "Low", "Updated 18 mins ago")
                )
                dao.insertOrUpdateStock(stocks)

                // Seed Donation History
                val records = listOf(
                    DonationRecordEntity(
                        hospitalName = "Ruby Hall Clinic",
                        donationDate = "12 Jul 2026",
                        unitsDonated = 1,
                        bloodGroup = "O+",
                        certificateId = "CERT-RHC-8829"
                    ),
                    DonationRecordEntity(
                        hospitalName = "Red Cross Blood Center",
                        donationDate = "15 Feb 2026",
                        unitsDonated = 1,
                        bloodGroup = "O+",
                        certificateId = "CERT-RC-4102"
                    ),
                    DonationRecordEntity(
                        hospitalName = "Jehangir Hospital",
                        donationDate = "18 Sep 2025",
                        unitsDonated = 1,
                        bloodGroup = "O+",
                        certificateId = "CERT-JH-1994"
                    )
                )
                records.forEach { dao.insertDonationRecord(it) }
            }
        }
    }
}
