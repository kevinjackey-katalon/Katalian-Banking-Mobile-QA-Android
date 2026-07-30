package com.katalon.katalianbanking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.katalon.katalianbanking.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BankUiState(
    val users: List<User> = emptyList(),
    val currentUser: User? = null,
    val isInitializing: Boolean = true
)

/**
 * Central view model mirroring the state + handlers owned by App.tsx in the web app:
 * users/currentUser state, localStorage-equivalent persistence, and all the
 * account/transfer/deposit/loan/security mutation handlers.
 */
class BankViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _uiState = MutableStateFlow(BankUiState())
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val users = repository.loadUsers()
            val sessionId = repository.loadSessionUserId()
            val current = users.find { it.id == sessionId }
            _uiState.value = BankUiState(users = users, currentUser = current, isInitializing = false)
        }
    }

    private fun persistUsers(users: List<User>) {
        viewModelScope.launch { repository.saveUsers(users) }
    }

    private fun persistSession(userId: String?) {
        viewModelScope.launch { repository.saveSessionUserId(userId) }
    }

    /** Mirrors handleLogin in App.tsx. */
    fun login(username: String, password: String): LoginResult {
        val user = _uiState.value.users.find { it.username == username }
            ?: return LoginResult.Invalid
        if (user.locked) return LoginResult.Locked
        if (user.passwordHash != password) return LoginResult.Invalid

        _uiState.value = _uiState.value.copy(currentUser = user)
        persistSession(user.id)
        return LoginResult.Success
    }

    /** Mirrors handleLogout in App.tsx. */
    fun logout() {
        _uiState.value = _uiState.value.copy(currentUser = null)
        persistSession(null)
    }

    private fun updateCurrentUser(updated: User) {
        val newUsers = _uiState.value.users.map { if (it.id == updated.id) updated else it }
        _uiState.value = _uiState.value.copy(currentUser = updated, users = newUsers)
        persistUsers(newUsers)
    }

    /** Mirrors handleSecurityAction in App.tsx. */
    fun performSecurityAction(action: SecurityAction) {
        val current = _uiState.value.currentUser ?: return
        when (action) {
            SecurityAction.Lockdown -> {
                val updatedUser = current.copy(locked = true)
                val newUsers = _uiState.value.users.map { if (it.id == current.id) updatedUser else it }
                _uiState.value = _uiState.value.copy(users = newUsers, currentUser = null)
                persistUsers(newUsers)
                persistSession(null)
            }
            SecurityAction.FreezeAll -> {
                val updatedAccounts = current.accounts.map { acc ->
                    if (acc.type.isCard || acc.type == AccountType.Checking) acc.copy(status = AccountStatus.Frozen) else acc
                }
                updateCurrentUser(current.copy(accounts = updatedAccounts))
            }
            SecurityAction.Report -> { /* no state mutation, mirrors web app's 'report' case (navigate only) */ }
        }
    }

    /** Mirrors handleApplicationSubmit in App.tsx. */
    suspend fun submitApplication(accountType: AccountType, appData: ApplicationData) {
        val current = _uiState.value.currentUser ?: return
        val newAccount = MockApi.submitApplication(accountType, appData)
        updateCurrentUser(current.copy(accounts = current.accounts + newAccount))
    }

    /** Mirrors handleLoanSubmit in App.tsx. */
    suspend fun submitLoanApplication(loanType: LoanType, loanData: LoanApplicationData) {
        val current = _uiState.value.currentUser ?: return
        val newLoan = MockApi.submitLoanApplication(loanType, loanData)
        updateCurrentUser(current.copy(loans = current.loans + newLoan))
    }

    /** Mirrors handleTransfer in App.tsx. */
    suspend fun transfer(fromAccountId: String, toAccountId: String, amount: Double) {
        val current = _uiState.value.currentUser ?: return
        MockApi.executeTransfer(fromAccountId, toAccountId, amount)

        val fromAcc = current.accounts.find { it.id == fromAccountId } ?: return
        val toAcc = current.accounts.find { it.id == toAccountId } ?: return

        val updatedAccounts = current.accounts.map { acc ->
            when (acc.id) {
                fromAccountId -> acc.copy(balance = acc.balance - amount)
                toAccountId -> {
                    val isCreditCard = acc.type.isCard
                    acc.copy(balance = if (isCreditCard) acc.balance - amount else acc.balance + amount)
                }
                else -> acc
            }
        }
        updateCurrentUser(current.copy(accounts = updatedAccounts))
    }

    /** Mirrors handleDeposit in App.tsx. */
    suspend fun deposit(toAccountId: String, amount: Double) {
        val current = _uiState.value.currentUser ?: return
        MockApi.executeDeposit(toAccountId, amount)
        val updatedAccounts = current.accounts.map { acc ->
            if (acc.id == toAccountId) acc.copy(balance = acc.balance + amount) else acc
        }
        updateCurrentUser(current.copy(accounts = updatedAccounts))
    }
}
