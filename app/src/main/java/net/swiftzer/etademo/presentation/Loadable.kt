package net.swiftzer.etademo.presentation

sealed interface Loadable<out T> {
    object Loading : Loadable<Nothing>
    data class Loaded<T>(val value: T) : Loadable<T>
}
