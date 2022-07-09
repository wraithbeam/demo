package com.example.demo.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;

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
    private TableView<ZipEntry> table;

    @FXML
    private TableColumn<ZipEntry, String> tableColumnLastUpdate;

    @FXML
    private TableColumn<ZipEntry, String> tableColumnName;

    @FXML
    private TableColumn<ZipEntry, String> tableColumnScope;

    @FXML
    private TableColumn<ZipEntry, String> tableColumnSize;

    //Not FX variables
    private File archive = new File("C:\\Users\\vlad\\Desktop\\zip1.zip");
    @FXML
    void exit(ActionEvent event) {

    }

    @FXML
    void openArchive(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip")
                );
        archive = fileChooser.showOpenDialog(new Stage());

        showArchiveInTable();
    }
    private void showArchiveInTable(){

    }

    @FXML
    void addFiles(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        Charset charset = StandardCharsets.ISO_8859_1;

        if (files != null) {
            byte[] finalByteCode = "Hk7t5nPyL5cNcHi".getBytes(charset);
            for (File file : files) {
                try {
                    byte[] fileName = (file.getName() + "zr8ZTm").getBytes(charset); //fn(text.txt)
                    byte[] fileContent = Files.readAllBytes(file.toPath());

                    fileContent = joinByteArray(fileName, fileContent);   //co(text123)

                    //TODO Написать дату изменения

                    finalByteCode = joinByteArray(finalByteCode, fileContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Files.write(archive.toPath(), finalByteCode, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private byte[] joinByteArray(byte[] byte1, byte[] byte2) {

        return ByteBuffer.allocate(byte1.length + byte2.length)
                .put(byte1)
                .put(byte2)
                .array();
    }


    @FXML
    void extractFiles(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File directory = directoryChooser.showDialog(new Stage());

        String[] filesInfo = getHiddenFilesInformation();

        for (String file : filesInfo){
            String[] params = file.split("zr8ZTm");
            File extractableFile = new File(directory.getAbsolutePath()+ "\\" + params[0]);
            try {
                Files.writeString((extractableFile.toPath()), params[1], StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String[] getHiddenFilesInformation(){
        try {
            String content = new String(Files.readAllBytes(archive.toPath()), StandardCharsets.ISO_8859_1);
            String[] filesInfo = content.split("Hk7t5nPyL5cNcHi");
            filesInfo = ArrayUtils.remove(filesInfo, 0);

            return filesInfo;

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
