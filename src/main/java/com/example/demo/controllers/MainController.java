package com.example.demo.controllers;

import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainController {

    @FXML
    private Button buttonAdd;

    @FXML
    private Button buttonExtract;

    @FXML
    private MenuItem menuBtnExit;

    @FXML
    private MenuItem menuBtnOpenArchive;

    @FXML
    private MenuItem menuBtnSave;

    @FXML
    private MenuItem menuBtnSaveAs;

    @FXML
    private TableView<File> table;

    @FXML
    private TableColumn<File, String> tableColumnLastUpdate;

    @FXML
    private TableColumn<File, String> tableColumnName;

    @FXML
    private TableColumn<File, String> tableColumnScope;

    @FXML
    private TableColumn<File, String> tableColumnSize;

    //Not FX variables
    private File archive;

    @FXML
    void addFiles(MouseEvent event) {

    }

    @FXML
    void exit(ActionEvent event) {

    }

    @FXML
    void extractFiles(MouseEvent event) {

    }

    @FXML
    void openArchive(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        archive = fileChooser.showOpenDialog(new Stage());
        showArchiveInTable();
    }

    private void showArchiveInTable(){
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(archive.getAbsolutePath()));
            ObservableList<ZipEntry> files;

            ZipEntry zipEntry;
            while((zipEntry=zis.getNextEntry())!=null){
                files.add(zipEntry);
                System.out.printf("File name: %s \t File size: %d \n",zipEntry.getName(), zipEntry.getSize());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void save(ActionEvent event) {

    }

    @FXML
    void saveAs(ActionEvent event) {

    }

}
