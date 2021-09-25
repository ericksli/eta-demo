package net.swiftzer.etademo.presentation

import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import androidx.navigation.NavArgsLazy

/**
 * Returns a [Lazy] delegate to access the ViewModel's [SavedStateHandle] as an [Args] instance.
 */
@MainThread
inline fun <reified Args : NavArgs> navArgs(savedStateHandle: SavedStateHandle) =
    NavArgsLazy(Args::class) {
        val pairs = savedStateHandle.keys()
            .map { Pair<String, Any?>(it, savedStateHandle[it]) }
            .toTypedArray()
        bundleOf(*pairs)
    }
