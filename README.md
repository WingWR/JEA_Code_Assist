# JEA Code Assist

## Overview

**JEA Code Assist** is an IntelliJ IDEA plugin designed as a **Teaching Assistant (TA)** for the *Java Enterprise Application Development* course.  
It provides an AI-powered assistant with **Retrieval-Augmented Generation (RAG)** and **source citation**, enabling students and instructors to interactively query course materials directly within IntelliJ IDEA.

---

## Key Features



### Course-Specific Q&A with RAG

- The plugin preprocesses course materials (e.g., lecture slides, notes, and PDFs) into **knowledge chunks**.
- These chunks are indexed for fast semantic retrieval.
- When a user asks a question, the plugin retrieves the most relevant materials and provides them to an **LLM** (Large Language Model).
- The answer combines general Java knowledge with **verified, course-specific content**.

### Source Citation

- Every AI-generated answer explicitly cites the **document source and page number**.  

- If no relevant course material is found, the assistant clearly states:  

  > “Response is based on general knowledge; no specific course material is referenced.”

### Context-Aware Assistance

- Supports **right-click context menu** actions in the code editor.
- Users can select a code snippet and ask a question directly related to it.
- The assistant retrieves relevant knowledge considering the code context.

### User Interface

- Integrated **Tool Window (sidebar)** in IntelliJ IDEA.
- Provides a chat interface for interactive Q&A.
- Displays **context tags** for added code snippets.
- Context tags are visually appealing with dark theme support, rounded borders, and responsive wrapping.

---

## Architecture Overview

### System Components

1. **Frontend (Plugin UI)**
   - Developed using IntelliJ Swing components.
   - Contains chat interface, context panel, and tool window.
2. **Backend (Service Layer)**
   - Handles context management, course material indexing, and RAG pipeline orchestration.
3. **RAG Module**
   - Preprocesses materials → creates embeddings → stores vectors → retrieves top relevant chunks.
4. **LLM Integration**
   - Sends retrieved context and user query to the LLM API.
   - Returns enriched, source-cited responses.
5. **Persistence**
   - Stores cached embeddings and recent query history for improved efficiency.

### Data Flow

```text
User Query → Context Manager → Retriever (Vector Search) → LLM (w/ Context) → Response + Citation → UI Display
```

---

## Installation & Setup

### 1. Requirements

- IntelliJ IDEA 2023.2 or later  
- Java 17+  
- OpenAI or Azure-compatible API key for LLM integration  

### 2. Installation Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/your-org/JEA_Code_Assist.git
   cd JEA_Code_Assist
   ```

2. Open the project in IntelliJ IDEA.  

3. Build and run the plugin via the IntelliJ Plugin DevKit.  

4. Install the plugin (via `Install Plugin from Disk...`) in your IntelliJ.

### 3. API Key Configuration

In IntelliJ:  
`File → Settings → Tools → JEA Code Assist → API Key`  
Enter your valid LLM API key.

---

## How to Use

### Adding Context

1. Highlight code/choose a file in the editor.  
2. Right-click → `Ask Teachinkg Assistant`/`Add to AI Context`.  
3. The selected snippet is added as a **context tag** in the sidebar.

### Asking Questions

1. Open the **JEA Assistant Tool Window**.  
2. Type your question in the chat box.  
3. The assistant will respond with an answer and cite related course material.

### Deleting Context

- Click the ❌ button on a context tag to remove it.

---

## Example Use Case

**User Action:**

> Highlights a servlet initialization block and asks:  
> “Why do we use `@WebServlet` annotation instead of `web.xml`?”

**Assistant Response:**

> “In Java EE 6 and later, `@WebServlet` allows annotation-based servlet declaration, simplifying deployment.  
> (Source: Lecture 3 - Page 12)”

---

## Technical Highlights

| Component           | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| **RAG Indexing**    | Uses text chunking and vector embeddings for semantic retrieval. |
| **LLM Integration** | Context-aware prompt engineering with citation injection.    |
| **Context Manager** | Tracks selected code snippets and manages user context.      |
| **Tool Window UI**  | Modern, responsive, dark-themed design with interactive tags. |

---

## Team Collaboration & Workflow

- **Version Control**: Git + GitHub branching strategy.  
- **Collaboration**: PR reviews, issue tracking, and feature-driven commits.  
- **Development Tools**: IntelliJ Plugin SDK, Gradle, Aliyun API.

---

## Presentation Checklist

| Item                      | Description                                               |
| ------------------------- | --------------------------------------------------------- |
| **Functional Demo**       | Show RAG-based Q&A, citation, and context awareness.      |
| **Architecture Overview** | Present components, data flow, and efficiency mechanisms. |
| **Collaboration**         | Explain version control workflow and task division.       |
| **Design Challenges**     | Discuss major design tradeoffs and optimizations.         |

---

## License

This project is licensed under the **MIT License**.

---

## Future Improvements

- Multi-document citation.  
- Visual diff for code-related answers.  
- Improved caching and offline query capabilities.  
- Support for other LLM providers (Claude, Gemini, etc.).

---

**JEA Code Assist** – Bringing Smart Teaching Assistance into Java Enterprise Learning.
