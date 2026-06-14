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



**Model used:** `mistralai/mistral-7b-instruct:free` (completely free, no credits needed)

##  Feature Summary

| Feature | Students | Professionals |
|---|---|---|
| Profile Setup | Class 6–12 + College, adaptive questions | Domain, skills, experience, role |
| Location | All Indian states + districts | All Indian states + districts |
| Career Suggestions | Based on standard + stream + interests | Based on domain + skills + goals |
| Roadmap | Academic → exam → college pathway | Upskilling → promotion → pivot |
| AI Chat | Exam info, career Q&A | Market trends, certifications |
| Spelling Correction | Interests field | Skills + interests fields |

