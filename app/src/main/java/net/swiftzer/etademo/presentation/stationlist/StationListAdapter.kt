package net.swiftzer.etademo.presentation.stationlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.swiftzer.etademo.R
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.databinding.StationListLineItemBinding
import net.swiftzer.etademo.databinding.StationListStationItemBinding
import java.lang.ref.WeakReference


class StationListAdapter(
    lifecycleOwner: LifecycleOwner,
    presenter: LineStationPresenter,
    callback: Callback,
) : ListAdapter<StationListItem, RecyclerView.ViewHolder>(StationListItem.DiffCallback) {
    private val lifecycleOwner = WeakReference(lifecycleOwner)
    private val presenter = WeakReference(presenter)
    private val callback = WeakReference(callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.station_list_line_item -> LineItemViewHolder(
                binding = StationListLineItemBinding.inflate(inflater, parent, false),
                lifecycleOwner = requireNotNull(lifecycleOwner.get()),
                presenter = requireNotNull(presenter.get()),
                callback = requireNotNull(callback.get()),
            )
            R.layout.station_list_station_item -> StationItemViewHolder(
                binding = StationListStationItemBinding.inflate(inflater, parent, false),
                lifecycleOwner = requireNotNull(lifecycleOwner.get()),
                presenter = requireNotNull(presenter.get()),
                callback = requireNotNull(callback.get()),
            )
            else -> throw UnsupportedOperationException("Unsupported view type $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StationListItem.Group -> R.layout.station_list_line_item
        is StationListItem.Child -> R.layout.station_list_station_item
        else -> throw UnsupportedOperationException("Unsupported view type at position $position")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is StationListItem.Group -> (holder as LineItemViewHolder).bind(item)
            is StationListItem.Child -> (holder as StationItemViewHolder).bind(item)
        }
    }

    class LineItemViewHolder(
        private val binding: StationListLineItemBinding,
        lifecycleOwner: LifecycleOwner,
        presenter: LineStationPresenter,
        callback: Callback,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.presenter = presenter
            binding.callback = callback
        }

        fun bind(group: StationListItem.Group) {
            binding.group = group
        }
    }

    class StationItemViewHolder(
        private val binding: StationListStationItemBinding,
        lifecycleOwner: LifecycleOwner,
        presenter: LineStationPresenter,
        callback: Callback,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.presenter = presenter
            binding.callback = callback
        }

        fun bind(child: StationListItem.Child) {
            binding.child = child
        }
    }

    interface Callback {
        fun toggleExpanded(line: Line)
        fun onClickLineAndStation(line: Line, station: Station)
    }
}

