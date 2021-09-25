package net.swiftzer.etademo.presentation.eta

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import net.swiftzer.etademo.R
import net.swiftzer.etademo.domain.EtaFailResult
import net.swiftzer.etademo.domain.EtaResult
import javax.inject.Inject

@ActivityScoped
class EtaPresenter @Inject constructor(@ActivityContext context: Context) {
    private val res = context.resources

    fun mapErrorMessage(result: EtaFailResult): String = when (result) {
        EtaResult.Delay -> res.getString(R.string.delay)
        is EtaResult.Error,
        EtaResult.InternalServerError,
        EtaResult.TooManyRequests,
        -> res.getString(R.string.error)
        is EtaResult.Incident -> result.message
    }
}
