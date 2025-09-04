package com.hsbc.builder.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import com.hsbc.builder.planner.PlannerAgent;
import com.hsbc.builder.retriever.CodeRetriever;
import com.hsbc.builder.generator.CodeGenerator;
import com.hsbc.builder.packager.ProjectPackager;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;


@Slf4j
@RestController
@RequestMapping("/builder")
public class PromptController {

    private final PlannerAgent planner;
    private final CodeRetriever retriever;
    private final CodeGenerator generator;
    private final ProjectPackager packager;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final Map<String, String> taskStatus = new HashMap<>();
    // Constructor for dependency injection

    public PromptController(PlannerAgent planner,
                            CodeRetriever retriever,
                            CodeGenerator generator,
                            ProjectPackager packager,
                            ThreadPoolTaskExecutor taskExecutor) {
        this.planner = planner;
        this.retriever = retriever;
        this.generator = generator;
        this.packager = packager;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/generate")
    public String generateApp(@RequestBody Map<String, String> request) {
        String taskId = "task-" + System.currentTimeMillis();
        taskStatus.put(taskId, "IN_PROGRESS");

        taskExecutor.execute(() -> {
            try {
                String userPrompt = request.get("prompt");
                List<String> modules = planner.planModules(userPrompt);
                StringBuilder prompt = new StringBuilder("Planned modules: " + String.join(", ", modules) + "\n");

                Map<String, String> generatedModules = new HashMap<>();
                for (String module : modules) {
                    List<Content> content = retriever.retrieveRelevantSnippets(module);
                    prompt.append("Retrieved ").append(content.size()).append(" snippets for module: ").append(module).append("\n");
                    String code = generator.generateServiceStub(module, content);
                    generatedModules.put(module, code);
                }
                log.info("Modules generated:\n{}", prompt);   
                Path projectPath = packager.assembleProject(generatedModules);
                taskStatus.put(taskId, "✅ Project generated at: " + projectPath.toAbsolutePath());
            } catch (Exception e) {
                log.error("Error generating project", e);
                taskStatus.put(taskId, "❌ Error: " + e.getMessage());
            }
        });

        return "Task submitted successfully. Task ID: " + taskId;
    }

    @GetMapping("/status/{taskId}")
    public String getTaskStatus(@PathVariable String taskId) {
        return taskStatus.getOrDefault(taskId, "Task ID not found");
    }
}
