package com.hsbc.builder.config;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;


public class ClassLevelSplitter implements DocumentSplitter{

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();
        // Regex to split at the start of each class definition
        String[] classSections = text.split("(?=// ===|(?=^\\s*(public\\s+)?(class|interface|@Entity)\\b))");
        List<TextSegment> classSegments = new ArrayList<>();

        for (String classSection : classSections) {
            if (!classSection.trim().isEmpty()) {
                classSegments.add(new TextSegment(classSection.trim(), document.metadata()));
            }
        }

        return classSegments;
    }

}
