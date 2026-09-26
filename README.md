# CareerForge — AI Resume & Career Builder v2.0

A JavaFX 21 desktop application for building resumes and cover letters,
with an optional Gemini AI integration (automatic local fallback when no
API key is configured). Built for a university Java coursework project.

---

## 1. Requirements

- **JDK 21** (Temurin, Oracle, or OpenJDK all work)
- **Apache Maven 3.9+**
- Internet access the first time you build (Maven downloads JavaFX,
  SQLite JDBC, Jackson, and PDFBox from Maven Central)

---

## 2. Project Layout

```
CareerForge/
├── pom.xml
└── src/main/
    ├── java/com/aizen/
    │   ├── Main.java                  Application entry point
    │   ├── model/                     Person → Applicant → Student, Resume, User, etc.
    │   ├── db/                        Database connection + schema setup
    │   ├── dao/                       GenericDAO<T,ID>, UserDAO, ResumeDAO
    │   ├── controller/                One controller per FXML screen
    │   ├── service/                   ApiService, PdfService, JsonService, AtsService, ...
    │   ├── thread/                    TaskManager + Task subclasses
    │   ├── exception/                 Custom checked/unchecked exceptions
    │   └── util/                      SceneManager, ValidationUtil, JsonUtil, PasswordUtil
    └── resources/com/aizen/
        ├── fxml/                      9 screens
        └── css/                       style.css (light), dark.css
```

---

## 3. Setup: IntelliJ IDEA

1. **File → Open** → select the `aizen` folder (the one containing `pom.xml`).
2. IntelliJ detects the Maven project automatically and prompts to import —
   click **Load Maven Project** (or use the Maven tool window → refresh icon).
3. Wait for dependencies to download (bottom-right progress bar).
4. Set the Project SDK: **File → Project Structure → Project → SDK → 21**.
5. Configure the API key (optional — see Section 5) as a Run Configuration
   environment variable: **Run → Edit Configurations → + → Maven** →
   Command line: `clean javafx:run` → Environment variables:
   `AIZEN_API_KEY=your_key_here`.
6. Run the configuration, or use the terminal (Section 4).

## Setup: Eclipse

1. **File → Import → Maven → Existing Maven Projects** → browse to the
   `aizen` folder → **Finish**.
2. Right-click the project → **Maven → Update Project** to force a
   dependency refresh if needed.
3. Ensure a JDK 21 is registered: **Window → Preferences → Java →
   Installed JREs**, add your JDK 21 if it isn't listed, and set it as
   default (or set it per-project under **Build Path → Libraries**).
4. To run with the API key set, use **Run → Run Configurations → Maven
   Build** → Goals: `clean javafx:run` → **Environment** tab → add
   `AIZEN_API_KEY`.
5. Run the configuration, or use the terminal (Section 4).

---

## 4. Running from the Terminal

```bash
cd aizen

# Optional — enables live Gemini AI generation instead of the local fallback
export AIZEN_API_KEY="your_gemini_api_key_here"      # macOS/Linux
setx AIZEN_API_KEY "your_gemini_api_key_here"         # Windows (new terminal after)

mvn clean javafx:run
```

The first run creates `aizen.db` (SQLite file) in the working directory
and auto-creates all tables — no manual DB setup required.

---

## 5. Configuring `AIZEN_API_KEY`

1. Get a free API key at https://aistudio.google.com/app/apikey
2. Set it as an environment variable named exactly `AIZEN_API_KEY`
   (never hard-code it in source — the app reads it via
   `System.getenv("AIZEN_API_KEY")` in `ApiService`).
3. If the variable is missing, unreadable, or the network call fails for
   any reason, AiZen **automatically and silently** falls back to
   `LocalFallbackEngine`, a built-in heuristic text generator — the app
   never crashes or blocks on the AI feature being unavailable. You can
   see which mode is active on the **Settings** screen and via the
   colored status dot in the top bar.

---

## 6. Feature Walkthrough

- **Register / Login** — accounts are stored in SQLite with a salted
  SHA-256 password hash (`PasswordUtil`); session is held in memory for
  the life of the app (`SceneManager.currentUser`).
- **Dashboard** — saved-resume count, recent-activity feed, and quick
  action tiles, loaded via a background `Task` so the DB read never
  blocks the UI.
- **Resume Builder** — tabbed form (Personal / Experience / Education /
  Projects / Certifications / Skills) on the left, live plain-text
  preview on the right that updates automatically as you edit. Save,
  Export PDF, Export/Import JSON, and a one-click ATS keyword check are
  all in the toolbar; each dispatches to its own background `Task`.
- **Cover Letter Generator** — pick a tone, enter role/company/job
  description, and generate — via Gemini if configured, otherwise the
  local fallback — with one-click clipboard copy and PDF export.
- **Saved Resumes** — TableView of everything you've saved, with View
  (read-only modal), Edit (re-opens in the builder), Duplicate, Export
  PDF, and Delete (with a confirmation dialog).
- **Settings** — light/dark theme toggle, AI-engine status, account info,
  and logout.

---

## 7. How the Academic Requirements Were Met

| Requirement | Where |
|---|---|
| **Abstraction** | `Person` is an abstract class; `getRole()` is an abstract method every subclass must implement. |
| **Inheritance** | `Person` → `Applicant` → `Student`, a three-level chain. |
| **Polymorphism / Overriding** | `Applicant.getRole()` returns `"Applicant"`; `Student.getRole()` overrides it to return `"Student"`. Called polymorphically through a `Person` reference. |
| **Method Overloading** | `Applicant.updateProfile(...)` has 3 overloads (2/3/4 args); `Student.updateProfile(...)` adds a 4th, 6-argument overload — same method name, different signatures, resolved at compile time. |
| **Encapsulation** | Every model class keeps fields `private`, exposes only validated getters/setters (`Person.setEmail`, `Student.setCgpa`, etc.) that throw `ValidationException` on bad input. |
| **Generics** | `GenericDAO<T, ID>` is implemented by both `UserDAO` (`User, Integer`) and `ResumeDAO` (`Resume, Integer`) without duplicating CRUD method signatures. |
| **Collections** | `ObservableList` (JavaFX-bound, used by `Resume`'s child entity lists and every `TableView`), plain `List`/`ArrayList` in the DAO and service layers, `Map`-free but `Optional<T>` used throughout DAOs for null-safety. |
| **Concurrency** | `TaskManager` wraps a fixed 4-thread `ExecutorService`. Every DB write (`SaveTask`), PDF render (`PdfTask`), AI call (`ApiTask`), and ATS scan (`AtsTask`) extends `javafx.concurrent.Task` and is submitted through it — never run on the FX Application Thread. UI updates from task callbacks use `Platform.runLater` / JavaFX's own thread-safe `setOnSucceeded`/`setOnFailed` hooks. |
| **Defensive Exception Handling** | 4 custom exceptions (`DatabaseException`, `ValidationException`, `ApiException`, `PdfGenerationException`) are thrown by the model/service/DAO layers and caught at the controller boundary, surfaced to the user via `Alert` dialogs — the app never shows a raw stack trace or silently swallows an error. |

---

## 8. Notes & Known Trade-offs

- The resume preview is a formatted plain-text `TextArea` rather than a
  `WebView`/HTML renderer, to keep the dependency surface small and the
  preview trivially exportable as-is; PDF export (Apache PDFBox) is the
  actual polished output artifact and does apply per-template spacing.
- `ResumeDAO.save`/`update` write the parent row and replace all child
  rows inside a single manual transaction (`setAutoCommit(false)` +
  `commit()`), so a resume and its Experience/Education/Project/
  Certification/Skill rows are always consistent.
- This project was written and its ~45 Java source files were verified
  to compile cleanly against a JDK 21 + JavaFX toolchain; the four files
  that depend on Jackson/PDFBox/SQLite (`ApiService`, `JsonService`,
  `PdfService`, `JsonUtil`) were reviewed by hand against those
  libraries' current APIs since Maven Central wasn't reachable in the
  authoring sandbox. Run `mvn clean compile` first if you want to
  double-check before `javafx:run`.
