package com.dicoding.picodiploma.dicodingstoryapp.auth

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.view.login.LoginActivity
import com.dicoding.picodiploma.dicodingstoryapp.JsonConverter
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val RESOURCE = "GLOBAL"

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginLogoutTest {

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(countingIdlingResource)

        mockWebServer.start(8080)
        ApiConfig.baseUrl = "http://127.0.0.1:8080/"
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(countingIdlingResource)

        mockWebServer.shutdown()
    }

    @Test
    fun loginLogoutFlow_Success() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("succes_response.json"))
        mockWebServer.enqueue(mockResponse)

        loginProcess()

        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fab_add))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btn_logout))
            .perform(click())

        onView(withText(R.string.logout))
            .perform(click())

        onView(withText(R.string.logout))
            .check(matches(isDisplayed()))

        onView(withText(R.string.logout_confirmation))
            .check(matches(isDisplayed()))

        onView(withText(R.string.yes))
            .perform(click())

        onView(withId(R.id.loginButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun loginWithInvalidEmail_ShowError() {
        val mockResponse = MockResponse()
            .setResponseCode(500)
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.emailEditText))
            .perform(typeText("invalid-email"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.loginButton))
            .perform(click())

        onView(withText(R.string.unvalid_email))
            .check(matches(isDisplayed()))
    }

    @Test
    fun loginWithShortPassword_ShowError() {
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("123"), closeSoftKeyboard())

        onView(withId(R.id.loginButton))
            .perform(click())

        onView(withText(R.string.unvalid_password))
            .check(matches(isDisplayed()))
    }

    @Test
    fun cancelLogout_StayInMainActivity() {
        loginProcess()

        onView(withId(R.id.btn_logout))
            .perform(click())

        onView(withText(R.string.logout))
            .perform(click())

        onView(withText(R.string.logout))
            .check(matches(isDisplayed()))

        onView(withText(R.string.no))
            .perform(click())

        onView(withId(R.id.fab_add))
            .check(matches(isDisplayed()))
    }
    @Test
    fun loginWithServerError_500_ShowError() {
        val mockResponse = MockResponse()
            .setResponseCode(500)
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.emailEditText))
            .perform(typeText("aditgenz@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("00000000"), closeSoftKeyboard())

        onView(withId(R.id.loginButton))
            .perform(click())

        onView(withText(R.string.login_failed))
            .check(matches(isDisplayed()))
    }
    private fun loginProcess() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("succes_response.json"))
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.emailEditText))
            .perform(typeText("aditgenz@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("00000000"), closeSoftKeyboard())

        onView(withId(R.id.loginButton))
            .perform(click())

        onView(withText("Yeah!"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText(R.string.enter))
            .perform(click())

    }
}
