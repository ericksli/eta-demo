package net.swiftzer.etademo.presentation

import android.content.res.Configuration
import androidx.core.os.ConfigurationCompat
import net.swiftzer.etademo.common.Language
import java.util.*

val Configuration.appLanguage: Language
    get() = if (ConfigurationCompat.getLocales(this)[0].language == Locale.CHINESE.language) {
        Language.CHINESE
    } else {
        Language.ENGLISH
    }
