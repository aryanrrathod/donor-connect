package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.BloodViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Donor Connect", appName)
  }

  @Test
  fun `donor registration occurs only once`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BloodViewModel(app)

    // Ensure clean state
    viewModel.resetRegistrationForTesting()
    assertFalse(viewModel.userProfile.value.isRegisteredDonor)

    // First registration should succeed
    var firstSuccess = false
    viewModel.registerAsDonor(
      name = "Test Donor",
      bloodGroup = "O+",
      phone = "9876543210",
      area = "Kothrud",
      city = "Pune"
    ) { success ->
      firstSuccess = success
    }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertTrue("First registration must succeed", firstSuccess)
    assertTrue("Profile must mark isRegisteredDonor as true", viewModel.userProfile.value.isRegisteredDonor)
    assertEquals("Test Donor", viewModel.userProfile.value.name)
    assertEquals("O+", viewModel.userProfile.value.bloodGroup)

    // Second registration attempt MUST be rejected (occur once rule)
    var secondSuccess = true
    viewModel.registerAsDonor(
      name = "Duplicate Name",
      bloodGroup = "AB+",
      phone = "9999999999",
      area = "Camp",
      city = "Pune"
    ) { success ->
      secondSuccess = success
    }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertFalse("Second registration attempt must fail because registration occurs once", secondSuccess)
    // Original profile data remains preserved
    assertEquals("Test Donor", viewModel.userProfile.value.name)
  }

  @Test
  fun `tracker customization saves and persists per request`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BloodViewModel(app)

    val customConfig = com.example.viewmodel.TrackerCustomization(
      transportMode = "Emergency Ambulance",
      assignedResponderName = "Dr. Sameer Joshi",
      responderRole = "Critical Care Paramedic",
      responderContact = "9822001122",
      customEtaMinutes = 8,
      hospitalWardRoom = "Emergency Trauma Wing 2",
      alertRadiusKm = 10,
      updateFrequencySec = 5,
      sirenSoundAlerts = true,
      smsAttendantAlerts = true
    )

    viewModel.saveTrackerCustomization(101L, customConfig)
    val saved = viewModel.trackerCustomizations.value[101L]

    org.junit.Assert.assertNotNull(saved)
    assertEquals("Emergency Ambulance", saved?.transportMode)
    assertEquals("Dr. Sameer Joshi", saved?.assignedResponderName)
    assertEquals(10, saved?.alertRadiusKm)
    assertEquals(8, saved?.customEtaMinutes)
    assertEquals("Emergency Trauma Wing 2", saved?.hospitalWardRoom)
  }
}
