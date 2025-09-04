# Builder Framework MVP

A hackathon-ready framework to generate containerized microservice apps using LLaMA 3, RAG, and Spring Boot.

## Structure

- `builder-backend/`: Spring Boot backend (core logic, RAG, generation)
- `templates/`: Microservice/component templates
- `retriever/`: Embedding and retrieval scripts (Python, FAISS/Chroma)
- `cli/`: CLI or prompt UI (optional)
- `docker-compose.yml`: One-click local deployment

## Quick Start

1. Clone your GitHub repos locally for embedding.
2. Run embedding scripts in `retriever/`.
3. Start the backend: `cd builder-backend && ./mvnw spring-boot:run`
4. Use CLI or API to generate apps from prompts.

## Requirements

- Java 17+
- Python 3.9+
- Docker

## Next Steps

- Implement RAG modules in `builder-backend/src/main/java/com/builder/rag/`
- Add microservice templates in `templates/`
- Wire up LLaMA 3 via langchain4j
