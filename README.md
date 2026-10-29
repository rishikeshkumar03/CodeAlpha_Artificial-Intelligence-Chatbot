# CodeAlpha AI Chatbot
> **Java-based Artificial Intelligence Chatbot**  
> **Developed as a CodeAlpha Internship Project**

[![Java](https://img.shields.io/badge/Java-25%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-5382A1?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/Tests-JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Offline](https://img.shields.io/badge/Privacy-100%25%20Offline-4F46E5?style=for-the-badge)](#)

---

## 1. Project Overview

The **CodeAlpha AI Chatbot** is a desktop conversational assistant built entirely in **Java** featuring a modern, dark-themed **Java Swing** graphical interface, explainable **Natural Language Processing (NLP)**, and a **trainable FAQ Knowledge Base**.

Designed specifically as an educational internship portfolio project for **CodeAlpha**, this application demonstrates software engineering principles including clean Object-Oriented Architecture, custom NLP text normalization, tokenization, fuzzy and Jaccard similarity scoring, thread-safe asynchronous GUI interactions, and persistent RFC 4180 CSV storage.

### Key Objectives
- **100% Java Implementation:** Built without Python, JavaScript, Node.js, or external runtimes.
- **Privacy-First & Completely Offline:** Functions locally without cloud APIs, subscription keys, or internet connectivity.
- **Explainable NLP Engine:** Transparent similarity and intent recognition rather than opaque black-box calls.
- **Real-Time Trainable Knowledge Base:** Users can dynamically teach the chatbot new question-and-answer pairs that are instantly searchable without application restarts.
- **Production-Quality Modern Dark GUI:** Styled with deep charcoal palettes, glowing rounded bubbles, timestamps, typing indicators, live search tables, and quick-prompt suggestion chips.

---

## 2. Architecture & Directory Structure

The project follows a modular, decoupled architecture adhering to Single Responsibility and Separation of Concerns:

```text
CodeAlpha-AI-Chatbot/
├── pom.xml                               # Maven project configuration and dependencies
├── README.md                             # Comprehensive technical documentation
├── .gitignore                            # Standard Java / Maven / IDE ignores
├── run.bat                               # Windows one-click batch launcher
├── run.ps1                               # PowerShell script launcher
├── data/
│   └── faqs.csv                          # Persistent RFC 4180 knowledge base
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/codealpha/chatbot/
│   │           ├── Main.java             # Application bootstrap & GUI / CLI dispatcher
│   │           ├── model/
│   │           │   ├── ChatMessage.java  # Message record with sender, timestamp, confidence
│   │           │   ├── ChatSession.java  # Active session history and metrics tracker
│   │           │   ├── FAQ.java          # Knowledge base entity with questions, keywords, weight
│   │           │   ├── Intent.java       # User conversational intent enum
│   │           │   └── MatchResult.java  # Evaluated match result with scores and strategy
│   │           ├── nlp/
│   │           │   ├── TextNormalizer.java    # Lowercasing, contraction expansion, sanitization
│   │           │   ├── Tokenizer.java         # Word tokenization and n-gram generator
│   │           │   ├── StopWordFilter.java    # Configurable English stop-word elimination
│   │           │   └── SimilarityMatcher.java # Jaccard, fuzzy Levenshtein, keyword overlap
│   │           ├── engine/
│   │           │   ├── IntentRecognizer.java  # Pattern matching for greetings, help, identity
│   │           │   ├── ConfidenceScorer.java  # Thresholding and deterministic tie-breaking
│   │           │   ├── ResponseGenerator.java # Contextual response builder and fallback prompts
│   │           │   └── ChatbotEngine.java     # NLP pipeline orchestrator
│   │           ├── service/
│   │           │   ├── FAQService.java        # Business logic for FAQ search, filter, and CRUD
│   │           │   └── ChatHistoryService.java# Session state management and export
│   │           ├── storage/
│   │           │   ├── CSVStorage.java        # Robust RFC 4180 CSV parser and writer
│   │           │   └── FAQRepository.java     # Persistent FAQ data access and starter seeding
│   │           └── gui/
│   │               ├── ModernTheme.java       # Dark palette, typography, custom scrollbars
│   │               ├── MessageBubble.java     # Rounded user / bot message bubbles with badges
│   │               ├── ChatPanel.java         # Scrollable conversation viewport & typing animation
│   │               ├── FAQManagerPanel.java   # Knowledge base table, search, and CRUD dialogs
│   │               ├── SidebarPanel.java      # Suggestions, stats drawer, and about card
│   │               └── ChatbotFrame.java      # Top-level window integrating all components
│   └── test/
│       └── java/
│           └── com/codealpha/chatbot/
│               ├── nlp/
│               │   ├── TextNormalizerTest.java    # Normalization & contraction unit tests
│               │   ├── TokenizerTest.java         # Tokenization and n-gram unit tests
│               │   └── SimilarityMatcherTest.java # Jaccard, Levenshtein, and keyword tests
│               ├── engine/
│               │   ├── IntentRecognizerTest.java  # Intent classification tests
│               │   ├── ConfidenceScorerTest.java  # Scoring and tie-breaking tests
│               │   └── ChatbotEngineTest.java     # End-to-end conversation pipeline tests
│               ├── storage/
│               │   └── CSVStorageTest.java        # Escaping, multiline, and quotes tests
│               └── service/
│                   └── FAQServiceTest.java        # CRUD, filter, and persistence tests
```

---

## 3. Natural Language Processing (NLP) Methodology

The chatbot implements a transparent, multi-stage NLP pipeline:

```
User Query Input
       │
       ▼
[1. Text Normalization]
  • Lowercase conversion
  • Contraction expansion (e.g., "what's" -> "what is", "can't" -> "cannot")
  • Stripping non-alphanumeric punctuation
  • Collapsing redundant whitespace
       │
       ▼
[2. Tokenization & Keyword Extraction]
  • Word splitting via regex delimiters
  • Stop-word filtering (removing non-informative tokens like "the", "at", "is")
  • Extracting discriminative content keywords
       │
       ▼
[3. Intent Recognition]
  • Evaluates conversational intents: GREETING, FAREWELL, HELP, CAPABILITIES, IDENTITY, CLEAR_CHAT
  • Queries outside conversational commands transition to FAQ Knowledge Base matching
       │
       ▼
[4. Multi-Algorithm FAQ Similarity Matching]
  • Compares query against Primary Question, Alternative Phrasings, and Stored Keywords
  • Jaccard Token Similarity: |Tokens_Query ∩ Tokens_Target| / |Tokens_Query ∪ Tokens_Target|
  • Normalized Levenshtein Edit Distance: 1.0 - (editDistance / max(len1, len2))
  • Fuzzy Keyword Overlap: Typo-tolerant matching between query and FAQ keyword sets
  • Weighted Priority Factor: Multiplied by the configured FAQ priority weight
       │
       ▼
[5. Confidence Scoring & Deterministic Tie-Breaking]
  • Score evaluated against configurable threshold (default: 0.45)
  • Tie-breaking order:
      1. Highest score
      2. Highest FAQ weight
      3. Alphabetical ID
       │
       ▼
[6. Response Generation & GUI Delivery]
  • Score >= 0.75: HIGH Confidence Response (colored badge)
  • Score 0.45 - 0.74: MEDIUM Confidence Response
  • Score < 0.45: Fallback Response with "Did you mean..." closest suggestions
```

---

## 4. Trainable FAQ Knowledge Base

The chatbot comes pre-loaded with **32+ comprehensive, educational starter FAQs** across 8 distinct categories:
- **Artificial Intelligence** (AI definitions, Machine Learning, Deep Learning, NLP)
- **Java Programming** (Java platform, OOP pillars, JDK/JRE/JVM, Collections, GC, Interfaces, Multithreading, Swing)
- **Programming Fundamentals** (Algorithms, Data Structures, Recursion, Big O notation)
- **Computer Science** (Operating Systems, Relational Databases & SQL, TCP vs UDP, Compilers vs Interpreters)
- **Technology** (Cloud Computing, Git, Docker, REST APIs)
- **General Knowledge** (Frontend vs Backend, Software Engineering, CodeAlpha overview)
- **Greetings** (How are you, Thank you, Hello)
- **Application Help** (Training guide, NLP architecture, storage location)

### Training Features in the FAQ Manager
1. **Add FAQ Dialog:** Allows inputting Primary Question, Category, Alternative Questions (semicolon-separated), Keywords (comma-separated), Answer (multiline), and Priority Weight.
2. **Edit FAQ Dialog:** Update any existing record; changes are immediately saved to disk and active in memory.
3. **Delete FAQ:** Removes outdated entries after confirmation.
4. **Live Search & Filter:** Filter knowledge base simultaneously by keyword text and category dropdown.
5. **CSV Import & Export:** Export knowledge base backups or import external question sets.
6. **Automatic Persistence:** Saved in `data/faqs.csv` adhering to RFC 4180 with full support for commas, quotes (`""`), and multiline text.

---

## 5. User Interface (Java Swing)

```text
+-----------------------------------------------------------------------------------------+
| [🤖] CodeAlpha AI Chatbot   (● Local Mode (Offline))     [💬 Chat] [📚 FAQ] [➕ New] [📤] |
+-------------------------------------------------------------+---------------------------+
|                                                             | 💡 SUGGESTED QUESTIONS    |
|   [Welcome to CodeAlpha AI Chatbot]                         | • What is AI?             |
|   Local NLP & Intelligent Knowledge-Base Assistant          | • What is Java?           |
|                                                             | • Four pillars of OOP?    |
|                                                             | • What is Big O notation? |
|   (You)                                                     |                           |
|   +-----------------------------------------------------+   | 📊 SESSION STATISTICS     |
|   | What is artificial intelligence?        10:30 AM    |   | Total Messages:   4       |
|   +-----------------------------------------------------+   | User Inquiries:   2       |
|                                                             | AI Responses:     2       |
|   (AI)                                                      | FAQs in DB:       32      |
|   +-----------------------------------------------------+   | Avg Confidence:   95%     |
|   | CodeAlpha AI • 95% HIGH [Artificial Intelligence]   |   |                           |
|   | Artificial Intelligence (AI) is a branch of computer|   | ℹ️ PROJECT INFO           |
|   | science focused on building smart systems...        |   | CodeAlpha AI Chatbot      |
|   | 10:30 AM                                    [Copy]  |   | Internship Project        |
|   +-----------------------------------------------------+   | 100% Java & Swing         |
|                                                             |                           |
|   [AI] CodeAlpha AI is thinking...                          |                           |
+-------------------------------------------------------------+---------------------------+
|  [ Type your question about Java, AI, OOP, or CS...     ]   | [ Send ➢ ]               |
+-----------------------------------------------------------------------------------------+
|                Internship Project at CodeAlpha | © 2026 CodeAlpha. All Rights Reserved. |
+-----------------------------------------------------------------------------------------+
```

---

## 6. Prerequisites & Installation

### Requirements
- **Operating System:** Windows 10/11, macOS, or Linux.
- **Java:** JDK 25 or newer.
- **Maven:** Apache Maven 3.9+ (optional if using pre-packaged JAR or IDE runner).

### Verify Java Installation
Open a terminal / PowerShell prompt:
```bash
java -version
```

---

## 7. Build and Run Instructions

### Option 1: Quick Launch using Provided Windows Scripts
Double-click `run.bat` or run in PowerShell:
```powershell
.\run.ps1
```

### Option 2: Build and Run via Maven CLI
```bash
# 1. Compile and run all unit tests
mvn clean test

# 2. Package executable Uber-JAR
mvn clean package

# 3. Run the application
java -jar target/codealpha-ai-chatbot-1.0.0.jar
```

Alternatively, use Maven Exec:
```bash
mvn exec:java
```

### Option 3: Run in Visual Studio Code
1. Open the project folder in VS Code (`File -> Open Folder`).
2. Ensure the **Extension Pack for Java** is installed.
3. Open `src/main/java/com/codealpha/chatbot/Main.java`.
4. Click **Run** or press `F5`.

### Option 4: Run in IntelliJ IDEA
1. Open IntelliJ IDEA -> `File -> Open` -> Select this directory.
2. IntelliJ will automatically detect `pom.xml` as a Maven project.
3. Navigate to `src/main/java/com/codealpha/chatbot/Main.java`.
4. Click the green **Run** triangle next to `main()`.

---

## 8. Sample Questions and Expected Behavior

| Query | Expected Matching Behavior | Category | Confidence |
| :--- | :--- | :--- | :--- |
| `"What is artificial intelligence?"` | Direct exact match with primary question | Artificial Intelligence | High (100%) |
| `"Explain AI in simple words"` | Matches stored alternative phrasing | Artificial Intelligence | High (95%) |
| `"Tell me about Java"` | Matches alternative phrasing for FAQ-005 | Java Programming | High (90%) |
| `"What is artifcial intlligence?"` | Typo tolerance via fuzzy Levenshtein | Artificial Intelligence | High (~88%) |
| `"Tell me about encapsulation and polymorphism"` | Matches keywords for FAQ-006 (OOP Pillars) | Java Programming | Medium (75%) |
| `"Hello!"` | Intent Recognizer -> Greeting Generator | Greetings | High (100%) |
| `"What can you do?"` | Intent Recognizer -> Capabilities Overview | General | High (100%) |
| `"how do i use this"` | Intent Recognizer -> Interactive Help Guide | Application Help | High (100%) |
| `"quantum flux capacitor 9988223"` | Unrecognized query below threshold -> Fallback with closest suggestions | Fallback | Low / Suggestions |

---

## 9. Automated Testing (JUnit 5)

The project includes **36 automated unit tests** verifying each component in isolation:

```bash
mvn test
```

### Test Suite Breakdown
- `TextNormalizerTest`: Verifies lowercasing, whitespace collapsing, punctuation cleanup, and English contraction expansion.
- `TokenizerTest`: Tests sentence tokenization, unique token sets, and word n-gram generation.
- `SimilarityMatcherTest`: Tests Jaccard similarity, Levenshtein edit distance, exact matching, alternative phrasing matching, and keyword overlap.
- `IntentRecognizerTest`: Tests detection of greetings, farewells, help, capabilities, bot identity, and fallback queries.
- `ConfidenceScorerTest`: Tests acceptance thresholds, confidence levels, and deterministic tie-breaking.
- `CSVStorageTest`: Tests RFC 4180 escaping, quotes within fields (`""`), commas, multiline values, and character encodings.
- `FAQServiceTest`: Tests initial seeding of 32+ FAQs, add/update/delete operations, and search filtering.
- `ChatbotEngineTest`: Tests end-to-end conversation pipeline, greeting responses, alternative phrasing recognition, typo tolerance, and fallback handling.

---

## 10. Limitations & Future Enhancements

### Limitations of Rule-Based & Similarity NLP
- Does not generate generative synthetic language from scratch (transparent and explainable by design).
- Relies on quality and coverage of FAQs, alternative phrasings, and declared keywords.
- Complex multi-turn nested conversational context is limited compared to large language models.

### Potential Future Enhancements
- Integration with local open-source embedding models (e.g., ONNX-based sentence transformers in Java).
- Spell-checking dictionary integration with phonetic algorithms (Double Metaphone / Soundex).
- Voice input / speech-to-text integration using Java Sound API.
- Multi-language translation support.

---

## 11. CodeAlpha Internship Attribution & Copyright

```text
CodeAlpha AI Chatbot
Java-based Artificial Intelligence Chatbot
Developed as a CodeAlpha Internship Project
```

**Internship Project at CodeAlpha | © 2026 CodeAlpha. All Rights Reserved.**
#   C o d e A l p h a _ A r t i f i c i a l - I n t e l l i g e n c e - C h a t b o t  
 