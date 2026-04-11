package com.example.flashmind.feature.deck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmind.core.model.InsightPriority
import com.example.flashmind.core.model.StudyCoachSnapshot
import com.example.flashmind.core.model.VocabularyCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StudyCoachPanel(
    snapshot: StudyCoachSnapshot,
    onOpenLearnMode: () -> Unit,
    onFilterStarred: () -> Unit,
) {
    val radarAlpha = animateFloatAsState(
        targetValue = 0.88f + (snapshot.readinessScore / 100f) * 0.12f,
        label = "radarAlpha",
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AndroidView(
                    modifier = Modifier
                        .size(108.dp)
                        .alpha(radarAlpha.value),
                    factory = { context -> StudyRadarView(context) },
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Trợ lý AI trên thiết bị",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DeckTextColor,
                    )
                    Text(
                        text = snapshot.focusBand,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF5D5568),
                    )
                    AnimatedContent(
                        targetState = snapshot.readinessScore,
                        label = "readinessScore",
                    ) { score ->
                        Text(
                            text = "Độ sẵn sàng $score",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DeckTextColor,
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CoachMetricChip("Cần học ngay", snapshot.urgentCards)
                CoachMetricChip("Thẻ khó", snapshot.hardCards)
                CoachMetricChip("Gắn sao", snapshot.starredCards)
            }

            AnimatedVisibility(
                visible = snapshot.insights.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    snapshot.insights.forEach { insight ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EEE1)),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(priorityColor(insight.priority), CircleShape),
                                    )
                                    Text(
                                        text = insight.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeckTextColor,
                                    )
                                }
                                Text(
                                    text = insight.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF5A5248),
                                )
                                if (insight.actionLabel == "Filter starred") {
                                    TextAction("Lọc thẻ gắn sao", onFilterStarred)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachMetricChip(
    label: String,
    value: Int,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E2D6)),
        shape = RoundedCornerShape(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = Color(0xFF5D5568))
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = DeckTextColor)
        }
    }
}

@Composable
internal fun DeckRecyclerSection(
    cards: List<VocabularyCard>,
    onEditCard: (VocabularyCard) -> Unit,
    onDeleteCard: (String) -> Unit,
    onToggleCardStar: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Turbo list",
                style = MaterialTheme.typography.titleLarge,
                color = DeckTextColor,
            )
            Text(
                text = "RecyclerView + DiffUtil + stable IDs for large datasets and smoother updates.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5A5248),
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 460.dp),
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        setHasFixedSize(true)
                        itemAnimator = null
                        isNestedScrollingEnabled = true
                        adapter = DeckCardsAdapter(
                            onEditCard = onEditCard,
                            onDeleteCard = onDeleteCard,
                            onToggleCardStar = onToggleCardStar,
                        )
                    }
                },
                update = { recyclerView ->
                    (recyclerView.adapter as DeckCardsAdapter).submitList(cards)
                },
            )
        }
    }
}

private class DeckCardsAdapter(
    private val onEditCard: (VocabularyCard) -> Unit,
    private val onDeleteCard: (String) -> Unit,
    private val onToggleCardStar: (String) -> Unit,
) : ListAdapter<VocabularyCard, DeckCardSnapshotViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckCardSnapshotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_snapshot, parent, false)
        return DeckCardSnapshotViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckCardSnapshotViewHolder, position: Int) {
        holder.bind(
            card = getItem(position),
            onEditCard = onEditCard,
            onDeleteCard = onDeleteCard,
            onToggleCardStar = onToggleCardStar,
        )
    }

    override fun getItemId(position: Int): Long = getItem(position).id.hashCode().toLong()

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<VocabularyCard>() {
            override fun areItemsTheSame(oldItem: VocabularyCard, newItem: VocabularyCard): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: VocabularyCard, newItem: VocabularyCard): Boolean =
                oldItem == newItem
        }
    }
}

private class DeckCardSnapshotViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.cardTitle)
    private val bodyView: TextView = itemView.findViewById(R.id.cardBody)
    private val metaView: TextView = itemView.findViewById(R.id.cardMeta)
    private val actionEdit: TextView = itemView.findViewById(R.id.actionEdit)
    private val actionStar: TextView = itemView.findViewById(R.id.actionStar)
    private val actionDelete: TextView = itemView.findViewById(R.id.actionDelete)
    private val accentView: View = itemView.findViewById(R.id.cardAccent)

    fun bind(
        card: VocabularyCard,
        onEditCard: (VocabularyCard) -> Unit,
        onDeleteCard: (String) -> Unit,
        onToggleCardStar: (String) -> Unit,
    ) {
        titleView.text = card.front
        bodyView.text = card.back
        metaView.text = buildString {
            append("Ease ")
            append(card.progress.easeFactor)
            append(" • Interval ")
            append(card.progress.intervalDays)
            append("d")
        }
        actionEdit.setOnClickListener { onEditCard(card) }
        actionStar.text = if (card.isStarred) "Unstar" else "Star"
        actionStar.setOnClickListener { onToggleCardStar(card.id) }
        actionDelete.setOnClickListener { onDeleteCard(card.id) }
        accentView.setBackgroundColor(
            if (card.isStarred) 0xFFFF8A5B.toInt() else 0xFF8A6BFF.toInt(),
        )
        itemView.setOnClickListener { onEditCard(card) }
    }
}

private fun priorityColor(priority: InsightPriority): Color {
    return when (priority) {
        InsightPriority.HIGH -> Color(0xFFFF6B4A)
        InsightPriority.MEDIUM -> Color(0xFF1F6B70)
        InsightPriority.LOW -> Color(0xFF8F5E3B)
    }
}
