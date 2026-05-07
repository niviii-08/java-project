# 🧭 Pathfinder — AI-Powered Career Guidance System

> A Java desktop application for Indian students and professionals to get personalised AI-driven career guidance, roadmaps, and recommendations.

---

## 📁 Project Structure

```
PathfinderApp/
├── src/
│   └── com/pathfinder/
│       ├── Main.java                  ← Entry point
│       ├── api/
│       │   └── OpenRouterClient.java  ← AI API integration
│       ├── db/
│       │   ├── DBConnection.java      ← MySQL JDBC connection
│       │   ├── UserDAO.java           ← Database operations
│       │   └── PasswordUtil.java      ← Password hashing (SHA-256 + salt)
│       ├── model/
│       │   ├── User.java              ← User model
│       │   └── Session.java           ← Session singleton
│       ├── ui/
│       │   ├── SplashScreen.java      ← Landing page (Swing)
│       │   ├── AuthFrame.java         ← Login / Register
│       │   ├── DashboardFrame.java    ← Main app shell with sidebar
│       │   ├── UIComponents.java      ← Reusable dark-theme components
│       │   ├── HomePanel.java         ← Welcome screen
│       │   ├── ProfilePanel.java      ← Student / Professional profile form
│       │   ├── CareerPanel.java       ← AI career suggestions
│       │   ├── RoadmapPanel.java      ← AI roadmap generator
│       │   └── ChatPanel.java         ← AI chat interface
│       └── util/
│           ├── Theme.java             ← Color/font constants
│           └── IndiaData.java         ← All Indian states & districts
├── lib/
│   └── mysql-connector-java.jar       ← (You must download this)
├── run.sh                             ← Linux/macOS build & run
├── run.bat                            ← Windows build & run
└── README.md
```

---

## ⚙️ Prerequisites

| Requirement | Version | Download |
|---|---|---|
| Java JDK | 17 or higher | https://adoptium.net |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| MySQL Connector/J | 8.x | https://dev.mysql.com/downloads/connector/j/ |
| OpenRouter API Key | Free | https://openrouter.ai |

---

## 🗄️ Database Setup — SQL Commands

Run these commands in MySQL (via MySQL Workbench, DBeaver, or CLI):

### Step 1: Create the database

```sql
CREATE DATABASE IF NOT EXISTS pathfinder_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pathfinder_db;
```

### Step 2: Users table

```sql
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(150)        NOT NULL UNIQUE,
    password_hash VARCHAR(255)        NOT NULL,
    role          ENUM('student','professional') NOT NULL,
    created_at    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME            ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_role  (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 3: Student profiles table

```sql
CREATE TABLE student_profiles (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT          NOT NULL UNIQUE,
    standard_level VARCHAR(50)  NOT NULL COMMENT 'e.g. Class 10, 1st Year College',
    stream         VARCHAR(100)          COMMENT 'e.g. Science PCM, Commerce',
    state          VARCHAR(100),
    district       VARCHAR(100),
    institution    VARCHAR(200)          COMMENT 'School or college name',
    interests      TEXT                  COMMENT 'Comma-separated interests',
    marks_json     TEXT                  COMMENT 'JSON of subject marks',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_standard (standard_level),
    INDEX idx_state     (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 4: Professional profiles table

```sql
CREATE TABLE professional_profiles (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NOT NULL UNIQUE,
    domain           VARCHAR(150) NOT NULL COMMENT 'e.g. Software Engineering, Healthcare',
    years_experience VARCHAR(20)           COMMENT 'e.g. 3-5 years',
    skills           TEXT                  COMMENT 'Comma-separated skills',
    interests        TEXT                  COMMENT 'Career interests and goals',
    current_role     VARCHAR(150)          COMMENT 'Job title',
    company          VARCHAR(150)          COMMENT 'Current employer',
    state            VARCHAR(100),
    district         VARCHAR(100),
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_domain (domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 5: AI results table

```sql
CREATE TABLE ai_results (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    result_type VARCHAR(50)  NOT NULL COMMENT 'e.g. career_suggestions, roadmap',
    result_json LONGTEXT     NOT NULL COMMENT 'Full AI response JSON',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_type (user_id, result_type),
    INDEX idx_created   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 6: Verify tables were created

```sql
SHOW TABLES;
-- Expected output:
-- ai_results
-- professional_profiles
-- student_profiles
-- users

DESCRIBE users;
DESCRIBE student_profiles;
DESCRIBE professional_profiles;
DESCRIBE ai_results;
```

---

## 🔑 Configuration — Placeholders to Replace

### 1. Database Password
Open `src/com/pathfinder/db/DBConnection.java`:

```java
private static final String PASSWORD = "YOUR_DB_PASSWORD_HERE";
//                                       ^^^^^^^^^^^^^^^^^^^^
//                                       Replace with your MySQL root password
```

Also update `USERNAME` if you're not using `root`:
```java
private static final String USERNAME = "root"; // change if needed
```

### 2. OpenRouter API Key
Instead of editing Java files, use the `config.properties` file in the project root:

```properties
OPENROUTER_API_KEY=your_key_here
```

**How to get a free OpenRouter API key:**
1. Go to https://openrouter.ai
2. Sign up (free)
3. Navigate to **Keys** in your dashboard
4. Create a new key
5. Copy and paste it into the file above

**Model used:** `mistralai/mistral-7b-instruct:free` (completely free, no credits needed)

---

## 🚀 How to Run

### Step 1: Place the JDBC driver
Download `mysql-connector-j-8.x.x.jar` from MySQL website.
Rename it to `mysql-connector-java.jar` and place it in the `lib/` folder.

### Step 2: Set up the database
Run all SQL commands from the section above in MySQL.

### Step 3: Edit configuration
Set your OpenRouter API key in `config.properties` and your DB password in `DBConnection.java`.

### Step 4: Build and run

**Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

**Windows:**
```
Double-click run.bat
```

**Or manually (any OS):**
```bash
mkdir out
javac -cp "lib/mysql-connector-java.jar" -d out $(find src -name "*.java")
java  -cp "out:lib/mysql-connector-java.jar" com.pathfinder.Main
# On Windows use semicolon: "out;lib/mysql-connector-java.jar"
```

---

## 🎨 Theme & Design

The app uses a dark **Pathfinder design system**:

| Token | Value | Usage |
|---|---|---|
| BG | `#07080C` | Page background |
| BG2 | `#0E1018` | Sidebar, panels |
| CARD | `#191D27` | Card backgrounds |
| TEAL | `#00D4AA` | Students, primary accent |
| AMBER | `#FFB347` | Professionals, secondary accent |
| PURPLE | `#9B6DFF` | Gradients, highlights |
| BLUE | `#4F8EF7` | Info, links |
| ROSE | `#FF5F7E` | Errors, warnings |
| GREEN | `#3DD68C` | Success states |

---

## 🧠 AI Model Details

- **Provider:** OpenRouter (https://openrouter.ai)
- **Model:** `mistralai/mistral-7b-instruct:free`
- **Cost:** Free (no payment required)
- **Capabilities:**
  - Career suggestions with match percentages
  - Step-by-step career roadmaps
  - Conversational guidance
  - Spelling correction for skills/interests

The model is prompted with a detailed **Indian career guidance system prompt** covering:
- All Indian academic standards (Class 6–12, UG, PG)
- Stream-specific careers (PCM → Engineering/JEE, PCB → Medical/NEET, etc.)
- Indian competitive exams (JEE, NEET, GATE, CAT, UPSC, CLAT, NDA...)
- Indian universities (IITs, NITs, AIIMS, DU, Mumbai University...)
- Rupee-based salary ranges
- Professional career pivots and upskilling

---

## 📋 Feature Summary

| Feature | Students | Professionals |
|---|---|---|
| Profile Setup | Class 6–12 + College, adaptive questions | Domain, skills, experience, role |
| Location | All Indian states + districts | All Indian states + districts |
| Career Suggestions | Based on standard + stream + interests | Based on domain + skills + goals |
| Roadmap | Academic → exam → college pathway | Upskilling → promotion → pivot |
| AI Chat | Exam info, career Q&A | Market trends, certifications |
| Spelling Correction | Interests field | Skills + interests fields |

---

## 🐛 Troubleshooting

| Issue | Fix |
|---|---|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | JDBC jar not in `lib/` or wrong classpath |
| `Communications link failure` | MySQL server not running, or wrong host/port |
| `Access denied for user 'root'` | Wrong password in `DBConnection.java` |
| `API Error 401` | Invalid OpenRouter API key |
| `API Error 429` | Rate limit hit — wait a minute and retry |
| Window doesn't appear | Check Java version: `java -version` (need 17+) |
| Blank/grey window on Linux | Try adding `-Dawt.useSystemAAFontSettings=on` to java command |

---

## 📌 Notes

- All passwords are stored as **SHA-256 hashed with random salt** (never plain text)
- No data is sent to any server except the OpenRouter API (career data for AI inference)
- The app works offline except for AI features (career analysis, chat, roadmap generation)
- The `lib/` folder is excluded from source control — you must download the JDBC driver yourself
