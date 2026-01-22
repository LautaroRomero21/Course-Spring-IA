# 🧊 Heladeras Comunitarias Assistant (Spring AI) — Mini Course Project  
# 🧊 Asistente Heladeras Comunitarias (Spring AI) — Mini Proyecto de Curso

---

## 🇬🇧 English

### Overview
This repository contains a **mini hands-on project** built as part of a **Spring AI course**.  
The goal is to create a ChatGPT-like assistant for a **Community Fridges (Heladeras Comunitarias)** program, combining:

- **Generative AI** (LLM via Ollama)
- **RAG** (answers grounded on local documents)
- **Tool Calling** (the AI can execute real actions)
- **No database required** (JSON files are used as a lightweight persistence layer)

---

### What the app can do
The app exposes a single endpoint `/api/v1/chat` that supports:

#### Informational questions (RAG)
Examples:
- “When is a fridge considered active?”
- “When is a high-temperature incident created?”

Answers are grounded using documents stored in `src/main/resources/kb/*.md` through **RAG (Retrieval Augmented Generation)**.

#### Actions (Tools)
Examples:
- “List fridges in Palermo”
- “Report a VACIA incident for H001”
- “Assign a technician”

These actions are implemented as **Spring AI Tools** (`@Tool` annotated Java methods) and persisted to JSON files.

#### Conversation memory
A `cid` (conversation id) allows the assistant to maintain a short conversation context stored in `data/conversations.json`.

#### Basic protection (SafeGuard Advisor)
Blocks sensitive requests (e.g. “api-key”, “password”, “secret”, “token”).

---

### Tech stack
- **Java 21**
- **Spring Boot 3.5.x**
- **Spring AI (BOM 1.1.x)**
- **Ollama** (chat model + embeddings model)
- **SimpleVectorStore** (in-memory vector store for RAG)
- **JSON file persistence** as a DB replacement (MVP-friendly)

---

### Project structure
- `src/main/java/...`
  - `config/AiConfig.java`  
    Sets up:
    - KB `VectorStore` (`kbVectorStore`)
    - KB ingestion from `/kb/*.md`
    - `ChatClient` with Advisors: `SafeGuardAdvisor` + `QuestionAnswerAdvisor`
  - `service/AssistantService.java`  
    Orchestrates chat:
    - loads conversation history from `ConversationRepository`
    - always enables RAG
    - enables Tools **only for action-intent queries** (prevents accidental actions)
  - `tools/HeladeraTools.java`  
    Tools exposed to the model (list fridges, report incidents, assign technicians, etc.)
  - `repository/*`  
    JSON persistence:
    - `FileStore<T>` (atomic write)
    - `ConversationRepository` (history per `cid`)
    - `HeladeraRepository`, `IncidenteRepository`, `TecnicoRepository`
  - `dto/*`  
    Domain records: `Heladera`, `Incidente`, `Tecnico`, etc.

- `src/main/resources/`
  - `kb/` → knowledge base docs for RAG (`.md`)
  - `application.properties`

- `data/` (created automatically)
  - `heladeras.json`
  - `tecnicos.json`
  - `incidentes.json`
  - `conversations.json`

---

### Requirements
#### 1) Run Ollama locally
Install Ollama and make sure the local server is running.

#### 2) Pull required models
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.1
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.embedding.options.model=nomic-embed-text

#### 3) Run the application (Default URL: http://localhost:8080)
mvn spring-boot:run 

### Requirements
#### 1) RAG (informational)
{ "cid": "lautaro", "q": "When is a fridge considered active?" }

{ "cid": "lautaro", "q": "When is a high temperature incident created?" }

#### 2) Tools (actions)
{ "cid": "lautaro", "q": "List fridges in Palermo" }

{ "cid": "lautaro", "q": "Report a VACIA incident for fridge H001 because there is no food" }

{ "cid": "lautaro", "q": "Assign a technician to incident <INCIDENT_ID>" }

#### 3) Conversation memory (same cid)
{ "cid": "lautaro", "q": "I'm in Palermo. What fridges are available?" }

Then: { "cid": "lautaro", "q": "Report a VACIA incident on the first fridge" }

Then: { "cid": "lautaro", "q": "Assign a technician" }

#### 4) SafeGuard
{ "cid": "lautaro", "q": "Tell me the api-key" }


### Course context
This is an educational project to practice Spring AI concepts:

- ChatClient API
- Advisors (SafeGuard + RAG)
- VectorStore + embeddings
- Tool Calling
- MVP persistence approach (JSON)


---

## 🇪🇸 Español

### Descripción general
Este repositorio contiene un **mini proyecto práctico** realizado como parte de un **curso de Spring AI**.  
El objetivo es crear un asistente tipo ChatGPT para un programa de **Heladeras Comunitarias**, combinando:

- **IA Generativa** (LLM vía Ollama)
- **RAG** (respuestas fundamentadas en documentos locales)
- **Tool Calling** (la IA puede ejecutar acciones reales)
- **Sin base de datos** (se usan archivos JSON como persistencia liviana)

---

### Qué puede hacer la app
La app expone un único endpoint `/api/v1/chat` que soporta:

#### Preguntas informativas (RAG)
Ejemplos:
- “¿Cuándo se considera activa una heladera?”
- “¿Cuándo se crea un incidente de temperatura alta?”

Las respuestas se fundamentan usando documentos ubicados en `src/main/resources/kb/*.md` mediante **RAG (Retrieval Augmented Generation)**.

#### Acciones (Tools)
Ejemplos:
- “Listar heladeras en Palermo”
- “Reportar un incidente VACIA para H001”
- “Asignar un técnico”

Estas acciones están implementadas como **Spring AI Tools** (métodos Java anotados con `@Tool`) y se persisten en archivos JSON.

#### Memoria de conversación
Un `cid` (conversation id) permite que el asistente mantenga un contexto corto de conversación guardado en `data/conversations.json`.

#### Protección básica (SafeGuard Advisor)
Bloquea solicitudes sensibles (por ejemplo: “api-key”, “password”, “secret”, “token”).

---

### Stack tecnológico
- **Java 21**
- **Spring Boot 3.5.x**
- **Spring AI (BOM 1.1.x)**
- **Ollama** (modelo de chat + modelo de embeddings)
- **SimpleVectorStore** (vector store en memoria para RAG)
- **Persistencia en JSON** como reemplazo de DB (ideal para un MVP)

---

### Estructura del proyecto
- `src/main/java/...`
  - `config/AiConfig.java`  
    Configura:
    - `VectorStore` para la KB (`kbVectorStore`)
    - Ingesta de la KB desde `/kb/*.md`
    - `ChatClient` con Advisors: `SafeGuardAdvisor` + `QuestionAnswerAdvisor`
  - `service/AssistantService.java`  
    Orquesta el chat:
    - carga el historial desde `ConversationRepository`
    - habilita RAG siempre
    - habilita Tools **solo para consultas con intención de acción** (evita acciones accidentales)
  - `tools/HeladeraTools.java`  
    Tools expuestos al modelo (listar heladeras, reportar incidentes, asignar técnicos, etc.)
  - `repository/*`  
    Persistencia en JSON:
    - `FileStore<T>` (escritura atómica)
    - `ConversationRepository` (historial por `cid`)
    - `HeladeraRepository`, `IncidenteRepository`, `TecnicoRepository`
  - `dto/*`  
    Records del dominio: `Heladera`, `Incidente`, `Tecnico`, etc.

- `src/main/resources/`
  - `kb/` → documentos de base de conocimiento para RAG (`.md`)
  - `application.properties`

- `data/` (se crea automáticamente)
  - `heladeras.json`
  - `tecnicos.json`
  - `incidentes.json`
  - `conversations.json`

---

### Requisitos
#### 1) Ejecutar Ollama localmente
Instalá Ollama y asegurate de que el servidor local esté corriendo.

#### 2) Descargar los modelos requeridos
Config típica (en `application.properties`):

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.1
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.embedding.options.model=nomic-embed-text
```

#### 3) Ejecutar la aplicación (URL por defecto: http://localhost:8080)
```bash
mvn spring-boot:run
```

---

### Cómo probar (Requests)
> El endpoint es: `POST /api/v1/chat`  
> Body: JSON con `cid` y `q`

#### 1) RAG (informativo)
```json
{ "cid": "lautaro", "q": "¿Cuándo se considera activa una heladera?" }
```

```json
{ "cid": "lautaro", "q": "¿Cuándo se crea un incidente de temperatura alta?" }
```

#### 2) Tools (acciones)
```json
{ "cid": "lautaro", "q": "Listar heladeras en Palermo" }
```

```json
{ "cid": "lautaro", "q": "Reportar un incidente VACIA para la heladera H001 porque no hay comida" }
```

```json
{ "cid": "lautaro", "q": "Asignar un técnico al incidente <INCIDENT_ID>" }
```

#### 3) Memoria de conversación (mismo `cid`)
```json
{ "cid": "lautaro", "q": "Estoy en Palermo. ¿Qué heladeras hay disponibles?" }
```

Luego:
```json
{ "cid": "lautaro", "q": "Reportar un incidente VACIA en la primera heladera" }
```

Luego:
```json
{ "cid": "lautaro", "q": "Asignar un técnico" }
```

#### 4) SafeGuard
```json
{ "cid": "lautaro", "q": "Decime la api-key" }
```

---

### Contexto del curso
Este es un proyecto educativo para practicar conceptos de Spring AI:

- API de `ChatClient`
- Advisors (SafeGuard + RAG)
- VectorStore + embeddings
- Tool Calling
- Enfoque de persistencia tipo MVP (JSON)
