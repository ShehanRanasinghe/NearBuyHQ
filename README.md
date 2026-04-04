<div align="center">

# 🛒 NearBuyHQ — Shop Admin App

**NearBuyHQ** is the Android admin application for shop owners in the **NearBuy** ecosystem.  
It lets shop owners manage their products, handle orders, run promotions, view analytics, and keep their store information up to date — all backed by **Firebase Firestore** in real time.

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2024-blue)](https://developer.android.com/about/versions/nougat)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

</div>

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Screenshots (App Flow)](#screenshots-app-flow)
4. [Tech Stack & Dependencies](#tech-stack--dependencies)
5. [Project Architecture](#project-architecture)
6. [Package & File Reference](#package--file-reference)
7. [Firebase Firestore Structure](#firebase-firestore-structure)
8. [Environment Setup (.env)](#environment-setup-env)
9. [Build & Run](#build--run)
10. [Permissions](#permissions)
11. [Contributors](#contributors)

---

## Overview

NearBuyHQ is part of a two-app system:

| App | Audience | Role |
|---|---|---|
| **NearBuyHQ** *(this repo)* | Shop owner / admin | Manage shop, products, orders, promotions |
| **NearBuy** *(customer app)* | Shoppers | Browse shops, place orders |

Both apps read from and write to the same **Firebase Firestore** database.  
The shop owner's Firebase Auth UID acts as the **shop ID** — every product, order, and notification is stored under `NearBuyHQ/{userId}/…`.

---

## Features

### 🔐 Authentication
- **Register** with full name, email, username and password
- **Email OTP verification** — a 6-digit code is sent via Gmail SMTP before the account is activated
- **Login** with email / username and password
- **Forgot Password** flow with OTP-based reset
- Session persistence via `SharedPreferences` (`SessionManager`)

### 🏠 Dashboard
- Greeting header showing the owner's name and shop name (loaded live from Firestore)
- **Stats cards**: Total Products, Low-Stock count, Orders & Revenue
- **Business Overview filter**: Today / Yesterday / Last 7 Days / This Month / Lifetime
- Quick-action tiles: Add Product, Manage Inventory, Discounts, Manage Orders, View Reports, Set Store Location
- Bottom navigation bar: Dashboard · Products · Orders · Analytics · Profile

### 📦 Product Management
- **Add / Edit Product** — name, description, price, unit, stock quantity, category
- **Product categories**: Vegetables, Fruits, Grains, Meats, Seafood, Dairy, Bakery, Beverages, Snacks, Other
- **Product List** — scrollable, searchable list of all shop products
- **Product Details** — full product view with edit and delete options
- **Inventory** — quick stock overview with low-stock highlighting (threshold: 10 units)

### 📋 Order Management
- **Order List** — all orders with status filter tabs (All / Pending / Processing / Delivered / Cancelled)
- Real-time search across orders
- **Order Details** — full order summary, item breakdown, customer info, and status update controls

### 🏷️ Discounts
- **Promotions tab** — create, edit and delete percentage/flat-amount promotions with start/end dates
- **Deals tab** — create, edit and delete limited-time deals with a deal price and expiry
- Tab-based UI with `ViewPager2` + `TabLayout`

### 📊 Analytics
- Live bar chart (MPAndroidChart) showing daily revenue for the past 7 days
- Summary cards: Total Revenue, Total Sales, Completed / Pending / Processing / Cancelled orders

### 📣 Notifications
- In-app notification feed for the shop owner
- Notification types: **Low Stock Alert**, **New Order**, **Deal Expiring**
- Searchable list; notifications are scoped to the shop owner's UID

### 📝 Reports
- View customer-submitted reports/complaints stored in Firestore
- **Report Details** screen with full description and metadata
- Searchable list

### ⚙️ Settings / Profile
- View and edit personal info: name, email, phone
- View and edit shop details: shop name, location address, opening hours
- **Location Picker** — interactive Google Maps picker with Google Places autocomplete search
- Logout with confirmation dialog

---

## Screenshots (App Flow)

```
SplashScreen → Welcome → Register / Login
                              ↓
                       OTP Verification
                              ↓
                          Dashboard
              ┌──────────────┼──────────────┐
        Products          Orders         Analytics
    ┌────────────┐    ┌──────────┐    ┌──────────────┐
  Add Product  Inv.  Order List  Det.  Bar Chart + KPIs
  Prod. List  Prod.                         
  Prod. Det.                    
              Discounts     Reports    Profile + Location
```

---

## Tech Stack & Dependencies

| Category | Library / Tool | Version |
|---|---|---|
| **Language** | Java | 17 |
| **Android Gradle Plugin** | `com.android.application` | 8.13.0 |
| **Min / Target SDK** | Android API | 24 / 36 |
| **UI** | AndroidX AppCompat | 1.7.1 |
| **UI** | Material Design Components | 1.13.0 |
| **UI** | ConstraintLayout | 2.2.1 |
| **UI** | RecyclerView | 1.3.2 |
| **Firebase** | Firebase BOM | 34.11.0 |
| **Firebase** | Firebase Authentication | (BOM-managed) |
| **Firebase** | Cloud Firestore | (BOM-managed) |
| **Maps** | Google Maps SDK for Android | 18.2.0 |
| **Maps** | Google Places SDK | 3.3.0 |
| **Location** | Play Services Location (GPS) | 21.3.0 |
| **Charts** | MPAndroidChart | v3.1.0 |
| **Email (OTP)** | JavaMail android-mail | 1.6.7 |
| **Email (OTP)** | android-activation | 1.6.7 |
| **Testing** | JUnit | 4.13.2 |
| **Testing** | AndroidX Test JUnit | 1.3.0 |
| **Testing** | Espresso Core | 3.7.0 |

---

## Project Architecture

The app follows a **Repository + Activity** pattern (no ViewModel/LiveData), with Firebase as the single source of truth.

```
com.example.nearbuyhq/
├── app/startup/          ← App entry points
├── auth/                 ← Authentication screens + OTP service
├── core/                 ← Session management + Firebase config
├── dashboard/            ← Dashboard home + Analytics
├── data/
│   ├── model/            ← POJO data classes
│   ├── remote/firebase/  ← Firestore collection name constants
│   └── repository/       ← All Firestore read/write logic
├── discounts/            ← Promotions & Deals screens + adapters
├── notifications/        ← Notification screen + adapter
├── orders/               ← Order list & detail screens + adapter
├── products/             ← Product CRUD screens + adapters
├── reports/              ← Reports screen + adapter
└── settings/             ← Profile, Location Picker, Logout
```

**Data flow:**
```
Activity / Fragment
      │ calls
      ▼
Repository  (e.g. ProductRepository)
      │ uses
      ▼
FirebaseFirestore / FirebaseAuth
      │ results via
      ▼
DataCallback<T> / OperationCallback  (interface callbacks)
      │ back to
      ▼
Activity (runOnUiThread → update UI)
```

---

## Package & File Reference

### `app.startup`
| File | Purpose |
|---|---|
| `SplashScreen.java` | Launcher activity — shows the NearBuyHQ logo for ~1.5 s, then routes to `Welcome` (first run) or `Dashboard` (returning user). |
| `Welcome.java` | Onboarding screen with **Get Started** and **Log In** buttons. |

---

### `auth`
| File | Purpose |
|---|---|
| `Login.java` | Email/username + password login via Firebase Auth. Checks email-verification status in Firestore before granting access; redirects to `OTPVerification` if OTP was never completed. |
| `Register.java` | New account registration form (full name, email, username, password). Calls `AuthRepository.register()` and then sends an OTP to the email. |
| `ForgotPassword.java` | Forgot-password flow — enters email, triggers OTP dispatch, then allows password reset. |
| `OTPVerification.java` | 6-digit OTP input screen. Verifies the code stored in Firestore (`otp_codes` collection). On success, marks the account as email-verified and navigates to `Dashboard`. |
| `EmailOtpService.java` | Generates a random 6-digit OTP, stores it in Firestore with a **10-minute expiry**, and sends a styled HTML email via **Gmail SMTP** (credentials from `BuildConfig` / `.env`). |

---

### `core`
| File | Purpose |
|---|---|
| `SessionManager.java` | Singleton wrapper around `SharedPreferences`. Stores and retrieves `userId`, `userName`, `userEmail`, `userPhone`, `shopId`, `shopName` across the app lifecycle. Exposes `isLoggedIn()` and `clearSession()`. |
| `firebase/FirebaseConfig.java` | Reads the `FIREBASE_ENABLED` build config flag. Used to short-circuit repository calls when Firebase is disabled (local dev / testing). |

---

### `dashboard`
| File | Purpose |
|---|---|
| `Dashboard.java` | Main home screen after login. Displays live stats (total products, low-stock count, orders, revenue) filtered by a selectable time period (Today / Yesterday / 7 Days / Month / Lifetime). Contains a **bottom navigation bar** and six quick-action tiles. |
| `Analytics.java` | Analytics screen with a **MPAndroidChart bar chart** showing daily revenue for the last 7 days, plus KPI cards: Total Revenue, Total Sales, Completed / Pending / Processing / Cancelled order counts. |

---

### `data/model`
| File | Purpose |
|---|---|
| `User.java` | POJO for a shop-owner user profile: `uid`, `name`, `email`, `username`, `phone`, `shopName`, `shopLocation`, `openingHours`, `emailVerified`. |

---

### `data/remote/firebase`
| File | Purpose |
|---|---|
| `FirebaseCollections.java` | Central constants for all Firestore collection/subcollection names (`NearBuyHQ`, `otp_codes`, `products`, `deals`, `promotions`, `orders`, `reports`, `notifications`). Prevents hard-coded strings across the codebase. |

---

### `data/repository`
| File | Purpose |
|---|---|
| `AuthRepository.java` | Firebase Auth operations: `register()`, `login()`, `logout()`, `resetPassword()`, `getUserProfile()`, `updateUserProfile()`, `isEmailVerifiedInFirestore()`. Populates `SessionManager` after login. |
| `ProductRepository.java` | CRUD for `NearBuyHQ/{uid}/products`: add, update, delete, get by ID, get all by shop + optional category filter. |
| `OrderRepository.java` | Read/update `NearBuyHQ/{uid}/orders`: get all orders, get by date range (for Dashboard/Analytics filters), update order status. |
| `DiscountRepository.java` | CRUD for `NearBuyHQ/{uid}/promotions` and `NearBuyHQ/{uid}/deals`. |
| `NotificationRepository.java` | Read notifications from `NearBuyHQ/{uid}/notifications`. |
| `ReportRepository.java` | Read reports from `NearBuyHQ/{uid}/reports`. |
| `DataCallback<T>.java` | Generic callback interface — `onSuccess(T data)` / `onError(Exception e)` — used for data-fetch operations. |
| `OperationCallback.java` | Simpler callback — `onSuccess()` / `onError(Exception e)` — used for write/delete operations. |

---

### `discounts`
| File | Purpose |
|---|---|
| `DiscountsActivity.java` | Host activity for the Discounts section. Contains a `ViewPager2` + `TabLayout` with two tabs: **Promotions** (tab 0) and **Deals** (tab 1). |
| `DiscountsPagerAdapter.java` | `FragmentStateAdapter` that supplies `PromotionsFragment` and `DealsFragment` to the `ViewPager2`. |
| `PromotionsFragment.java` | Lists all active promotions for the shop in a `RecyclerView`. FAB to add a new promotion. |
| `PromotionAdapter.java` | `RecyclerView.Adapter` for promotion list items. |
| `Promotion.java` | POJO: `id`, `title`, `description`, `discountType` (percentage / flat), `discountValue`, `startDate`, `endDate`, `status`. |
| `AddEditPromotion.java` | Add / edit promotion form. Validates date ranges and saves to Firestore. |
| `DealsFragment.java` | Lists all deals in a `RecyclerView`. FAB to add a new deal. |
| `DealsAdapter.java` | `RecyclerView.Adapter` for deal list items. |
| `Deal.java` | POJO: `id`, `title`, `description`, `originalPrice`, `dealPrice`, `expiryDate`, `status`. |
| `AddDeal.java` | Add / edit deal form. |
| `DealDetails.java` | Read-only deal detail view. |

---

### `notifications`
| File | Purpose |
|---|---|
| `Notifications.java` | Full-screen list of in-app notifications for the shop owner. Supports real-time search. Notification types include *Low Stock Alert*, *New Order*, *Deal Expiring*. |
| `NotificationsAdapter.java` | `RecyclerView.Adapter` for notification list items — shows title, body, timestamp, and a type icon. |
| `Notification.java` | POJO: `id`, `title`, `message`, `type`, `shopId`, `createdAt`, `read`. |

---

### `orders`
| File | Purpose |
|---|---|
| `Order_List.java` | Displays all orders with tab-style status filters (All / Pending / Processing / Delivered / Cancelled / Rejected) and a real-time search bar. |
| `Order_details.java` | Full order detail screen — shows customer name, address, product line items, total, order date, and status. Provides **Accept / Reject / Mark as Delivered** action buttons. |
| `OrderAdapter.java` | `RecyclerView.Adapter` for order list rows — shows order ID, customer, total, date and colour-coded status badge. |
| `Order.java` | POJO: `orderId`, `customerId`, `customerName`, `customerAddress`, `items` (list), `orderTotal`, `status`, `createdAt`, `updatedAt`. |

---

### `products`
| File | Purpose |
|---|---|
| `Add_Product.java` | Add/edit product form — name, description, price, unit, stock quantity, and category selector chips. Saves to `NearBuyHQ/{uid}/products`. |
| `Products_List.java` | Searchable product list with optional category filter. |
| `ProductsListAdapter.java` | `RecyclerView.Adapter` for the product list — shows name, price, stock, and category tag. |
| `Product_Details.java` | Full product detail view with **Edit** and **Delete** actions. |
| `Inventory.java` | Compact inventory view sorted by stock level; low-stock items are highlighted in red. |
| `InventoryAdapter.java` | `RecyclerView.Adapter` for inventory rows. |
| `ProductItem.java` | POJO: `productId`, `itemName`, `itemDetails`, `price`, `stockQuantity`, `unit`, `category`, `status`, `shopId`, `createdAt`, `updatedAt`. Includes `isLowStock(threshold)` helper. |

---

### `reports`
| File | Purpose |
|---|---|
| `Reports.java` | Lists all customer-submitted reports/complaints stored under `NearBuyHQ/{uid}/reports`. Supports text search. |
| `ReportDetails.java` | Full detail screen for a single report: reporter name, subject, description, timestamp. |
| `ReportsAdapter.java` | `RecyclerView.Adapter` for report list rows. |
| `Report.java` | POJO: `reportId`, `reporterName`, `subject`, `description`, `status`, `createdAt`. |

---

### `settings`
| File | Purpose |
|---|---|
| `ProfilePage.java` | Displays and allows editing of the owner's personal info (name, email, phone) and shop details (shop name, location, opening hours). Triggers `LocationPickerActivity` for map-based location selection. |
| `LocationPickerActivity.java` | Full-screen Google Maps activity. Supports: map tap to drop a pin, drag to reposition, Google Places autocomplete search bar, Geocoder reverse-geocoding for address display. Returns `(lat, lng, address)` to `ProfilePage`. |
| `LogoutConfirmation.java` | Confirmation dialog activity for logout — clears `SessionManager` and Firebase Auth session, then returns to `Login`. |

---

## Firebase Firestore Structure

```
Firestore root
│
├── NearBuyHQ/                        ← Root collection (one doc per shop owner)
│    └── {userId}/                    ← Document ID == Firebase Auth UID == shopId
│         ├── (fields)                name, email, username, phone,
│         │                           shopName, shopLocation, openingHours,
│         │                           emailVerified, createdAt, updatedAt
│         │
│         ├── products/               ← NearBuyHQ/{uid}/products/{productId}
│         │    └── {productId}        itemName, itemDetails, price, stockQuantity,
│         │                           unit, category, status, shopId, createdAt, updatedAt
│         │
│         ├── orders/                 ← NearBuyHQ/{uid}/orders/{orderId}
│         │    └── {orderId}          customerId, customerName, customerAddress,
│         │                           items[], orderTotal, status, createdAt, updatedAt
│         │
│         ├── promotions/             ← NearBuyHQ/{uid}/promotions/{promoId}
│         │    └── {promoId}          title, description, discountType, discountValue,
│         │                           startDate, endDate, status
│         │
│         ├── deals/                  ← NearBuyHQ/{uid}/deals/{dealId}
│         │    └── {dealId}           title, description, originalPrice, dealPrice,
│         │                           expiryDate, status
│         │
│         ├── reports/                ← NearBuyHQ/{uid}/reports/{reportId}
│         │    └── {reportId}         reporterName, subject, description, status, createdAt
│         │
│         └── notifications/          ← NearBuyHQ/{uid}/notifications/{notifId}
│              └── {notifId}          title, message, type, shopId, createdAt, read
│
└── otp_codes/                        ← Root-level (written before user is authenticated)
     └── {email}                      otp, expiresAt, createdAt
```

> **Note:** `userId == shopId` — registering an account simultaneously registers the shop. No separate shops collection is needed.

---

## Environment Setup (.env)

The app reads sensitive keys from a `.env` file in the project root (ignored by git).

### 1 — Firebase

Add your Firebase Android app config:
```
app/google-services.json
```

### 2 — Create `.env`

Create a file named `.env` in the **project root** (same level as `settings.gradle.kts`):

```dotenv
# ── Firebase ───────────────────────────────────────────────
FIREBASE_ENABLED=true
FIREBASE_PROJECT_ID=your-firebase-project-id

# ── Google Maps / Places ───────────────────────────────────
GOOGLE_MAP_APIKEY=your-google-maps-api-key

# ── Gmail SMTP (for OTP emails) ────────────────────────────
# Use a Gmail App Password, NOT your account password
SMTP_EMAIL=your-sender@gmail.com
SMTP_PASSWORD=your-gmail-app-password
```

| Key | Where it's used |
|---|---|
| `FIREBASE_ENABLED` | `FirebaseConfig.isFirebaseEnabled()` — gates all Firestore calls |
| `FIREBASE_PROJECT_ID` | Exposed as `BuildConfig.FIREBASE_PROJECT_ID` |
| `GOOGLE_MAP_APIKEY` | Injected into `AndroidManifest` via `manifestPlaceholders`; also used by `LocationPickerActivity` + Places SDK |
| `SMTP_EMAIL` / `SMTP_PASSWORD` | `EmailOtpService` — sends OTP emails via JavaMail over Gmail SMTP |

> If `FIREBASE_ENABLED=false`, all repository calls are short-circuited and return error callbacks immediately. Use this for UI-only development without a Firebase project.

---

## Build & Run

### Prerequisites
- Android Studio **Hedgehog** (2023.1.1) or later
- JDK 17
- Android device or emulator running API 24+

### Steps

```powershell
# 1. Clone the repository
git clone https://github.com/<your-org>/NearBuyHQ.git
cd NearBuyHQ

# 2. Add google-services.json
#    Copy your Firebase Android config to: app/google-services.json

# 3. Create .env (see Environment Setup above)

# 4. Build debug APK
.\gradlew.bat :app:assembleDebug

# 5. Install on connected device
.\gradlew.bat :app:installDebug

# 6. Run unit tests
.\gradlew.bat :app:testDebugUnitTest
```

The debug APK is output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Firestore Indexes

If Firestore prompts you to create a composite index for queries ordered by `updatedAt` or `createdAt`, follow the link in Logcat or create indexes manually in the Firebase Console for:

| Collection | Fields |
|---|---|
| `NearBuyHQ/{uid}/products` | `shopId` ASC + `updatedAt` DESC |
| `NearBuyHQ/{uid}/orders` | `shopId` ASC + `createdAt` DESC |

---

## Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | Firebase Firestore + Auth, Google Maps, Gmail SMTP |
| `ACCESS_FINE_LOCATION` | GPS capture for shop location in `LocationPickerActivity` |
| `ACCESS_COARSE_LOCATION` | Fallback location for the map picker |

---

## Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/ShehanRanasinghe">
        <img src="https://github.com/ShehanRanasinghe.png" width="80" alt="Shehan Ranasinghe"/><br/>
        <sub><b>Shehan Ranasinghe</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/sitharakavindi">
        <img src="https://github.com/sitharakavindi.png" width="80" alt="Sithara Kavindi"/><br/>
        <sub><b>Sithara Kavindi</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/PramodyaKarunathilake">
        <img src="https://github.com/PramodyaKarunathilake.png" width="80" alt="Pramodya Karunathilake"/><br/>
        <sub><b>Pramodya Karunathilake</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/SanduniKarunathilake">
        <img src="https://github.com/SanduniKarunathilake.png" width="80" alt="Sanduni Karunathilake"/><br/>
        <sub><b>Sanduni Karunathilake</b></sub>
      </a>
    </td>
  </tr>
</table>

---

## License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for full terms.

This project was developed as coursework for the **Higher National Diploma in Software Engineering (HNDSE)** at the **National Institute of Business Management (NIBM)** — Mobile Application Development module.

