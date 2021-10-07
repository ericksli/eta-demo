package net.swiftzer.etademo.presentation.eta

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import net.swiftzer.etademo.R
import net.swiftzer.etademo.domain.EtaFailResult
import net.swiftzer.etademo.domain.EtaResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(ParameterizedRobolectricTestRunner::class)
class EtaPresenterTest(
    private val result: EtaFailResult,
    private val expectedString: String?,
    @StringRes private val expectedResourceId: Int,
) {

    private lateinit var presenter: EtaPresenter
    private lateinit var res: Resources

    @Before
    fun setUp() {
        presenter = EtaPresenter(ApplicationProvider.getApplicationContext())
        res = ApplicationProvider.getApplicationContext<Context>().resources
    }

    @Test
    fun mapErrorMessage() {
        if (expectedString == null) {
            expectThat(presenter.mapErrorMessage(result)).isEqualTo(res.getString(expectedResourceId))
        } else {
            expectThat(presenter.mapErrorMessage(result)).isEqualTo(expectedString)
        }
    }

    companion object {
        @JvmStatic
        @get:ParameterizedRobolectricTestRunner.Parameters(name = "")
        val data = listOf(
            arrayOf(EtaResult.Delay, null, R.string.delay),
            arrayOf(EtaResult.Incident("Incident", "https://example.com"), "Incident", 0),
            arrayOf(EtaResult.TooManyRequests, null, R.string.error),
            arrayOf(EtaResult.InternalServerError, null, R.string.error),
            arrayOf(EtaResult.Error(RuntimeException("Testing")), null, R.string.error),
        )
    }
}
