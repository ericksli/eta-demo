package net.swiftzer.etademo.presentation.eta

import androidx.recyclerview.widget.DiffUtil
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.EtaResult

sealed interface EtaListItem {
    @JvmInline
    value class Header(val direction: EtaResult.Success.Eta.Direction) : EtaListItem

    data class Eta(
        val direction: EtaResult.Success.Eta.Direction,
        val destination: Station,
        val platform: String,
        val minuteCountdown: Int,
    ) : EtaListItem

    object DiffCallback : DiffUtil.ItemCallback<EtaListItem>() {
        override fun areItemsTheSame(oldItem: EtaListItem, newItem: EtaListItem): Boolean =
            when {
                oldItem is Header && newItem is Header -> oldItem.direction == newItem.direction
                oldItem is Eta && newItem is Eta -> oldItem == newItem
                else -> false
            }

        override fun areContentsTheSame(
            oldItem: EtaListItem,
            newItem: EtaListItem
        ): Boolean = when {
            oldItem is Header && newItem is Header -> oldItem == newItem
            oldItem is Eta && newItem is Eta -> oldItem == newItem
            else -> false
        }
    }
}
