package com.aizen.service;

import com.aizen.model.Resume;
import com.aizen.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Handles full resume import/export to standalone JSON files using
 * Jackson, independent of the SQLite persistence layer. Lets a user back
 * up a resume, move it to another machine, or hand it to another tool.
 */
public class JsonService {

    private final ObjectMapper mapper = JsonUtil.getMapper();

    public void exportResume(Resume resume, File destination) throws IOException {
        mapper.writeValue(destination, resume);
    }

    public Resume importResume(File source) throws IOException {
        Resume resume = mapper.readValue(source, Resume.class);
        // Imported resumes are treated as new local copies, not overwrites of an existing DB row.
        resume.setId(0);
        return resume;
    }
}
