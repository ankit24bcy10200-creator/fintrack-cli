# Project Statement: FinTrack CLI
**Personal Finance Manager with Statistical Anomaly Detection**

## 1. Problem Statement
Managing personal finances is one of the most critical life skills for university students and early-career professionals. However, existing personal finance solutions suffer from significant shortcomings:
- **Manual Spreadsheets & Static Ledgers**: Traditional spreadsheets (Excel, Google Sheets) require continuous manual reconciliation and offer zero proactive intelligence. Users only discover fraudulent charges, accidental double-billing, or runaway impulse spending weeks later when reviewing monthly statements.
- **Privacy and Connectivity Concerns**: Modern cloud-based personal finance apps frequently demand linking sensitive bank credentials to third-party servers, require persistent internet connectivity, and present security risks through centralized data breaches.
- **Absence of Real-time Outlier Intelligence**: Standard software tools merely tabulate income and expenses without analyzing behavioral patterns. They cannot distinguish between a typical ₹500 grocery run and an anomalous ₹45,000 charge.

**FinTrack CLI** solves this problem by delivering a fast, privacy-preserving, terminal-native personal finance application in Core Java. It combines complete transaction and budget management with an active **Statistical Anomaly Detection Engine** (Z-score and IQR algorithms) that monitors spending habits in real time and automatically alerts users to abnormal transactions.

---

## 2. Objectives
1. **Apply Core Object-Oriented Principles**:
   - **Abstraction**: Encapsulate banking concepts into clean contracts (`Account`, `AnomalyDetectionStrategy`, `AlertObserver`).
   - **Inheritance**: Specialize account behavior into distinct financial vehicles (`SavingsAccount`, `CheckingAccount`, `CreditAccount`).
   - **Polymorphism**: Support interchangeable anomaly detection strategies and dynamic dispatch for account balance modifications and alert publishing.
   - **Encapsulation**: Enforce strict data hiding, validation invariants, and cryptographically secure credential handling.
2. **Implement Four GoF Design Patterns**:
   - **Factory Pattern**: Dynamically instantiate Account subtypes based on business rules.
   - **Strategy Pattern**: Swap between parametric (Z-Score) and non-parametric (IQR) outlier detection at runtime.
   - **Observer Pattern**: Asynchronously broadcast budget threshold warnings and anomaly alerts across decoupled subscribers.
   - **Singleton Pattern**: Provide centralized, thread-safe system logging and audit trails.
3. **Ensure High Performance & Resilience**:
   - Guarantee sub-second analytical report generation over 10,000+ transactions.
   - Implement zero-crash fault-tolerant input validation and atomic file persistence.
4. **Deliver Complete Offline Privacy**:
   - Rely strictly on local JSON persistence and CSV exports without external database servers or cloud dependencies.

---

## 3. Scope & Target Users
### Target Users
- **University Students & Scholars**: Tracking daily stipends, pocket money, food expenses, and hostel supplies.
- **Early Professionals & Freelancers**: Managing multiple income streams, personal checking accounts, and credit card debts.
- **Privacy-Conscious Individuals**: Users seeking full control over their financial data without telemetry, ads, or external cloud storage.

### Scope Boundaries
- **In Scope**:
  - Multi-user authentication with salted SHA-256 password hashing.
  - Multi-account portfolio management (Savings, Checking, Credit).
  - Full CRUD operations for income, expenses, and intra-account transfers.
  - Monthly budget limit configuration per spending category.
  - Statistical outlier detection via Z-Score ($\mu \pm k\sigma$) and Tukey's IQR ($Q_3 + k \cdot IQR$).
  - Real-time event notifications for budget warnings (80%), breaches (100%), and anomaly detections.
  - Category spending distributions with visual ASCII bar charts and monthly cash flow trends.
  - Formatted CSV exports and atomic JSON persistence.
- **Out of Scope**:
  - Direct live open-banking API integration (due to privacy and security sandbox constraints).
  - Heavy graphical user interface (designed explicitly as a terminal CLI application).

---

## 4. Functional Requirements
| ID | Module | Requirement Description |
|---|---|---|
| **FR-01** | User Auth | Secure registration and login using SHA-256 with 16-byte random salt and key stretching. |
| **FR-02** | Account Mgmt | Support Savings, Checking, and Credit accounts created via `AccountFactory`. |
| **FR-03** | Polymorphic Rules | Savings enforces minimum balance; Checking allows overdraft up to limit; Credit tracks debt balance. |
| **FR-04** | Transaction Mgmt| CRUD support for EXPENSE, INCOME, and TRANSFER across standard categories. |
| **FR-05** | Budgeting | Set monthly budget limits per category; calculate spending percentages and remaining funds. |
| **FR-06** | Anomaly Detection | Interchangeable Z-Score and IQR strategies with user-configurable sensitivity thresholds. |
| **FR-07** | Observer Alerts | Automatic event dispatching when spending reaches 80%, breaches 100%, or triggers an outlier flag. |
| **FR-08** | Analytics & Reports| Category spending distribution with ASCII bar charts, monthly cash flow trends, and net savings. |
| **FR-09** | Data Export | Export transaction histories and budget reports to formatted CSV files. |
| **FR-10** | Demo Seeding | Built-in utility to populate realistic test accounts, transactions, and outliers for demonstration. |

---

## 5. Non-Functional Requirements
1. **Performance**: Analytical queries, budget aggregations, and statistical scans process 10,000+ records in < 500ms using $O(N)$ or $O(N \log N)$ algorithms.
2. **Security**: Zero plaintext password storage; constant-time comparison prevents timing attacks; input sanitization protects against malformed entries.
3. **Reliability & Fault Tolerance**: Robust exception handling wraps all I/O, parsing, and numeric operations; application never crashes on invalid user input.
4. **Maintainability**: Layered clean architecture (`model`, `service`, `strategy`, `factory`, `observer`, `persistence`, `cli`, `util`) with comprehensive Javadoc.
5. **Portability**: Pure Core Java application packaged with Maven and standalone execution scripts; runs cross-platform on Windows, macOS, and Linux without native dependencies.
6. **Data Integrity**: Atomic file writes via temporary files and atomic rename operations prevent data corruption during unexpected system termination.
