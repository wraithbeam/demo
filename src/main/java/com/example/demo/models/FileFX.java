package com.example.demo.models;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;

public class FileFX {
    private String name;
    private String scope;
    private String lastUpdate;
    private String size;

    public FileFX(File file, boolean isHidden) {
        name = file.getName();
        scope = isHidden ? "hidden" : "public";
        lastUpdate = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                new Date(file.lastModified())
        );
        double bytes = file.length();
        size = bytesToString(bytes);

    }
    public FileFX(ZipEntry file, boolean isHidden) {
        name = file.getName();
        scope = isHidden ? "hidden" : "public";
        lastUpdate = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                new Date(file.getLastModifiedTime().toMillis()));
        double bytes = file.getSize();
        size = bytesToString(bytes);
    }

    public FileFX() {
    }

    private String bytesToString(double value){
        if (value / (1024*1024*1024) >= 1) {
            return String.format("%.2f", (value / (1024 * 1024 * 1024))) + " Gb";
        }
        else if (value / (1024 * 1024) >= 1){
            return String.format("%.2f", (value / (1024 * 1024))) + " Mb";
        }
        else if (value / 1024 >= 1){
            return String.format("%.2f", (value / 1024)) + " Kb";
        }
        else {
            return String.format("%.2f", (value)) + " Bytes";
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

    public void setName(String name) {
        this.name = name;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setSize(double size) {
        this.size = bytesToString(size);
    }
}
