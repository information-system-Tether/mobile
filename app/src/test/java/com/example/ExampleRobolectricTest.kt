package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.DefaultFoodDatabase
import org.junit.Assert
import org.junit.Assert.assertEquals
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
    assertEquals("tether", appName)
  }

  @Test
  fun `food calculation is accurate for portion weight`() {
    val milk = DefaultFoodDatabase.search("Молоко").firstOrNull()
    Assert.assertNotNull(milk)
    val calculated = milk!!.calculateForWeight(200f) // 200g of Milk
    assertEquals(120f, calculated.calories, 0.5f)
    assertEquals(6.0f, calculated.protein, 0.5f)
  }

  @Test
  fun `account repository login and auto-sync work as expected`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.AccountRepository(context)
    val loginResult = repo.login("harp.mtw@gmail.com", "password123")
    org.junit.Assert.assertTrue(loginResult.isSuccess)
    val user = loginResult.getOrNull()
    org.junit.Assert.assertNotNull(user)
    assertEquals("harp.mtw@gmail.com", user?.email)
    assertEquals(com.example.data.model.SyncStatus.SYNCED, user?.syncStatus)
  }

  @Test
  fun `step calories and workout calculation are correct`() {
    val calories = com.example.data.model.StepActivityStats.calculateCaloriesFromSteps(10000, 70f)
    assertEquals(420f, calories, 1.0f)
    val runningBurn = com.example.data.model.WorkoutCategory.RUNNING.estimateBurned(30)
    assertEquals(345f, runningBurn, 1.0f)
  }
}
