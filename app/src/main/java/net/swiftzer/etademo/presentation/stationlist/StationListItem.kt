package net.swiftzer.etademo.presentation.stationlist

import androidx.recyclerview.widget.DiffUtil
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station

sealed interface StationListItem {
    data class Group(
        val line: Line,
        val isExpanded: Boolean,
    ) : StationListItem

    data class Child(
        val line: Line,
        val station: Station,
    ) : StationListItem

    object DiffCallback : DiffUtil.ItemCallback<StationListItem>() {
        override fun areItemsTheSame(oldItem: StationListItem, newItem: StationListItem): Boolean =
            when {
                oldItem is Group && newItem is Group -> oldItem.line == newItem.line
                oldItem is Child && newItem is Child -> oldItem.line == newItem.line && oldItem.station == oldItem.station
                else -> false
            }

        override fun areContentsTheSame(
            oldItem: StationListItem,
            newItem: StationListItem
        ): Boolean = when {
            oldItem is Group && newItem is Group -> oldItem == newItem
            oldItem is Child && newItem is Child -> oldItem == newItem
            else -> false
        }
    }
}
