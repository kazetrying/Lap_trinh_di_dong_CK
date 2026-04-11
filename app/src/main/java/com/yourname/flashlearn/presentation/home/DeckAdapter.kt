package com.yourname.flashlearn.presentation.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourname.flashlearn.databinding.ItemDeckBinding
import com.yourname.flashlearn.domain.model.Deck

class DeckAdapter(
    private val onStudyClick: (Deck) -> Unit,
    private val onDeckClick: (Deck) -> Unit,
    private val onDeleteClick: (Deck) -> Unit
) : ListAdapter<Deck, DeckAdapter.DeckViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemDeckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeckViewHolder(private val binding: ItemDeckBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(deck: Deck) {
            binding.tvDeckTitle.text = deck.title
            binding.tvCardCount.text = "${deck.cardCount} thẻ"
            try {
                binding.colorIndicator.setBackgroundColor(Color.parseColor(deck.coverColor))
            } catch (e: Exception) {
                binding.colorIndicator.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            binding.btnStudy.setOnClickListener { onStudyClick(deck) }
            binding.root.setOnClickListener { onDeckClick(deck) }
            binding.btnDelete.setOnClickListener { onDeleteClick(deck) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Deck>() {
        override fun areItemsTheSame(oldItem: Deck, newItem: Deck) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Deck, newItem: Deck) = oldItem == newItem
    }
}