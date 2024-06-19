package com.shrinkr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public class FileDto {
    private String fileName;
    private String fileUrl;

    public FileDto(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
