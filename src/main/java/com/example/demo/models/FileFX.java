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
        size = file.

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
