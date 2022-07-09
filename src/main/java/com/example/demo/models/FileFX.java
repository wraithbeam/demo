package com.example.demo.models;

import java.io.File;

public class FileFX {
    private File file;
    private String name;
    private String scope;
    private String lastUpdate;
    private String size;

    public FileFX(File file, boolean isHidden) {
        this.file = file;
        name = file.getName();
        scope = isHidden ? "hidden" : "public";
        lastUpdate = String.valueOf(file.lastModified());
        long bytes = file.length();

        if (bytes / (1024*1024*1024) >= 1) {
            size = String.format("%.2f", (bytes / (1024 * 1024 * 1024))) + " Gb";
        }
        else if (bytes / (1024 * 1024) >= 1){
            size = String.format("%.2f", (bytes / (1024 * 1024))) + " Mb";
        }
        else if (bytes / 1024 >= 1){
            size = String.format("%.2f", (bytes / 1024)) + " Kb";
        }
        else {
            size = String.format("%.2f", (bytes)) + " Bytes";
        }

    }

    public String getName() {
        return name;
    }

    public String getScope() {
        return scope;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getSize() {
        return size;
    }
}
