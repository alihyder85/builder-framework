package com.hsbc.builder.packager;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProjectPackager {

    public Path assembleProject(Map<String, String> generatedModules) throws IOException {
        Path projectDir = Paths.get("generated-projects", "demo-" + System.currentTimeMillis());
        Files.createDirectories(projectDir);

        for (Map.Entry<String, String> entry : generatedModules.entrySet()) {
            Path moduleFile = projectDir.resolve(entry.getKey() + "-Service.txt");
            Files.writeString(moduleFile, entry.getValue());
        }

        // // Add docker-compose.yml
        // String compose = 
        //     "version: '3'\n" +
        //     "services:\n" +
        //     "  app:\n" +
        //     "    build: .\n" +
        //     "    ports:\n" +
        //     "      - \"8080:8080\"\n";
        // Files.writeString(projectDir.resolve("docker-compose.yml"), compose);

        return projectDir;
    }
}


