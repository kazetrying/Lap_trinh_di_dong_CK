package com.yourname.flashlearn.presentation.deck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yourname.flashlearn.databinding.FragmentDeckDetailBinding
import com.yourname.flashlearn.databinding.ItemCardBinding
import com.yourname.flashlearn.domain.model.Flashcard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeckDetailFragment : Fragment() {

    private var _binding: FragmentDeckDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeckDetailViewModel by viewModels()
    private lateinit var adapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CardAdapter(
            onDelete = { viewModel.deleteCard(it) },
            onEdit = { card -> showEditCardDialog(card) }
        )
        binding.rvCards.adapter = adapter
        binding.rvCards.layoutManager = LinearLayoutManager(requireContext())

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cards.collectLatest { adapter.submitList(it) }
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.fabAddCard.setOnClickListener { showAddCardDialog() }
    }

    private fun showAddCardDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }
        val etFront = EditText(requireContext()).apply { hint = "Mặt trước (câu hỏi)" }
        val etBack = EditText(requireContext()).apply { hint = "Mặt sau (đáp án)" }
        layout.addView(etFront)
        layout.addView(etBack)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm thẻ mới")
            .setView(layout)
            .setPositiveButton("Thêm") { _, _ ->
                val front = etFront.text.toString().trim()
                val back = etBack.text.toString().trim()
                if (front.isNotEmpty() && back.isNotEmpty()) {
                    viewModel.addCard(front, back)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditCardDialog(card: Flashcard) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
        }
        val etFront = EditText(requireContext()).apply {
            hint = "Mặt trước"
            setText(card.front)
        }
        val etBack = EditText(requireContext()).apply {
            hint = "Mặt sau"
            setText(card.back)
        }
        layout.addView(etFront)
        layout.addView(etBack)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa thẻ")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                val front = etFront.text.toString().trim()
                val back = etBack.text.toString().trim()
                if (front.isNotEmpty() && back.isNotEmpty()) {
                    viewModel.updateCard(card.copy(front = front, back = back))
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CardAdapter(
    private val onDelete: (Flashcard) -> Unit,
    private val onEdit: (Flashcard) -> Unit
) : ListAdapter<Flashcard, CardAdapter.CardViewHolder>(CardDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Flashcard) {
            binding.tvFront.text = card.front
            binding.tvBack.text = card.back
            binding.btnDelete.setOnClickListener { onDelete(card) }
            binding.btnEdit.setOnClickListener { onEdit(card) }
        }
    }

    class CardDiff : DiffUtil.ItemCallback<Flashcard>() {
        override fun areItemsTheSame(a: Flashcard, b: Flashcard) = a.id == b.id
        override fun areContentsTheSame(a: Flashcard, b: Flashcard) = a == b
    }
}