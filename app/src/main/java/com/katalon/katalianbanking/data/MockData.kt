package com.katalon.katalianbanking.data

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/** Mirrors generateMockTransactions() in constants.ts */
private fun generateMockTransactions(count: Int, baseDescription: String): List<Transaction> {
    return (1..count).map { i ->
        val isCredit = Random.nextDouble() > 0.6
        val monthsBack = Random.nextInt(0, 4).toLong()
        val date = Instant.now().minus(monthsBack * 30, ChronoUnit.DAYS)
        Transaction(
            id = "tx-${Random.nextInt(100000, 999999)}",
            date = date.toString(),
            description = "$baseDescription ${i}",
            amount = String.format("%.2f", Random.nextDouble(10.0, 510.0)).toDouble(),
            type = if (isCredit) TransactionType.Credit else TransactionType.Debit,
            category = if (isCredit) "Income" else "General"
        )
    }
}

/** Mirrors USERS in constants.ts - same seed data (account numbers, balances, credentials). */
fun seedUsers(): List<User> = listOf(
    User(
        id = "user1",
        username = "bankinguser123",
        passwordHash = "notapassword@123",
        locked = false,
        canApplyForPlatinum = true,
        accounts = listOf(
            Account(
                id = "acc1-1",
                type = AccountType.Checking,
                accountNumber = "...7890",
                balance = 5345.54,
                transactions = listOf(
                    Transaction("tx1", "2025-05-10T10:00:00Z", "Apple Store Cupertino", 1299.00, TransactionType.Debit, "Technology"),
                    Transaction("tx2", "2025-05-08T14:30:00Z", "Katalian Payroll Deposit", 4500.00, TransactionType.Credit, "Salary"),
                    Transaction("tx3", "2025-04-25T12:00:00Z", "Whole Foods Market", 156.43, TransactionType.Debit, "Groceries")
                ) + generateMockTransactions(12, "Point of Sale")
            ),
            Account(
                id = "acc1-2",
                type = AccountType.Savings,
                accountNumber = "...1234",
                balance = 104456.67,
                transactions = listOf(
                    Transaction("tx4", "2025-05-01T00:00:00Z", "Interest Credit", 456.67, TransactionType.Credit, "Interest")
                ) + generateMockTransactions(5, "Internal Transfer")
            ),
            Account(
                id = "acc1-3",
                type = AccountType.CreditCard,
                accountNumber = "...9921",
                balance = 1250.00,
                transactions = listOf(
                    Transaction("tx5", "2025-05-12T10:00:00Z", "Gas Station X", 55.00, TransactionType.Debit, "Transport")
                ) + generateMockTransactions(8, "Merchant Purchase")
            )
        ),
        loans = emptyList()
    ),
    User(
        id = "user4",
        username = "lockedout25",
        passwordHash = "lockedoutpassword343",
        unlockPasswordHash = "resetpassword@45",
        locked = true,
        canApplyForPlatinum = false,
        accounts = listOf(
            Account(
                id = "acc4-1",
                type = AccountType.Checking,
                accountNumber = "...3456",
                balance = 12.14,
                transactions = generateMockTransactions(3, "Emergency Withdrawal")
            )
        ),
        loans = emptyList()
    )
)

/** Mirrors STATES in constants.ts */
val STATES = listOf(
    "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware",
    "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky",
    "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi",
    "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico",
    "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania",
    "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont",
    "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"
)

/** Mirrors LOAN_PRODUCTS in constants.ts */
data class LoanProduct(val type: LoanType, val rate: String, val description: String, val icon: String)

val LOAN_PRODUCTS = listOf(
    LoanProduct(LoanType.Personal, "5.99%", "Flexible funds for life's unexpected moments.", "\uD83D\uDCB0"),
    LoanProduct(LoanType.Auto, "4.25%", "Get behind the wheel of your dream car faster.", "\uD83D\uDE97"),
    LoanProduct(LoanType.Mortgage, "6.45%", "Your journey to home ownership starts here.", "\uD83C\uDFE0")
)
