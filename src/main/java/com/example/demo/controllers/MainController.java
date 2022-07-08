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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    void addFiles(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
        if (files != null) {
            byte[] finalByteCode = "Hk7t5nPyL5cNcHi".getBytes();
            for (File file : files) {
                try {
                    byte[] fileName = (file.getName() + "zr8ZTm").getBytes(); //fn(text.txt)
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
    void exit(ActionEvent event) {

    }

    @FXML
    void extractFiles(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File directory = directoryChooser.showDialog(new Stage());

        String[] filesInfo = getHiddenFilesInformation();

        for (String file : filesInfo){
            System.out.println(file);
            String[] params = file.split("zr8ZTm");
            File extractableFile = new File(directory.getAbsolutePath()+ "\\" + params[0]);
            try {
                Files.write((extractableFile.toPath()), (params[1]).getBytes() );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String[] getHiddenFilesInformation(){
        try {
            String content = new String(Files.readAllBytes(archive.toPath()));
            String[] filesInfo = content.split("Hk7t5nPyL5cNcHi");
            filesInfo = ArrayUtils.remove(filesInfo, 0);

            return filesInfo;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    void openArchive(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        archive = fileChooser.showOpenDialog(new Stage());
        try {
            System.out.println(new String(Files.readAllBytes(archive.toPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        showArchiveInTable();
    }

    private void showArchiveInTable(){

        try {
            FileInputStream fileInputStream = new FileInputStream(archive);
            int i;
            while((i=fileInputStream.read())!= -1){

                System.out.print((char)i);
            }
            fileInputStream.close();
            System.out.println("");

            System.out.println(archive.getAbsolutePath());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        try {
//            ZipInputStream zis = new ZipInputStream(new FileInputStream(archive.getAbsolutePath()));
//
//            ZipEntry zipEntry;
//            while((zipEntry=zis.getNextEntry())!=null){
//                System.out.printf("File name: %s \t File size: %d \n",zipEntry.getName(), zipEntry.getSize());
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        catch (Exception e){
//            System.out.println(e);
//        }
    }

    @FXML
    void save(ActionEvent event) {

    }

    @FXML
    void saveAs(ActionEvent event) {

    }

}
