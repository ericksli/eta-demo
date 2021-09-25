package net.swiftzer.etademo.presentation.eta

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.swiftzer.etademo.R
import net.swiftzer.etademo.databinding.EtaListEtaItemBinding
import net.swiftzer.etademo.databinding.EtaListHeaderItemBinding
import net.swiftzer.etademo.domain.EtaResult
import net.swiftzer.etademo.presentation.stationlist.LineStationPresenter
import java.lang.ref.WeakReference

class EtaListAdapter(
    lifecycleOwner: LifecycleOwner,
    presenter: LineStationPresenter,
) : ListAdapter<EtaListItem, RecyclerView.ViewHolder>(EtaListItem.DiffCallback) {
    private val lifecycleOwner = WeakReference(lifecycleOwner)
    private val presenter = WeakReference(presenter)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.eta_list_header_item -> HeaderViewHolder(
                binding = EtaListHeaderItemBinding.inflate(inflater, parent, false),
            )
            R.layout.eta_list_eta_item -> EtaViewHolder(
                lifecycleOwner = requireNotNull(lifecycleOwner.get()),
                binding = EtaListEtaItemBinding.inflate(inflater, parent, false),
                presenter = requireNotNull(presenter.get()),
            )
            else -> throw UnsupportedOperationException("Unsupported view type $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is EtaListItem.Header -> R.layout.eta_list_header_item
        is EtaListItem.Eta -> R.layout.eta_list_eta_item
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EtaListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is EtaListItem.Eta -> (holder as EtaViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(
        private val binding: EtaListHeaderItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: EtaListItem.Header) {
            binding.root.text = binding.root.resources.getString(
                when (header.direction) {
                    EtaResult.Success.Eta.Direction.UP -> R.string.up_track
                    EtaResult.Success.Eta.Direction.DOWN -> R.string.down_track
                }
            )
        }
    }

    class EtaViewHolder(
        lifecycleOwner: LifecycleOwner,
        private val binding: EtaListEtaItemBinding,
        presenter: LineStationPresenter,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.presenter = presenter
        }

        fun bind(eta: EtaListItem.Eta) {
            binding.eta = eta
        }
    }
}
