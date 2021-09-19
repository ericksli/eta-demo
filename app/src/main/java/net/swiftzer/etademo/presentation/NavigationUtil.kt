package net.swiftzer.etademo.presentation

import androidx.navigation.NavController
import androidx.navigation.NavDirections

/**
 * Prevent navigate if the current action ID same as the destination as it will throw
 * [IllegalArgumentException].
 *
 * [Reference](https://nezspencer.medium.com/navigation-components-a-fix-for-navigation-action-cannot-be-found-in-the-current-destination-95b63e16152e)
 */
fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run {
        navigate(direction)
    }
}
