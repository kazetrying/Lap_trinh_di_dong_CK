package com.example.flashmind.auth

import com.example.flashmind.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authSessionStore: AuthSessionStore = mock()
    private val sessionFlow = MutableStateFlow<AuthSession?>(null)

    @Before
    fun setUp() {
        whenever(authSessionStore.sessionFlow).thenReturn(sessionFlow)
    }

    @Test
    fun `register mode rejects mismatched passwords`() = runTest {
        val viewModel = AuthViewModel(authSessionStore)
        advanceUntilIdle()

        viewModel.switchMode(true)
        viewModel.onEmailChanged("dev@flashmind.ai")
        viewModel.onPasswordChanged("123456")
        viewModel.onConfirmPasswordChanged("654321")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Passwords do not match.", viewModel.uiState.value.error)
        verify(authSessionStore, never()).register("dev@flashmind.ai", "123456")
    }

    @Test
    fun `login failure exposes error state`() = runTest {
        whenever(authSessionStore.login("dev@flashmind.ai", "123456")).thenReturn(false)
        val viewModel = AuthViewModel(authSessionStore)
        advanceUntilIdle()

        viewModel.onEmailChanged("dev@flashmind.ai")
        viewModel.onPasswordChanged("123456")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Invalid email or password.", viewModel.uiState.value.error)
    }

    @Test
    fun `session flow updates authentication state`() = runTest {
        val viewModel = AuthViewModel(authSessionStore)
        sessionFlow.value = AuthSession(
            email = "owner@flashmind.ai",
            token = "remote-token",
            isRemote = true,
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals("owner@flashmind.ai", viewModel.uiState.value.currentUserEmail)
    }
}
