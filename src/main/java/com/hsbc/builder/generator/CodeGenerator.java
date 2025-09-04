package com.hsbc.builder.generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.DefaultContent;
import dev.langchain4j.data.message.AiMessage;
import java.util.List;
import dev.langchain4j.model.chat.ChatModel;

@Slf4j
@Component
public class CodeGenerator {

    private final ChatModel chatModel;

    public CodeGenerator(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

public String generateServiceStub(String module, List<Content> context) {
        log.info("Generating project for module: {}", module);

        String prompt = buildPrompt(module, context);

        // Ask LLaMA3
        String aiOutput = chatModel.chat(prompt);

        log.info("Final prompt sent to AI:\n{}", prompt);

        log.info("AI raw output:\n{}", aiOutput);

        // Parse and save files
        return aiOutput;

      }

      private String buildPrompt(String module, List<Content> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Spring Boot microservice code generator.\n");
        prompt.append("Generate a full project for module: ").append(module).append("\n\n");

        // Append retrieved context
        prompt.append("Here is some relevant reference context from an existing application:\n");
        for (Content content : context) {
            if (content instanceof DefaultContent defaultContent) {
                TextSegment textSegment = defaultContent.textSegment();
                if (textSegment != null) {
                    prompt.append("---\n").append(textSegment.text()).append("\n");
                } else {
                    log.warn("TextSegment is null for content: {}", content);
                }
            } else {
                log.warn("Content is not of type DefaultContent: {}", content);
            }
        }
        prompt.append("---\n\n");

        // Strict output format
        prompt.append("""
            Strictly follow this format for every file:
            FILE: <relative-path-from-project-root>
            <file content, no wrapping backticks or quotes>

            Example:
            FILE: pom.xml
            <xml content>

            FILE: src/main/java/com/example/demo/DemoApplication.java
            <code>

            Do not include explanations or commentary outside the files.

            Required files:
            - pom.xml
            - src/main/java/com/example/%s/%sApplication.java
            - src/main/java/com/example/%s/controller/%sController.java
            - src/main/java/com/example/%s/entity/%sRecord.java
            - src/main/java/com/example/%s/repository/%sRepository.java
            - src/main/java/com/example/%s/service/%sService.java
            - src/main/resources/application.properties
            - Dockerfile
            """.formatted(
                module.toLowerCase(), capitalize(module),
                module.toLowerCase(), capitalize(module),
                module.toLowerCase(), capitalize(module),
                module.toLowerCase(), capitalize(module),
                module.toLowerCase(), capitalize(module)
            ));

        return prompt.toString();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}


