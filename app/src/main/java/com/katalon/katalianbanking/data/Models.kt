package com.katalon.katalianbanking.data

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType { Credit, Debit }

@Serializable
data class Transaction(
    val id: String,
    val date: String, // ISO-8601
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: String
)

@Serializable
enum class AccountType(val label: String) {
    Checking("Checking"),
    Savings("Savings"),
    CreditCard("Credit Card"),
    PlatinumCreditCard("Platinum Credit Card");

    val isCard: Boolean get() = this == CreditCard || this == PlatinumCreditCard
}

@Serializable
enum class AccountStatus { Pending, Active, Frozen }

@Serializable
data class Account(
    val id: String,
    val type: AccountType,
    val accountNumber: String,
    val balance: Double,
    val status: AccountStatus? = null,
    val transactions: List<Transaction> = emptyList()
)

@Serializable
data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val accounts: List<Account> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val canApplyForPlatinum: Boolean = false,
    val locked: Boolean = false,
    val unlockPasswordHash: String? = null
)

@Serializable
enum class LoanType(val label: String) {
    Personal("Personal"),
    Auto("Auto"),
    Mortgage("Mortgage")
}

@Serializable
enum class LoanStatus { Pending, Approved, Active }

@Serializable
data class Loan(
    val id: String,
    val type: LoanType,
    val amount: Double,
    val interestRate: Double,
    val status: LoanStatus,
    val termMonths: Int
)

/** Mirrors ApplicationData from types.ts - used for new account (Checking/Savings/Credit) applications. */
data class ApplicationData(
    var firstName: String = "",
    var middleName: String = "",
    var lastName: String = "",
    var dob: String = "",
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var zip: String = "",
    var initialDeposit: Double? = null,
    var depositFromAccountId: String? = null
)

/** Mirrors LoanApplicationData from types.ts - extends ApplicationData with employment/loan fields. */
data class LoanApplicationData(
    var firstName: String = "",
    var lastName: String = "",
    var dob: String = "",
    var address: String = "",
    var employer: String = "",
    var jobTitle: String = "",
    var annualIncome: Double = 0.0,
    var loanAmount: Double = 0.0,
    var loanTerm: Int = 12,
    var purpose: String = ""
)

enum class SecurityAction { Report, Lockdown, FreezeAll }

/** Login outcome, mirrors the 'success' | 'locked' | 'invalid' union in App.tsx. */
enum class LoginResult { Success, Locked, Invalid }
