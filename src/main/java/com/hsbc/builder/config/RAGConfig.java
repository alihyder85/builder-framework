package com.hsbc.builder.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Slf4j
@Configuration
public class RAGConfig {

    @Value("${builderframework.rag.max-results:5}")
    private int maxResults;

    @Value("${builderframework.rag.min-score:0.4}")
    private double minScore;

    @Value("${builderframework.rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${builderframework.rag.chunk-overlap:200}")
    private int chunkOverlap;

    private final ConcurrentHashMap<String, Long> documentVersions = new ConcurrentHashMap<>();

    @Bean
    public TokenCountEstimator tokenCountEstimator(){
        return new HuggingFaceTokenCountEstimator();
    }

    @Bean(name = "ollamaChatMemoryProvider")
    ChatMemoryProvider chatMemoryProvider(TokenCountEstimator tokenizer) {
        return memoryId -> TokenWindowChatMemory.builder()
            .id(memoryId)
            .maxTokens(10_000, tokenizer)
            .build();
    }

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("llama3")
            .timeout(Duration.ofMinutes(5))
            .build();
    }

    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader) throws IOException {
        log.info("Initializing embedding store with RAG configuration");

        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        int totalEmbeddings = 0;

        // Load all templates from resources/templates
        Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath:templates/*.template");

        TextDocumentParser parser = new TextDocumentParser();

        for (Resource resource : resources) {
            Path contentPath = resource.getFile().toPath();
            Document document = loadDocument(contentPath, parser);

            // Track document version for potential updates
            documentVersions.put(resource.getFilename(), Files.getLastModifiedTime(contentPath).toMillis());

            log.info("Loading document: {} (size: {} bytes)",
                document.metadata().getString("file_name"),
                document.text().length());

            // Use ClassLevelSplitter instead of DocumentSplitters.recursive
            DocumentSplitter splitter = new ClassLevelSplitter();

            // Ingest document into embedding store
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

            ingestor.ingest(document);

            // Count embeddings added for this document
            int embeddingsAdded = splitter.split(document).size();
            totalEmbeddings += embeddingsAdded;

            // Log the number of embeddings stored after ingestion
            log.info("Document '{}' ingested. Total embeddings in store: {}", 
            document.metadata().getString("file_name"), 
            totalEmbeddings);
        }

        return embeddingStore;
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                      EmbeddingModel embeddingModel) {
        log.info("Creating content retriever with maxResults={}, minScore={}", maxResults, minScore);

        EmbeddingStoreContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(maxResults)
            .minScore(minScore)
            .build();
        log.info("RAGConfig ContentRetriever bean: {}", System.identityHashCode(embeddingStoreContentRetriever));    
        return embeddingStoreContentRetriever;
    }

}