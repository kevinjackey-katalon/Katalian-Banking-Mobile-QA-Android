package com.katalon.katalianbanking.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.katalon.katalianbanking.data.AccountType
import com.katalon.katalianbanking.data.LoanType
import com.katalon.katalianbanking.data.SecurityAction
import com.katalon.katalianbanking.ui.components.AiAssistantWidget
import com.katalon.katalianbanking.ui.components.KTopBar
import com.katalon.katalianbanking.ui.screens.*
import com.katalon.katalianbanking.viewmodel.BankViewModel

private object Routes {
    const val LOGIN = "login"
    const val RESET_PASSWORD = "reset_password"
    const val DASHBOARD = "dashboard"
    const val DOCUMENT_LIBRARY = "document_library"
    const val ACCOUNT_DETAILS = "account/{accountId}"
    const val TRANSFER = "transfer"
    const val DEPOSIT = "deposit"
    const val LOANS = "loans"
    const val CONTACT = "contact"
    const val SECURITY = "security/{action}"
    const val APPLY = "apply/{accountType}"
    const val APPLY_LOAN = "apply_loan/{loanType}"
}

@Composable
fun KatalianNavGraph() {
    val navController: NavHostController = rememberNavController()
    val viewModel: BankViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    if (state.isInitializing) return

    // Mirrors App.tsx: redirect to dashboard or login based on session presence.
    LaunchedEffect(state.currentUser) {
        val destination = navController.currentDestination?.route
        if (state.currentUser != null && (destination == Routes.LOGIN || destination == null)) {
            navController.navigate(Routes.DASHBOARD) { popUpTo(0) }
        }
    }

    val startDestination = if (state.currentUser != null) Routes.DASHBOARD else Routes.LOGIN

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLogin = { u, p -> viewModel.login(u, p) },
                    onLoginSuccess = { navController.navigate(Routes.DASHBOARD) { popUpTo(0) } },
                    onForgotPassword = { navController.navigate(Routes.RESET_PASSWORD) }
                )
            }
            composable(Routes.RESET_PASSWORD) {
                PasswordResetScreen(onBackToLogin = { navController.popBackStack() })
            }
            composable(Routes.DASHBOARD) {
                val user = state.currentUser
                if (user != null) {
                    Box(Modifier.fillMaxSize()) {
                        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                            KTopBar(title = "Katalian", subtitle = "Portfolio", user = user, onLogout = {
                                viewModel.logout()
                                navController.navigate(Routes.LOGIN) { popUpTo(0) }
                            })
                            DashboardScreen(
                                user = user,
                                onOpenAccount = { id -> navController.navigate("account/$id") },
                                onTransfer = { navController.navigate(Routes.TRANSFER) },
                                onDeposit = { navController.navigate(Routes.DEPOSIT) },
                                onDocumentLibrary = { navController.navigate(Routes.DOCUMENT_LIBRARY) },
                                onLoans = { navController.navigate(Routes.LOANS) },
                                onFreezeAll = { navController.navigate("security/freeze-all") },
                                onContact = { navController.navigate(Routes.CONTACT) },
                                onApply = { type -> navController.navigate("apply/${type.name}") }
                            )
                        }
                        AiAssistantWidget(allUsers = state.users)
                    }
                }
            }
            composable(Routes.DOCUMENT_LIBRARY) {
                androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                    KTopBar(title = "Document Library", onBack = { navController.popBackStack() })
                    DocumentLibraryScreen()
                }
            }
            composable(
                Routes.ACCOUNT_DETAILS,
                arguments = listOf(navArgument("accountId") { type = NavType.StringType })
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")
                val account = state.currentUser?.accounts?.find { it.id == accountId }
                if (account != null) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                        KTopBar(title = "Account Ledger", onBack = { navController.popBackStack() })
                        AccountDetailsScreen(account = account)
                    }
                }
            }
            composable(Routes.TRANSFER) {
                val user = state.currentUser
                if (user != null) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                        KTopBar(title = "Transfer", onBack = { navController.popBackStack() })
                        TransferScreen(
                            user = user,
                            onNavigateBack = { navController.popBackStack() },
                            onTransfer = { from, to, amt -> viewModel.transfer(from, to, amt) }
                        )
                    }
                }
            }
            composable(Routes.DEPOSIT) {
                val user = state.currentUser
                if (user != null) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                        KTopBar(title = "Deposit", onBack = { navController.popBackStack() })
                        DepositScreen(
                            user = user,
                            onNavigateBack = { navController.popBackStack(Routes.DASHBOARD, false) },
                            onDeposit = { toId, amt -> viewModel.deposit(toId, amt) }
                        )
                    }
                }
            }
            composable(Routes.LOANS) {
                androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                    KTopBar(title = "Lending", onBack = { navController.popBackStack() })
                    LoansScreen(
                        onApplyLoan = { type -> navController.navigate("apply_loan/${type.name}") },
                        onContact = { navController.navigate(Routes.CONTACT) }
                    )
                }
            }
            composable(Routes.CONTACT) {
                androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                    KTopBar(title = "Support", onBack = { navController.popBackStack() })
                    ContactScreen(
                        onReportStolen = { navController.navigate("security/report") },
                        onLockdown = { navController.navigate("security/lockdown") }
                    )
                }
            }
            composable(
                Routes.SECURITY,
                arguments = listOf(navArgument("action") { type = NavType.StringType })
            ) { backStackEntry ->
                val actionArg = backStackEntry.arguments?.getString("action")
                val action = when (actionArg) {
                    "lockdown" -> SecurityAction.Lockdown
                    "freeze-all" -> SecurityAction.FreezeAll
                    else -> SecurityAction.Report
                }
                val user = state.currentUser
                if (user != null) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                        KTopBar(title = "Security", onBack = { navController.popBackStack() })
                        SecurityScreen(
                            user = user,
                            action = action,
                            onCancel = { navController.popBackStack() },
                            onComplete = { completedAction ->
                                viewModel.performSecurityAction(completedAction)
                                if (completedAction == SecurityAction.Lockdown) {
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                                } else {
                                    navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.DASHBOARD) { inclusive = true } }
                                }
                            }
                        )
                    }
                }
            }
            composable(
                Routes.APPLY,
                arguments = listOf(navArgument("accountType") { type = NavType.StringType })
            ) { backStackEntry ->
                val typeArg = backStackEntry.arguments?.getString("accountType")
                val accountType = try { AccountType.valueOf(typeArg ?: "") } catch (e: Exception) { null }
                val user = state.currentUser
                if (user != null && accountType != null) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                        KTopBar(title = "Open ${accountType.label}", onBack = { navController.popBackStack() })
                        ApplicationScreen(
                            user = user,
                            accountType = accountType,
                            onCancel = { navController.popBackStack(Routes.DASHBOARD, false) },
                            onSubmit = { type, data -> viewModel.submitApplication(type, data) }
                        )
                    }
                }
            }
            composable(
                Routes.APPLY_LOAN,
                arguments = listOf(navArgument("loanType") { type = NavType.StringType })
            ) { backStackEntry ->
                val typeArg = backStackEntry.arguments?.getString("loanType")
                val loanType = try { LoanType.valueOf(typeArg ?: "") } catch (e: Exception) { null }
                if (loanType != null) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                        KTopBar(title = "${loanType.label} Loan", onBack = { navController.popBackStack() })
                        LoanApplicationScreen(
                            loanType = loanType,
                            onCancel = { navController.popBackStack(Routes.LOANS, false) },
                            onSubmit = { type, data -> viewModel.submitLoanApplication(type, data) }
                        )
                    }
                }
            }
        }
    }
}
