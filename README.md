# FinTrack CLI — Personal Finance Manager with Statistical Anomaly Detection

[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-Layered%20OOP-green.svg)]()
[![Design Patterns](https://img.shields.io/badge/Design%20Patterns-Factory%20%7C%20Strategy%20%7C%20Observer%20%7C%20Singleton-orange.svg)]()
[![Persistence](https://img.shields.io/badge/Persistence-Atomic%20JSON%20%2B%20CSV-purple.svg)]()
[![Tests](https://img.shields.io/badge/Tests-JUnit%205-brightgreen.svg)]()

> A robust, terminal-native personal finance manager built in Core Java that goes beyond standard transaction tracking by integrating a **statistical anomaly-detection engine** (Z-Score and Interquartile Range / IQR) to proactively flag irregular spending behavior in real time.

---

## Table of Contents
1. [Overview & Problem Statement](#overview--problem-statement)
2. [Core Architecture & OOP Pillars](#core-architecture--oop-pillars)
3. [GoF Design Patterns Implemented](#gof-design-patterns-implemented)
4. [Statistical Anomaly Detection Engine](#statistical-anomaly-detection-engine)
5. [System Modules & Key Features](#system-modules--key-features)
6. [Project Structure](#project-structure)
7. [Getting Started & Quick Run](#getting-started--quick-run)
8. [CLI Walkthrough & Sample Output](#cli-walkthrough--sample-output)
9. [Running Unit Tests](#running-unit-tests)
10. [Documentation & Diagrams](#documentation--diagrams)

---

## Overview & Problem Statement

Most students and early professionals track expenses manually or in spreadsheets, with no way to catch unusual or erroneous spending until it's too late. FinTrack CLI provides a lightweight, offline, terminal-based tool that not only records and reports transactions but **actively flags anomalies** using statistical methods — applying object-oriented design and data analysis without requiring a heavy GUI, database server, or internet connection.

---

## Core Architecture & OOP Pillars

FinTrack CLI adheres strictly to the four foundational pillars of Object-Oriented Programming:

| OOP Pillar | Concrete Implementation in FinTrack CLI |
|---|---|
| **Abstraction** | Abstract base class [`Account`](file:///src/main/java/fintrack/model/Account.java) defines financial contracts (`withdraw`, `deposit`, `calculateMonthlyInterest`, `getAvailableFunds`). Callers interact with abstract contracts without depending on underlying implementation. Interfaces [`AnomalyDetectionStrategy`](file:///src/main/java/fintrack/strategy/AnomalyDetectionStrategy.java) and [`AlertObserver`](file:///src/main/java/fintrack/observer/AlertObserver.java) abstract outlier algorithms and notification mechanisms. |
| **Inheritance** | [`SavingsAccount`](file:///src/main/java/fintrack/model/SavingsAccount.java), [`CheckingAccount`](file:///src/main/java/fintrack/model/CheckingAccount.java), and [`CreditAccount`](file:///src/main/java/fintrack/model/CreditAccount.java) inherit from [`Account`](file:///src/main/java/fintrack/model/Account.java), inheriting common attributes (ID, user, balance, timestamps) while specializing financial rules. |
| **Polymorphism** | Dynamic method dispatch enables polymorphic withdrawal checks (Checking allows overdraft up to limit with optional fee; Savings enforces minimum balance constraint; Credit tracks debt line). At runtime, [`AnomalyService`](file:///src/main/java/fintrack/service/AnomalyService.java) polymorphically invokes whichever statistical strategy is currently selected. |
| **Encapsulation** | All model fields are private/protected. State mutations occur solely through validated mutators. Critical business logic enforces invariants (e.g. positive amounts, valid dates, salt-hash verification). Passwords are never stored in plaintext. |

---

## GoF Design Patterns Implemented

FinTrack CLI explicitly implements 4 classic Gang of Four (GoF) design patterns:

### 1. Factory Pattern (`AccountFactory`)
- **Package**: `fintrack.factory`
- **Class**: [`AccountFactory`](file:///src/main/java/fintrack/factory/AccountFactory.java)
- **Purpose**: Decouples the client UI and service layer from concrete account classes. Based on the selected `AccountType` (`SAVINGS`, `CHECKING`, `CREDIT`), the factory instantiates the proper subclass and validates type-specific parameters (e.g., minimum balance, overdraft limit, APR).

### 2. Strategy Pattern (`AnomalyDetectionStrategy`)
- **Package**: `fintrack.strategy`
- **Interface**: [`AnomalyDetectionStrategy`](file:///src/main/java/fintrack/strategy/AnomalyDetectionStrategy.java)
- **Concrete Strategies**:
  - [`ZScoreStrategy`](file:///src/main/java/fintrack/strategy/ZScoreStrategy.java): Parametric statistical model based on historical mean and standard deviation.
  - [`IQRStrategy`](file:///src/main/java/fintrack/strategy/IQRStrategy.java): Non-parametric Tukey fence model based on quartiles ($Q_1, Q_3$).
- **Purpose**: Allows users to dynamically switch between outlier detection algorithms and tune sensitivity thresholds at runtime without modifying business logic.

### 3. Observer Pattern (`AlertPublisher`, `AlertObserver`)
- **Package**: `fintrack.observer`
- **Subject**: [`AlertPublisher`](file:///src/main/java/fintrack/observer/AlertPublisher.java)
- **Interface**: [`AlertObserver`](file:///src/main/java/fintrack/observer/AlertObserver.java)
- **Subscribers**:
  - [`BudgetObserver`](file:///src/main/java/fintrack/observer/BudgetObserver.java): Listens for budget threshold events (80% warning, 100% breach).
  - [`AnomalyObserver`](file:///src/main/java/fintrack/observer/AnomalyObserver.java): Listens for flagged outlier events and renders warning banners.
  - [`AuditLogObserver`](file:///src/main/java/fintrack/observer/AuditLogObserver.java): Persists all critical security and financial events to `data/audit.log`.
- **Purpose**: Completely decouples transaction recording from downstream notifications and logging actions.

### 4. Singleton Pattern (`Logger`)
- **Package**: `fintrack.util`
- **Class**: [`Logger`](file:///src/main/java/fintrack/util/Logger.java)
- **Purpose**: Employs the thread-safe Bill Pugh Initialization-on-Demand Holder idiom. Ensures a single centralized logging manager across threads, writing timestamped logs with levels (`DEBUG`, `INFO`, `WARN`, `ERROR`) to `data/fintrack.log`.

---

## Statistical Anomaly Detection Engine

### 1. Z-Score (Standard Score) Method
For a historical collection of expense transactions $X = \{x_1, x_2, \dots, x_n\}$ within a spending category:
$$\mu = \frac{1}{n}\sum_{i=1}^n x_i, \quad \sigma = \sqrt{\frac{1}{n-1}\sum_{i=1}^n (x_i - \mu)^2}$$
For a newly recorded expense $x$:
$$Z = \frac{|x - \mu|}{\sigma}$$
- If $Z > \text{Threshold}$ (default $2.5\sigma$, configurable), the transaction is flagged.
- **Example**: If typical restaurant meals average ₹500 with $\sigma = ₹100$, a sudden ₹3,500 banquet charge yields $Z = 30.0$, immediately flagging the charge with a clear mathematical explanation.

### 2. Interquartile Range (IQR / Tukey's Fences) Method
Non-parametric method that sorts spending amounts:
$$Q_1 = \text{25th Percentile}, \quad Q_3 = \text{75th Percentile}, \quad IQR = Q_3 - Q_1$$
$$\text{Upper Fence} = Q_3 + (k \cdot IQR)$$
- Where $k$ is the sensitivity multiplier (default $1.5$ for outliers, $3.0$ for extreme outliers).
- If $x > \text{Upper Fence}$, the transaction is flagged.

---

## System Modules & Key Features

### Module 1: Account & User Management
- Secure user registration and login with **SHA-256 + 16-byte random salt** and 1,000 rounds of key stretching.
- Polymorphic account creation:
  - **Savings Account**: Enforces minimum balance; calculates earned monthly interest.
  - **Checking Account**: Accommodates overdraft limit; tracks overdraft transaction fees.
  - **Credit Account**: Tracks outstanding revolving balance (debt) against credit line ceiling; calculates monthly finance charges.
- Intra-account fund transfers with automatic rollback on error.

### Module 2: Transaction & Budget Management
- CRUD operations for `EXPENSE`, `INCOME`, and `TRANSFER` across 10 distinct categories.
- Monthly budget limits per category with proactive threshold alerts (warning at 80%, breach at 100%).
- Real-time evaluation: every newly created expense transaction is immediately assessed by both the anomaly engine and budget service.
- CSV data export for transactions and budget utilization.

### Module 3: Anomaly Detection Engine
- Pluggable Strategy pattern (`ZScoreStrategy` & `IQRStrategy`).
- Configurable sensitivity thresholds via CLI settings.
- Real-time outlier alerts and comprehensive batch scan of historical records.
- "What-If" simulator: test hypothetical expense amounts without mutating balances.

### Module 4: Analytics & Visual Reports
- Category spending breakdown with terminal ASCII progress bars: `[████████░░░░] 65.0%`.
- Monthly cash flow trends (inflow, outflow, net savings, savings rate).
- Financial Health Executive Summary.
- "Seed Realistic Demo Data" feature to immediately populate 25+ realistic records with pre-configured outliers.

---

## Project Structure

```
fintrack-cli/
├── pom.xml                               # Maven build descriptor
├── build.bat                             # Windows build & test automation script
├── run.bat                               # Application launch script
├── statement.md                          # Problem statement & specifications
├── README.md                             # Project documentation & user guide
├── src/
│   ├── main/java/fintrack/
│   │   ├── cli/                          # Presentation layer (CliApp, Menu, ConsoleUtil)
│   │   ├── factory/                      # Factory pattern (AccountFactory)
│   │   ├── model/                        # Domain models (Account, Transaction, Budget, User)
│   │   ├── observer/                     # Observer pattern (AlertPublisher, AlertObserver)
│   │   ├── persistence/                  # Persistence layer (JsonFileStore, CsvExporter)
│   │   ├── service/                      # Service layer (Auth, Account, Txn, Budget, Anomaly)
│   │   ├── strategy/                     # Strategy pattern (ZScoreStrategy, IQRStrategy)
│   │   └── util/                         # Utilities (Logger Singleton, PasswordHasher)
│   └── test/java/fintrack/               # JUnit 5 unit tests
│       ├── AnomalyStrategyTest.java
│       ├── BudgetServiceTest.java
│       ├── AccountFactoryTest.java
│       └── PasswordHasherTest.java
├── data/                                 # Atomic local JSON storage and audit logs
│   ├── fintrack_data.json
│   ├── audit.log
│   └── fintrack.log
├── exports/                              # Exported CSV spreadsheets
└── docs/                                 # Academic project report and Mermaid diagrams
    ├── PROJECT_REPORT.md
    ├── architecture_diagram.mermaid
    ├── class_diagram.mermaid
    ├── sequence_diagram.mermaid
    ├── use_case_diagram.mermaid
    └── workflow_diagram.mermaid
```

---

## Getting Started & Quick Run

### Prerequisites
- **Java Development Kit (JDK 17 or higher)** (Tested on Java 24).
- Windows PowerShell or Command Prompt (also compatible with Linux / macOS terminal).

### 1. Build and Test
Run the automated build script:
```cmd
build.bat
```
This script automatically:
1. Detects Maven (or uses standalone compiler and test runner).
2. Compiles all main classes.
3. Compiles and executes the complete JUnit 5 test suite.
4. Packages an executable runnable JAR: `target/fintrack-cli.jar`.

### 2. Run the Application
Launch the interactive terminal interface:
```cmd
run.bat
```
Or directly with Java:
```cmd
java -jar target/fintrack-cli.jar
```

---

## CLI Walkthrough & Sample Output

### 1. Welcome & Authentication
```
================================================================================
   WELCOME TO FINTRACK CLI
================================================================================
  1. Login to Existing Account
  2. Register New User
  3. Exit FinTrack

Select Option (1-3): 2
Full Name: Ankit Sharma
Choose Username (alphanumeric, 3-20 chars): ankit_s
Choose Secure Password (min 6 characters): secret123
✔ Registration successful! You can now log in with your credentials.
```

### 2. Instant Demo Seeding
In the main menu, press `7` to seed realistic financial test data:
```
Seeding Realistic Financial Demo Dataset...
✔ Account created: SBI Wealth Savings (₹50,000.00)
✔ Account created: HDFC Salary Checking (₹25,000.00)
✔ Account created: ICICI Platinum Credit Card (Limit ₹100,000.00)
ℹ Injecting Anomaly 1: Sudden ₹18,500 Banquet Dinner (normal baseline ₹400-₹800)...

[ANOMALY ENGINE ALERT] ⚠️ Statistical Outlier Detected!
   Title : Unusual Expense Flagged
   Detail: Expense ₹18,500.00 exceeds category mean ₹573.33 by 5.82σ (Threshold: 2.50σ)
   Action: Transaction recorded but flagged for review in the Anomaly Report.
```

### 3. Accounts Overview Table
```
┌──────────────┬────────────────────────────┬──────────┬─────────────┬──────────────────────┬──────────────────────────────────────────────────────┐
│ Account ID   │ Name                       │ Type     │ Balance (₹) │ Available Funds (₹)  │ Details / Limits                                     │
├──────────────┼────────────────────────────┼──────────┼─────────────┼──────────────────────┼──────────────────────────────────────────────────────┤
│ ACC-94E51F4A │ SBI Wealth Savings         │ SAVINGS  │ ₹50,000.00  │ ₹48,000.00           │ Min Bal: ₹2,000 | Int: 4.5%                          │
│ ACC-11A0B2CD │ HDFC Salary Checking       │ CHECKING │ ₹105,480.00 │ ₹115,480.00          │ Overdraft: ₹10,000 | Fee: ₹15                        │
│ ACC-D839E421 │ ICICI Platinum Credit Card │ CREDIT   │ ₹73,500.00  │ ₹26,500.00           │ Credit Limit: ₹100,000 | APR: 18.5% (Debt: ₹73,500)  │
└──────────────┴────────────────────────────┴──────────┴─────────────┴──────────────────────┴──────────────────────────────────────────────────────┘
```

### 4. Category Spending Breakdown with ASCII Progress Bars
```
┌──────────────────────────┬─────────────────┬─────────┬───────────┬─────────────────────┐
│ Category                 │ Total Spent (₹) │ % Share │ Txn Count │ Visual Distribution │
├──────────────────────────┼─────────────────┼─────────┼───────────┼─────────────────────┤
│ Shopping & Electronics   │ ₹55,000.00      │ 62.4%   │ 1         │ [█████████░░░░░░]   │
│ Food & Dining            │ ₹23,660.00      │ 26.8%   │ 10        │ [████░░░░░░░░░░░]   │
│ Bills & Utilities        │ ₹4,700.00       │ 5.3%    │ 4         │ [█░░░░░░░░░░░░░░]   │
│ Entertainment & Leisure  │ ₹4,050.00       │ 4.6%    │ 4         │ [█░░░░░░░░░░░░░░]   │
└──────────────────────────┴─────────────────┴─────────┴───────────┴─────────────────────┘
```

---

## Running Unit Tests

The test suite covers algorithmic outlier mathematics, budget threshold alerts, factory instantiation, and password security.

To execute tests directly:
```cmd
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path "target/classes;target/test-classes;lib/gson-2.10.1.jar" --scan-class-path
```
Expected output:
```
[INFO] Running JUnit 5 Test Suite...
Test run finished after 340 ms
[         4 containers found      ]
[         0 containers skipped    ]
[         4 containers started    ]
[         0 containers aborted    ]
[         4 containers successful ]
[         0 containers failed     ]
[        15 tests found           ]
[         0 tests skipped         ]
[        15 tests started         ]
[         0 tests aborted         ]
[        15 tests successful      ]
[         0 tests failed          ]
[SUCCESS] All unit tests passed!
```

---

## Documentation & Diagrams

Complete system design diagrams are located in the [`docs/`](file:///docs/) directory:
- **[Full 15-Section Project Report](file:///docs/PROJECT_REPORT.md)**
- **[System Architecture Diagram](file:///docs/architecture_diagram.mermaid)**
- **[Class Diagram & Inheritance Hierarchy](file:///docs/class_diagram.mermaid)**
- **[Sequence Diagram: Transaction & Anomaly Flow](file:///docs/sequence_diagram.mermaid)**
- **[Use Case Diagram](file:///docs/use_case_diagram.mermaid)**
- **[Workflow Diagram](file:///docs/workflow_diagram.mermaid)**
