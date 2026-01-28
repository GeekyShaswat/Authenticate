# Authenticate - OTP Login App
Android app for passwordless authentication using Email + OTP with session tracking.
- Google Drive link for app apk - https://drive.google.com/file/d/16We9yTpgkQVQI3sEOkWy_Dz10bKoQTyf/view?usp=sharing

**Tech Stack:** Kotlin | Jetpack Compose | MVVM | Coroutines | Firebase Analytics
---

## OTP Logic and Expiry Handling

### How OTP Works

**Generation:**
- Random 6-digit number (100000-999999)
- Stored in a `Map<String, OtpData>` with email as key
- Each email has only ONE active OTP

**Expiry Logic:**
```kotlin
val timePassed = System.currentTimeMillis() - otpData.generatedAt
if (timePassed > 60_000L) {
    return ValidationResult.Expired
}
```

- OTP generation time is saved using `System.currentTimeMillis()`
- On validation, we check: current time - generation time
- If difference > 60,000 milliseconds (60 seconds), OTP is expired
- Timer updates every second using `LaunchedEffect` with `delay(1000L)`

**Attempt Tracking:**
- User gets 3 attempts to enter correct OTP
- Each wrong attempt increments counter
- After 3 failed attempts, user must request new OTP
- Generating new OTP resets attempts to 0

**OTP Rules:**
- ✅ Successfully entered OTP is removed from storage (one-time use)
- ✅ New OTP replaces old OTP in Map
- ✅ New OTP resets attempt counter to 0
- ✅ New OTP restarts 60-second timer

**Timer Restart Logic:**
```kotlin
LaunchedEffect(email, generatedAt) {
    // Restarts when generatedAt changes (new OTP)
    while (timeRemaining > 0) {
        delay(1000L)
        timeRemaining = getRemainingTime(email)
    }
}
```
- `generatedAt` timestamp changes when new OTP is generated
- `LaunchedEffect` detects the change and restarts timer
- This is why we pass `generatedAt` in the state

---

## 📊 Data Structures and Why

### 1. Map<String, OtpData> for Storage
```kotlin
private val otpStorage = mutableMapOf<String, OtpData>()
```

**Why Map?**
- **O(1) Read** - Instant access to OTP by email
- **Unique** - Each email can have only one OTP
- **Easy update** - `map[email] = newData` replaces old data
- Better than List which requires O(n) search

**Data stored:**
```kotlin
data class OtpData(
    val code: String,           // 6-digit OTP
    val generatedAt: Long,      // When OTP was created
    val attempts: Int = 0,      // Failed attempts count
    val maxAttempts: Int = 3    // Maximum allowed
)
```

### 2. Sealed Class for Results
```kotlin
sealed class ValidationResult {
    object Success : ValidationResult()
    object Expired : ValidationResult()
    object MaxAttemptsExceeded : ValidationResult()
    data class Incorrect(val remainingAttempts: Int) : ValidationResult()
    object NoOtpFound : ValidationResult()
}
```

**Why Sealed Class?**
- **Type-safe** - Compiler checks all cases are handled
- **Carries data** - Can include `remainingAttempts` with `Incorrect`
- **Clear outcomes** - Each result has specific meaning
- Better than boolean or enum which can't carry extra data

### 3. StateFlow for UI State
```kotlin
private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
val authState: StateFlow<AuthState> = _authState.asStateFlow()
```

**Why StateFlow?**
- **Reactive** - UI updates automatically when state changes
- **Compose-friendly** - Works with `collectAsState()`
- **Lifecycle-aware** - No memory leaks
- **Single source of truth** - Only ViewModel modifies state

---

## Firebase Analytics - Why and How

### Why Firebase Analytics?

1. **Free** - No cost for basic event tracking
2. **Familiar** - Used Firebase before for authentication
3. **Industry standard** - Widely used in production apps
4. **DebugView** - Real-time event tracking during development

### Events Logged
```kotlin
1. otp_generated           → When OTP is created
2. otp_validation_success  → User logs in successfully
3. otp_validation_failure  → Wrong/expired/max attempts
4. user_logout            → Session ends (includes duration)
```

### Privacy

Emails are masked before logging:
```kotlin
john.doe@gmail.com → j***@gmail.com
```

### Testing Events
```bash
# Enable real-time debug mode
adb shell setprop debug.firebase.analytics.app com.shaswat.authenticate

# View events in Firebase Console → Analytics → DebugView
```

---

##  AI Usage Disclosure

### What I Used AI For:

1. **Firebase analytics** - Looked up current Firebase BoM syntax and initialization
2. **Time calculations** - Verified millisecond conversion logic
3. **Sealed class patterns** - Explored different ways to structure results

### What I Implemented Myself:

1. **OTP logic** - Designed validation flow, expiry checking, attempt tracking
2. **Map storage** - Chose Map over List for O(1) lookups
3. **MVVM architecture** - Structured ViewModel, State, and UI separation
4. **Timer bug fix** - Figured out `generatedAt` needed in state for timer restart
5. **State management** - Designed AuthState sealed class and one-way data flow
6. **Max attempts logic** - Fixed calculation to show 2→1→exceeded (not 0)
7. **UI/UX decisions** - Field clearing, keyboard closing, error messages

**How I Used AI:**
- As reference (like Stack Overflow or documentation)
- Always understood the topic/code implementation 
- Made all architecture and design decisions myself

---

**Basic flow:**
1. Enter email → Send OTP
2. Check Toast for OTP code
3. Enter OTP → Login
4. Watch session timer
5. Logout

**Test cases:**
- Wrong OTP 3 times → See "2 attempts" → "1 attempt" → "Max exceeded"
- Wait 60 seconds → OTP expires
- Click Resend → Timer resets to 60
- Rotate device → State survives

---

## Known Limitations

- OTP shown in Toast (production would use SMS/Email API)
- No persistent storage (session lost on app close)
- Single session only

**Author:** Shaswat Kotnala
**Assignment for:** Lokal  
**Date:** 27 January 2026
