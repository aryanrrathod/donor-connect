package com.example

import com.example.location.LiveGpsLocation
import com.example.location.LiveLocationManager
import com.example.ui.screens.TrackingStage
import com.example.ui.screens.mapStatusToStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRequestTrackingStageMapping() {
    assertEquals(TrackingStage.BROADCASTED, mapStatusToStage("Urgent"))
    assertEquals(TrackingStage.BROADCASTED, mapStatusToStage("Active"))
    assertEquals(TrackingStage.DONORS_ALERTED, mapStatusToStage("Donors Notified"))
    assertEquals(TrackingStage.DONOR_ACCEPTED, mapStatusToStage("Donor Accepted"))
    assertEquals(TrackingStage.DONOR_ACCEPTED, mapStatusToStage("En Route"))
    assertEquals(TrackingStage.HOSPITAL_SCREENING, mapStatusToStage("In Screening"))
    assertEquals(TrackingStage.FULFILLED, mapStatusToStage("Fulfilled"))
  }

  @Test
  fun testTrackingStageProgression() {
    val stages = TrackingStage.entries
    assertEquals(5, stages.size)
    assertEquals(1, TrackingStage.BROADCASTED.step)
    assertEquals(5, TrackingStage.FULFILLED.step)
  }

  @Test
  fun testLiveLocationDistanceCalculation() {
    // Distance between same coordinates should be 0
    val distZero = LiveLocationManager.calculateDistanceKm(18.5204, 73.8567, 18.5204, 73.8567)
    assertEquals(0.0, distZero, 0.01)

    // Distance between Pune (18.5204, 73.8567) and Mumbai (18.9220, 72.8347) is approx 115-125 km
    val distMumbai = LiveLocationManager.calculateDistanceKm(18.5204, 73.8567, 18.9220, 72.8347)
    assertTrue("Distance to Mumbai should be > 100km and < 150km: was $distMumbai", distMumbai in 100.0..150.0)
  }

  @Test
  fun testLiveGpsLocationFormatting() {
    val gps = LiveGpsLocation(
      latitude = 18.52043,
      longitude = 73.85674,
      accuracyMeters = 8.0f,
      area = "Aundh",
      city = "Pune"
    )
    assertEquals("18.5204° N, 73.8567° E", gps.formattedCoordinates())
    assertEquals("±8m", gps.formattedAccuracy())
    assertEquals("Aundh, Pune", gps.formattedAddress())
  }
}
