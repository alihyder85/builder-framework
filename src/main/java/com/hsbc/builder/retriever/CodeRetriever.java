package com.hsbc.builder.retriever;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.Content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import java.util.List;
import dev.langchain4j.rag.query.Query;

@Slf4j
@Component
//@RequiredArgsConstructor
public class CodeRetriever {

    private final ContentRetriever retriever;

    public CodeRetriever(ContentRetriever retriever) {
    this.retriever = retriever;
    log.info("Injected ContentRetriever: " + System.identityHashCode(retriever));
    }

    public List<Content> retrieveRelevantSnippets(String query) {
        Query q = new Query(query);
        return retriever.retrieve(q);
    }
}

