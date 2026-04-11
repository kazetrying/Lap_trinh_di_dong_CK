package com.yourname.flashlearn.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yourname.flashlearn.R
import com.yourname.flashlearn.databinding.FragmentHomeBinding
import com.yourname.flashlearn.util.UserManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: DeckAdapter

    @Inject
    lateinit var userManager: UserManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        binding.fabAddDeck.setOnClickListener { showCreateDeckDialog() }

        binding.toolbar.subtitle = "Xin chào, ${userManager.getCurrentUser()}"

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Đăng xuất?")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất") { _, _ ->
                        userManager.logout()
                        val intent = android.content.Intent(
                            requireContext(),
                            com.yourname.flashlearn.LoginActivity::class.java
                        )
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
                true
            } else false
        }
    }

    private fun setupRecyclerView() {
        adapter = DeckAdapter(
            onStudyClick = { deck ->
                findNavController().navigate(
                    R.id.studyFragment,
                    bundleOf("deckId" to deck.id)
                )
            },
            onDeckClick = { deck ->
                findNavController().navigate(
                    R.id.deckDetailFragment,
                    bundleOf("deckId" to deck.id)
                )
            },
            onDeleteClick = { deck ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xóa bộ thẻ?")
                    .setMessage("Bạn có chắc muốn xóa \"${deck.title}\" không?")
                    .setPositiveButton("Xóa") { _, _ -> viewModel.deleteDeck(deck) }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        )
        binding.rvDecks.adapter = adapter
        binding.rvDecks.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.decks.collectLatest { adapter.submitList(it) }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalDueCount.collectLatest {
                binding.tvDueCount.text = it.toString()
            }
        }
    }

    private val colorOptions = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
        "#2196F3", "#4CAF50", "#FF9800", "#795548"
    )
    private var selectedColor = "#4CAF50"

    private fun showCreateDeckDialog() {
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }
        val etTitle = android.widget.EditText(requireContext()).apply {
            hint = "Tên bộ thẻ"
        }
        val colorLabel = android.widget.TextView(requireContext()).apply {
            text = "Chọn màu:"
            setPadding(0, 16, 0, 8)
        }
        val colorRow = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }
        val colorPreview = android.view.View(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(48, 48).apply {
                marginEnd = 16
            }
            setBackgroundColor(android.graphics.Color.parseColor(selectedColor))
        }
        colorOptions.forEach { color ->
            val dot = android.view.View(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(60, 60).apply {
                    marginEnd = 8
                }
                setBackgroundColor(android.graphics.Color.parseColor(color))
                setOnClickListener {
                    selectedColor = color
                    colorPreview.setBackgroundColor(android.graphics.Color.parseColor(color))
                }
            }
            colorRow.addView(dot)
        }
        layout.addView(etTitle)
        layout.addView(colorLabel)
        layout.addView(colorPreview)
        layout.addView(colorRow)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tạo bộ thẻ mới")
            .setView(layout)
            .setPositiveButton("Tạo") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) viewModel.createDeck(title, "", selectedColor)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}