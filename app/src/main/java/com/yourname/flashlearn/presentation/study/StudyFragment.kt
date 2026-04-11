package com.yourname.flashlearn.presentation.study

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yourname.flashlearn.databinding.FragmentStudyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class StudyFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudyViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var currentText = ""
    private var isRepeating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        tts = TextToSpeech(requireContext(), this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        setupButtons()
    }

    private val choiceButtons by lazy {
        listOf(binding.btnChoice1, binding.btnChoice2,
            binding.btnChoice3, binding.btnChoice4)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isFinished) {
                    showFinishDialog(state)
                    return@collectLatest
                }
                val card = state.currentCard ?: return@collectLatest

                binding.progressStudy.progress = state.progress
                binding.tvProgress.text = "${state.studiedCards}/${state.totalCards}"

                if (state.isFlipped) {
                    binding.tvCardLabel.text = "ĐÁP ÁN"
                    binding.tvCardContent.text = card.back
                    currentText = card.back
                    binding.layoutAnswerButtons.visibility = View.VISIBLE
                    binding.tvHintTap.visibility = View.GONE
                    binding.layoutQuizButtons.visibility = View.GONE
                } else {
                    binding.tvCardLabel.text = "CÂU HỎI"
                    binding.tvCardContent.text = card.front
                    currentText = card.front
                    binding.layoutAnswerButtons.visibility = View.GONE
                    binding.tvHintTap.visibility = View.GONE

                    // Hiện quiz nếu có đủ 4 đáp án
                    if (state.quizChoices.size == 4) {
                        binding.layoutQuizButtons.visibility = View.VISIBLE
                        choiceButtons.forEachIndexed { i, btn ->
                            btn.text = state.quizChoices[i]
                            btn.isEnabled = true
                            btn.backgroundTintList = null
                        }
                    } else {
                        binding.layoutQuizButtons.visibility = View.GONE
                        binding.tvHintTap.visibility = View.VISIBLE
                    }
                }
                stopRepeating()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.elapsedSeconds.collectLatest { seconds ->
                binding.tvTimer.text = "⏱ ${seconds}s"
                binding.tvTimer.setTextColor(
                    if (seconds > 10) Color.RED
                    else Color.parseColor("#4CAF50")
                )
            }
        }
    }

    private fun setupButtons() {
        binding.toolbarStudy.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardFlashcard.setOnClickListener { viewModel.flipCard() }

        binding.btnTts.setOnClickListener {
            if (isRepeating) stopRepeating() else speakOnce()
        }
        binding.btnTts.setOnLongClickListener {
            if (isRepeating) stopRepeating() else startRepeating()
            true
        }

        binding.btnAgain.setOnClickListener { viewModel.submitAnswer(1) }
        binding.btnHard.setOnClickListener { viewModel.submitAnswer(2) }
        binding.btnGood.setOnClickListener { viewModel.submitAnswer(4) }
        binding.btnEasy.setOnClickListener { viewModel.submitAnswer(5) }

        // Quiz choice buttons
        choiceButtons.forEach { btn ->
            btn.setOnClickListener {
                val correct = viewModel.uiState.value.correctAnswer
                choiceButtons.forEach { it.isEnabled = false }

                if (btn.text == correct) {
                    btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    btn.postDelayed({ viewModel.submitAnswer(4) }, 800)
                } else {
                    btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
                    choiceButtons.find { it.text == correct }
                        ?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    btn.postDelayed({ viewModel.submitAnswer(1) }, 1200)
                }
            }
        }
    }

    private fun speakOnce() {
        if (!ttsReady || currentText.isEmpty()) return
        tts?.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, "SINGLE")
        binding.btnTts.text = "🔊 Đang đọc..."
        binding.btnTts.postDelayed({
            if (!isRepeating) binding.btnTts.text = "🔊 Nghe phát âm"
        }, 2000)
    }

    private fun startRepeating() {
        if (!ttsReady || currentText.isEmpty()) return
        isRepeating = true
        binding.btnTts.text = "⏹ Dừng lặp"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(u: String?) {}
            override fun onDone(u: String?) {
                if (isRepeating && u == "REPEAT") {
                    binding.btnTts.postDelayed({
                        if (isRepeating) tts?.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, "REPEAT")
                    }, 1000)
                }
            }
            override fun onError(u: String?) {}
        })
        tts?.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, "REPEAT")
    }

    private fun stopRepeating() {
        isRepeating = false
        tts?.stop()
        if (_binding != null) binding.btnTts.text = "🔊 Nghe phát âm"
    }

    private fun showFinishDialog(state: StudyUiState) {
        stopRepeating()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🎉 Hoàn thành!")
            .setMessage("Đã học: ${state.studiedCards} thẻ\n✅ Đúng: ${state.good}  ❌ Sai: ${state.again}")
            .setPositiveButton("OK") { _, _ -> findNavController().popBackStack() }
            .setCancelable(false)
            .show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
            tts?.setSpeechRate(0.8f)
            ttsReady = true
        }
    }

    override fun onDestroyView() {
        stopRepeating()
        tts?.stop()
        tts?.shutdown()
        _binding = null
        super.onDestroyView()
    }
}