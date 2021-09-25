package net.swiftzer.etademo.presentation.eta

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
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
import net.swiftzer.etademo.R
import net.swiftzer.etademo.databinding.EtaFragmentBinding
import net.swiftzer.etademo.presentation.appLanguage
import net.swiftzer.etademo.presentation.stationlist.LineStationPresenter
import javax.inject.Inject

@AndroidEntryPoint
class EtaFragment : Fragment() {
    private val viewModel by viewModels<EtaViewModel>()
    private var _binding: EtaFragmentBinding? = null
    private val binding: EtaFragmentBinding get() = _binding!!
    private var _adapter: EtaListAdapter? = null
    private val adapter: EtaListAdapter get() = _adapter!!

    @Inject
    lateinit var lineStationPresenter: LineStationPresenter

    @Inject
    lateinit var etaPresenter: EtaPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, true) {
            viewModel.goBack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EtaFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.lineStationPresenter = lineStationPresenter
        binding.etaPresenter = etaPresenter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setLanguage(resources.configuration.appLanguage)

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.changeSorting -> {
                    viewModel.toggleSorting()
                    true
                }
                else -> false
            }
        }

        _adapter = EtaListAdapter(
            lifecycleOwner = viewLifecycleOwner,
            presenter = lineStationPresenter,
        )
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EtaFragment.adapter
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateBack.collect {
                    findNavController().popBackStack()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.etaList.collect {
                    adapter.submitList(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewIncidentDetail.collect {
                    try {
                        requireActivity().startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(it)
                        })
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            requireContext(),
                            R.string.cannot_launch_browser,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopAutoRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _adapter = null
        _binding = null
    }
}
