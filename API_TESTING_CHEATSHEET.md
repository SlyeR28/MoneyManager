# 📄 Money Management API Testing Cheat Sheet (1-Page Reference)

---

## 🔐 1. Seeded Test Credentials

These accounts are auto-seeded in `docker` and `prod` profiles via `DatabaseSeeder.java`:

| Full Name | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **Alex Morgan** | `alex@moneymanager.com` | `Password@123` | `Active` |
| **Sarah Jenkins** | `sarah@moneymanager.com` | `Password@123` | `Active` |
| **David Miller** | `david@moneymanager.com` | `Password@123` | `Active` |
| **Emma Watson** | `emma@moneymanager.com` | `Password@123` | `Active` |
| **Admin User** | `admin@moneymanager.com` | `Password@123` | `Active` |

> 🌐 **Base URLs:**
> - **Docker Environment:** `http://localhost:8009`
> - **Local / Prod Environment:** `http://localhost:8080`

---

## ⚡ 2. Step 1: Login & Get Token in PowerShell

In Windows PowerShell, use `curl.exe` or `Invoke-RestMethod`:

### Option A: Using `curl.exe` (Single Line)
```powershell
curl.exe -X POST http://localhost:8009/api/v1/login -H "Content-Type: application/json" -d "{\"email\":\"alex@moneymanager.com\",\"password\":\"Password@123\"}"
```

### Option B: Using Native PowerShell `Invoke-RestMethod` (Recommended for PS)
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8009/api/v1/login" -Method POST -ContentType "application/json" -Body '{"email":"alex@moneymanager.com","password":"Password@123"}'
$TOKEN = $response.token
Write-Host "Your Token is: $TOKEN"
```

---

## 🚀 3. Complete API Endpoint Matrix

| Section | Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/v1/login` | Log in and receive JWT token | ❌ No |
| | `POST` | `/api/v1/register` | Register a new user | ❌ No |
| | `GET` | `/api/v1/activation?token={token}` | Activate registered account | ❌ No |
| **Dashboard** | `GET` | `/dashboard/` | Full dashboard summary & statistics | ✅ Yes |
| **Expenses** | `GET` | `/api/expenses/get` | Get all expenses for current month | ✅ Yes |
| | `GET` | `/api/expenses/top5` | Get latest 5 expenses | ✅ Yes |
| | `GET` | `/api/expenses/total` | Get total expense sum | ✅ Yes |
| | `POST` | `/api/expenses/add` | Add a new expense | ✅ Yes |
| | `DELETE` | `/api/expenses/{id}` | Delete expense by ID | ✅ Yes |
| **Incomes** | `GET` | `/api/incomes/get` | Get all incomes for current month | ✅ Yes |
| | `GET` | `/api/incomes/top5` | Get latest 5 incomes | ✅ Yes |
| | `GET` | `/api/incomes/total` | Get total income sum | ✅ Yes |
| | `POST` | `/api/incomes/add` | Add a new income | ✅ Yes |
| | `DELETE` | `/api/incomes/{id}` | Delete income by ID | ✅ Yes |
| **Categories** | `GET` | `/api/category/` | Get all categories for user | ✅ Yes |
| | `GET` | `/api/category/{type}` | Get by type (`EXPENSE` or `INCOME`) | ✅ Yes |
| | `POST` | `/api/category/create` | Create new custom category | ✅ Yes |
| | `PUT` | `/api/category/{id}` | Update existing category | ✅ Yes |
| **Filter** | `POST` | `/api/filter/` | Filter transactions by date/keyword | ✅ Yes |

---

## 📋 4. Ready-to-Run PowerShell Commands

### 🔹 Save Token Variable (Paste token received from Step 2):
```powershell
$TOKEN = "eyJhbGciOiJIUzI1NiJ9..."
$headers = @{ "Authorization" = "Bearer $TOKEN"; "Content-Type" = "application/json" }
```

### 🔹 Dashboard Summary
```powershell
Invoke-RestMethod -Uri "http://localhost:8009/dashboard/" -Method GET -Headers $headers
```

### 🔹 Get Expenses & Incomes
```powershell
# Get Current Month Expenses
Invoke-RestMethod -Uri "http://localhost:8009/api/expenses/get" -Method GET -Headers $headers

# Get Top 5 Expenses
Invoke-RestMethod -Uri "http://localhost:8009/api/expenses/top5" -Method GET -Headers $headers

# Get Total Expenses
Invoke-RestMethod -Uri "http://localhost:8009/api/expenses/total" -Method GET -Headers $headers

# Get Current Month Incomes
Invoke-RestMethod -Uri "http://localhost:8009/api/incomes/get" -Method GET -Headers $headers
```

### 🔹 Add New Expense
```powershell
$body = '{"name":"Groceries at Supermarket","amount":65.50,"categoryId":1,"date":"2026-08-25"}'
Invoke-RestMethod -Uri "http://localhost:8009/api/expenses/add" -Method POST -Headers $headers -Body $body
```

### 🔹 Add New Income
```powershell
$body = '{"name":"Freelance Web App","amount":1200.00,"categoryId":2,"date":"2026-08-25"}'
Invoke-RestMethod -Uri "http://localhost:8009/api/incomes/add" -Method POST -Headers $headers -Body $body
```

### 🔹 Filter Transactions
```powershell
$body = '{"type":"expense","startDate":"2026-07-01","endDate":"2026-08-30","keyword":"Groceries","sortField":"date","sortOrder":"desc"}'
Invoke-RestMethod -Uri "http://localhost:8009/api/filter/" -Method POST -Headers $headers -Body $body
```
