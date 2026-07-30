# Katalian Banking — Mobile (Android, QA)

A native **Kotlin + Jetpack Compose** Android application that mirrors the functionality of the
[Katalian-Banking-QA](https://github.com/kevinjackey-katalon/Katalian-Banking-QA) web app
(React + TypeScript + Vite).

## Feature parity with the web app

| Web app (`components/screens`) | Android screen | Notes |
|---|---|---|
| `LoginScreen.tsx` | `LoginScreen.kt` | Same mock credential check (`bankinguser123` / `notapassword@123`, and a locked demo user `lockedout25`) |
| `PasswordResetScreen.tsx` | `PasswordResetScreen.kt` | Simulated reset flow |
| `DashboardScreen.tsx` | `DashboardScreen.kt` | Net liquidity hero, account cards, apply-for-product tiles, quick links |
| `AccountDetailsScreen.tsx` | `AccountDetailsScreen.kt` | Transaction ledger, month filter, **PDF statement export** (Android `PdfDocument` API replacing jsPDF) |
| `TransferScreen.tsx` | `TransferScreen.kt` | Same validation rules (self-transfer, insufficient funds, credit-card payment shortcuts) |
| `DepositScreen.tsx` | `DepositScreen.kt` | Same 3-step ACH/Check flow |
| `LoansScreen.tsx` | `LoansScreen.kt` | Same 3 loan products/rates |
| `LoanApplicationScreen.tsx` | `LoanApplicationScreen.kt` | Same 3-step loan application |
| `ApplicationScreen.tsx` | `ApplicationScreen.kt` | Same identity/residence/funding steps + validation |
| `SecurityScreen.tsx` | `SecurityScreen.kt` | Report / Freeze-All / Nuclear Lockdown flows |
| `ContactScreen.tsx` | `ContactScreen.kt` | Concierge chat with the same canned keyword responses |
| `DocumentLibraryScreen.tsx` | `DocumentLibraryScreen.kt` | Generates the same generic Loan Request Form PDF |
| `AiAssistant.tsx` | `AiAssistantWidget.kt` + `GeminiAssistant.kt` | Floating "Ask AI Assistant" — calls the Gemini API if configured, otherwise an offline fallback |

`AdminScreen.tsx` (the internal debug/API-docs panel) was **not** ported, since it isn't
customer-facing banking functionality — let us know if you'd like that mirrored too.

## Architecture

- **Data models** (`data/Models.kt`) mirror `types.ts` exactly.
- **Mock data** (`data/MockData.kt`) mirrors `constants.ts` — identical seed users, account
  numbers, and balances.
- **Mock API** (`data/MockApi.kt`) mirrors `api/mockApi.ts`, including the same simulated
  network delays.
- **Persistence** (`data/UserRepository.kt`) uses Jetpack DataStore to mirror the web app's
  `localStorage` (`katalian_users_v1` / `katalian_session_v1`).
- **State/business logic** (`viewmodel/BankViewModel.kt`) mirrors the handlers in `App.tsx`
  (login, logout, transfer, deposit, security actions, applications).
- **Navigation** (`navigation/NavGraph.kt`) mirrors the React Router routes in `App.tsx`.

## Running locally

**Prerequisites:** Android Studio (Koala or newer), JDK 17.

1. Open this project in Android Studio and let Gradle sync (it will fetch the wrapper JAR
   automatically; if not, run `gradle wrapper` once with a local Gradle install).
2. *(Optional)* To enable the live Gemini-powered AI Assistant, add to `local.properties`:
   ```
   GEMINI_API_KEY=your_key_here
   ```
   Without a key, the assistant falls back to built-in offline responses.
3. Run the `app` configuration on an emulator or device (minSdk 24).

### Demo credentials
- `bankinguser123` / `notapassword@123` — active user with Checking, Savings, and Credit Card accounts
- `lockedout25` / `lockedoutpassword343` — locked account (demonstrates the "Account locked" error state)
