# Splendid - Personal Finance Tracker

A modern, offline-first Android application for tracking expenses, managing budgets, and monitoring IOUs. Built with Kotlin, Jetpack Compose, and Room Database.

## Features

✅ **Expense Tracking**
- Add, edit, and delete expenses with quantity support
- Smart title suggestions (autocomplete with frequency ordering)
- Category-based organization with custom colors
- Daily spending totals

✅ **Budget Management**
- Set monthly budget limits
- Real-time budget status with warnings
- Over/under budget tracking

✅ **IOU System**
- Track money owed and receivable
- Pending and settled transactions
- Net balance calculation
- Completely isolated from expense tracking

✅ **Analytics**
- Weekly and monthly spending charts
- Spending breakdown by title (pie chart)
- Case-insensitive grouping to prevent duplicates

✅ **Calendar View**
- Monthly overview with highlighted expense dates
- Quick date navigation

✅ **Offline-First**
- All data stored locally with Room
- No internet connection required
- Privacy-focused design

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material3
- **Architecture:** MVVM + Clean Architecture (Data, Domain, UI layers)
- **Database:** Room (SQLite)
- **Navigation:** Jetpack Navigation Compose
- **Async:** Kotlin Coroutines & Flow
- **Preferences:** DataStore

## Project Structure

```
app/src/main/java/com/splendid/
├── data/
│   ├── local/
│   │   ├── entity/          # Room entities
│   │   ├── dao/             # Data Access Objects
│   │   └── database/        # Database configuration
│   └── repository/          # Repository implementations
├── domain/
│   └── model/               # Domain models
├── ui/
│   ├── theme/               # Material3 theme
│   ├── components/          # Reusable UI components
│   ├── home/                # Home screen & ViewModel
│   ├── analytics/           # Analytics screen
│   ├── calendar/            # Calendar screen
│   ├── more/                # More menu
│   ├── categories/          # Category management
│   ├── budget/              # Budget settings
│   └── iou/                 # IOU management
├── utils/                   # Utility classes
└── MainActivity.kt
```

## Database Schema

**4 Main Tables:**
1. **Expense** - Core expense tracking
2. **Category** - Expense categorization (7 defaults + custom)
3. **IOU** - Other transactions (money owed/receivable)
4. **Budget** - Monthly budget limits

See [database_design.md](file:///C:/Users/appis/.gemini/antigravity/brain/d880657b-60c8-4aa8-9ba7-1c70b069e11e/database_design.md) for detailed schema.

## Building the App

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 26+ (Android 8.0)

### Build Steps

1. **Clone or open the project:**
   ```bash
   cd s:\my_personal_projects\Splendid
   ```

2. **Open in Android Studio:**
   - File → Open → Select the Splendid folder

3. **Sync Gradle:**
   - Android Studio will automatically sync Gradle dependencies
   - Or manually: File → Sync Project with Gradle Files

4. **Build the project:**
   ```bash
   ./gradlew build
   ```

5. **Run on emulator or device:**
   - Click the "Run" button in Android Studio
   - Or use command line:
   ```bash
   ./gradlew installDebug
   ```

### Build Variants
- **Debug:** Development build with debugging enabled
- **Release:** Production build (requires signing configuration)

## Default Categories

The app comes with 7 pre-configured categories:
- 🍔 Food (#FF6B6B)
- 🚗 Transport (#4ECDC4)
- 🛍️ Shopping (#45B7D1)
- 🎬 Entertainment (#FFA07A)
- 💡 Bills (#98D8C8)
- 🏥 Health (#F7DC6F)
- 📦 Other (#B19CD9)

Users can add custom categories with custom colors.

## Key Design Decisions

### Typo Prevention
- **Smart Autocomplete:** Triggers after 2-3 characters, shows frequency-ordered suggestions
- **Case-Insensitive Analytics:** "Coffee", "coffee", "COFFEE" grouped together
- **Future:** Manual merge functionality for fixing typos

### Edge Case Handling
- Budget congratulations only shown if budget was set
- Empty analytics show friendly "No expenses" message
- Quantity defaults to 1 if invalid value entered
- Deterministic sorting using `timestamp DESC, id DESC`

### Currency
- Default: ₹ (INR - Indian Rupee)
- Formatted using `NumberFormat.getCurrencyInstance(Locale("en", "IN"))`

## Testing

### Manual Testing Checklist
- [ ] Add expense with smart suggestions
- [ ] Edit expense (verify "Edited" tag appears)
- [ ] Delete expense with confirmation
- [ ] Set budget and verify warning when exceeded
- [ ] Add IOU and verify it doesn't affect daily total
- [ ] Try deleting category in use (should fail)
- [ ] Verify offline functionality

### Unit Tests
- ViewModels (expense calculations, budget logic)
- Use Cases (validation, data processing)
- Repository tests with in-memory Room database

## License

This project is for educational purposes.

## Contact

For questions or issues, please contact the development team.
