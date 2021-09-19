package net.swiftzer.etademo.presentation.stationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.swiftzer.etademo.databinding.StationListFragmentBinding
import net.swiftzer.etademo.presentation.safeNavigate
import javax.inject.Inject

@AndroidEntryPoint
class StationListFragment : Fragment() {
    private val viewModel by viewModels<StationListViewModel>()
    private var _binding: StationListFragmentBinding? = null
    private val binding: StationListFragmentBinding get() = _binding!!
    private var _adapter: StationListAdapter? = null
    private val adapter: StationListAdapter get() = _adapter!!

    @Inject
    lateinit var presenter: LineStationPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StationListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _adapter = StationListAdapter(
            lifecycleOwner = viewLifecycleOwner,
            callback = viewModel,
            presenter = this.presenter,
        )
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StationListFragment.adapter
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.list.collect {
                    adapter.submitList(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.launchEtaScreen.collect { (line, station) ->
                    findNavController().safeNavigate(
                        StationListFragmentDirections.actionStationListFragmentToEtaFragment(
                            line,
                            station
                        )
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _adapter = null
        _binding = null
    }
}
