package com.example.flashmind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flashmind.auth.AuthScreen
import com.example.flashmind.auth.AuthViewModel
import com.example.flashmind.feature.deck.CreateDeckScreen
import com.example.flashmind.feature.deck.CreateDeckViewModel
import com.example.flashmind.feature.deck.AiCreateDeckScreen
import com.example.flashmind.feature.deck.AiCreateDeckViewModel
import com.example.flashmind.feature.deck.DeckChatScreen
import com.example.flashmind.feature.deck.DeckChatViewModel
import com.example.flashmind.feature.deck.DeckDetailScreen
import com.example.flashmind.feature.deck.DeckDetailViewModel
import com.example.flashmind.feature.deck.DeckListScreen
import com.example.flashmind.feature.deck.DeckListViewModel
import com.example.flashmind.feature.deck.ImportDeckScreen
import com.example.flashmind.feature.deck.ImportDeckViewModel
import com.example.flashmind.feature.review.LearnModeScreen
import com.example.flashmind.feature.review.LearnModeViewModel
import com.example.flashmind.feature.review.MatchModeScreen
import com.example.flashmind.feature.review.MatchModeViewModel
import com.example.flashmind.feature.review.ReviewScreen
import com.example.flashmind.feature.review.ReviewViewModel
import com.example.flashmind.feature.review.TestModeScreen
import com.example.flashmind.feature.review.TestModeViewModel
import com.example.flashmind.settings.AppSettingsViewModel

@Composable
fun FlashMindNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val settingsViewModel: AppSettingsViewModel = hiltViewModel()
    val authState = authViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState = settingsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.value.ready, authState.value.isAuthenticated) {
        if (!authState.value.ready) return@LaunchedEffect
        val targetRoute = if (authState.value.isAuthenticated) "decks" else "auth"
        if (navController.currentDestination?.route != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.value.isAuthenticated) "decks" else "auth",
    ) {
        composable("auth") {
            AuthScreen(
                state = authState,
                onEmailChanged = authViewModel::onEmailChanged,
                onPasswordChanged = authViewModel::onPasswordChanged,
                onConfirmPasswordChanged = authViewModel::onConfirmPasswordChanged,
                onSwitchMode = authViewModel::switchMode,
                onSubmit = authViewModel::submit,
            )
        }
        composable("decks") {
            val viewModel: DeckListViewModel = hiltViewModel()
            DeckListScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                currentUserEmail = authState.value.currentUserEmail,
                useDarkTheme = settingsState.value.useDarkTheme,
                reminderEnabled = settingsState.value.reminderEnabled,
                reminderHour = settingsState.value.reminderHour,
                reminderMinute = settingsState.value.reminderMinute,
                onOpenDeck = { navController.navigate("deck/$it") },
                onCreateDeck = { navController.navigate("createDeck") },
                onAiCreateDeck = { navController.navigate("aiCreateDeck") },
                onImportDeck = { navController.navigate("importDeck") },
                onDeleteDeck = viewModel::deleteDeck,
                onLogout = authViewModel::logout,
                onSyncNow = viewModel::syncNow,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSortOrderChanged = viewModel::onSortOrderChanged,
                onDarkThemeChanged = settingsViewModel::setDarkTheme,
                onReminderEnabledChanged = settingsViewModel::setReminderEnabled,
                onReminderHourShift = settingsViewModel::shiftReminderHour,
                onReminderMinuteShift = settingsViewModel::shiftReminderMinute,
            )
        }
        composable("importDeck") {
            val viewModel: ImportDeckViewModel = hiltViewModel()
            ImportDeckScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onJsonChanged = viewModel::onJsonChanged,
                onBack = { navController.popBackStack() },
                onImport = {
                    viewModel.importDeck {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable("createDeck") {
            val viewModel: CreateDeckViewModel = hiltViewModel()
            CreateDeckScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onTitleChanged = viewModel::onTitleChanged,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onBack = { navController.popBackStack() },
                onSave = {
                    viewModel.createDeck {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable("aiCreateDeck") {
            val viewModel: AiCreateDeckViewModel = hiltViewModel()
            AiCreateDeckScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onTopicChanged = viewModel::onTopicChanged,
                onBack = { navController.popBackStack() },
                onCreate = {
                    viewModel.createDeck {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable("deck/{deckId}") {
            val viewModel: DeckDetailViewModel = hiltViewModel()
            DeckDetailScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onStartReview = { navController.navigate("review/$it") },
                onStartTest = { navController.navigate("test/$it") },
                onStartLearn = { navController.navigate("learn/$it") },
                onStartMatch = { navController.navigate("match/$it") },
                onOpenChat = { navController.navigate("chat/$it") },
                onAddCard = viewModel::startCreateCard,
                onEditCard = viewModel::startEditCard,
                onDeleteCard = viewModel::deleteCard,
                onToggleCardStar = viewModel::toggleCardStar,
                onBack = { navController.popBackStack() },
                onEditDeck = viewModel::startEditDeck,
                onFrontChanged = viewModel::onFrontChanged,
                onBackChanged = viewModel::onBackChanged,
                onPronunciationChanged = viewModel::onPronunciationChanged,
                onExampleChanged = viewModel::onExampleChanged,
                onImageUrlChanged = viewModel::onImageUrlChanged,
                onCardSearchQueryChanged = viewModel::onCardSearchQueryChanged,
                onCardFilterChanged = viewModel::onCardFilterChanged,
                onDeckTitleChanged = viewModel::onDeckTitleChanged,
                onDeckDescriptionChanged = viewModel::onDeckDescriptionChanged,
                onSaveDeck = viewModel::saveDeck,
                onDismissDeckEditor = viewModel::dismissDeckEditor,
                onSaveCard = viewModel::saveCard,
                onDismissEditor = viewModel::dismissEditor,
            )
        }
        composable("chat/{deckId}") {
            val viewModel: DeckChatViewModel = hiltViewModel()
            DeckChatScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onBack = { navController.popBackStack() },
                onPromptChanged = viewModel::onPromptChanged,
                onSend = viewModel::send,
                onQuickPrompt = viewModel::useQuickPrompt,
            )
        }
        composable("review/{deckId}") {
            val viewModel: ReviewViewModel = hiltViewModel()
            ReviewScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onGrade = viewModel::submitGrade,
                onBack = { navController.popBackStack() },
                onRevealAnswer = viewModel::revealAnswer,
            )
        }
        composable("test/{deckId}") {
            val viewModel: TestModeViewModel = hiltViewModel()
            TestModeScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onBack = { navController.popBackStack() },
                onPracticeWrongOnlyChanged = viewModel::setPracticeWrongOnly,
                onAnswer = viewModel::submitAnswer,
                onNext = viewModel::nextQuestion,
                onRestart = viewModel::restart,
            )
        }
        composable("learn/{deckId}") {
            val viewModel: LearnModeViewModel = hiltViewModel()
            LearnModeScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onBack = { navController.popBackStack() },
                onPracticeWrongOnlyChanged = viewModel::setPracticeWrongOnly,
                onAnswerChanged = viewModel::onAnswerChanged,
                onCheckAnswer = viewModel::checkAnswer,
                onRevealAnswer = viewModel::revealAnswer,
                onNext = viewModel::nextCard,
                onRestart = viewModel::restart,
            )
        }
        composable("match/{deckId}") {
            val viewModel: MatchModeViewModel = hiltViewModel()
            MatchModeScreen(
                state = viewModel.uiState.collectAsStateWithLifecycle(),
                onBack = { navController.popBackStack() },
                onSelectPrompt = viewModel::selectPrompt,
                onSelectAnswer = viewModel::selectAnswer,
                onRestart = viewModel::restart,
            )
        }
    }
}
