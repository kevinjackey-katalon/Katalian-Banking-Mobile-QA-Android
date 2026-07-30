package com.katalon.katalianbanking.data

import kotlinx.coroutines.delay
import kotlin.random.Random

/** Mirrors api/mockApi.ts - simulated network calls with the same artificial delays. */
object MockApi {

    suspend fun getUsers(): List<User> {
        delay(500)
        return seedUsers()
    }

    suspend fun submitApplication(accountType: AccountType, appData: ApplicationData): Account {
        delay(1500)
        return Account(
            id = "acc-${Random.nextInt(100000, 999999)}",
            type = accountType,
            accountNumber = "...${Random.nextInt(1000, 9999)}",
            balance = appData.initialDeposit ?: 0.0,
            status = if (accountType.isCard) AccountStatus.Pending else AccountStatus.Active,
            transactions = emptyList()
        )
    }

    suspend fun executeTransfer(fromId: String, toId: String, amount: Double): Boolean {
        delay(800)
        return true
    }

    suspend fun executeDeposit(toId: String, amount: Double): Boolean {
        delay(1200)
        return true
    }

    suspend fun submitLoanApplication(loanType: LoanType, loanData: LoanApplicationData): Loan {
        delay(2000)
        val rate = when (loanType) {
            LoanType.Mortgage -> 6.45
            LoanType.Auto -> 4.25
            LoanType.Personal -> 5.99
        }
        return Loan(
            id = "loan-${Random.nextInt(100000, 999999)}",
            type = loanType,
            amount = loanData.loanAmount,
            interestRate = rate,
            status = LoanStatus.Pending,
            termMonths = loanData.loanTerm
        )
    }
}
