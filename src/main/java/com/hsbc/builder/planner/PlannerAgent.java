package com.hsbc.builder.planner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlannerAgent {

    public List<String> planModules(String userPrompt) {
        List<String> modules = new ArrayList<>();
        if (userPrompt.toLowerCase().contains("audit")) {
            modules.add("audit");
        }
        if (userPrompt.toLowerCase().contains("workflow")) {
            modules.add("workflow");
        }
        if (userPrompt.toLowerCase().contains("reference")) {
            modules.add("reference-data");
        }
        return modules;
    }
}
