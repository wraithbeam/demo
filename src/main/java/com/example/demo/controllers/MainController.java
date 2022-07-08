package com.example.demo.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
            byte[] finalByteCode = "NmQW!#g*ao4{".getBytes();
            for (File file : files) {
                try {
                    byte[] fileName = ("fn(" + file.getName() + ")").getBytes(); //fn(text.txt)

                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    fileContent = joinByteArray("co(".getBytes(), fileContent);
                    fileContent = joinByteArray(fileContent, ")".getBytes());   //co(text123)

                    //TODO Написать дату изменения

                    finalByteCode = joinByteArray(finalByteCode, fileName);
                    finalByteCode = joinByteArray(finalByteCode, fileContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                finalByteCode = joinByteArray(finalByteCode, "}$$$###***&&&".getBytes());
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
//        DirectoryChooser directoryChooser = new DirectoryChooser();
//        directoryChooser.setTitle("Select Directory");
//        File directory = directoryChooser.showDialog(new Stage());


        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(archive);

            ArrayList<String> fileName = new ArrayList<>();
            ArrayList<String> fileContent = new ArrayList<>();
            ArrayList<Character> fullText = new ArrayList<>();

            char[] key = {'N','m','Q','W','!','#','g','*','a','o','4',};

            int i = 0;
            int j = 0;
            while((i = fileInputStream.read())!= -1){
                char s = (char) i;
                if(j == 10){
                    i = fileInputStream.read();
                    while (true){
                        s = (char) i;
                        System.out.print(s);
                        fullText.add(s);
                        if(s == '}')
                            break;
                        i = fileInputStream.read();
                    }
                    break;
                }
                if(s == key[j])
                    j++;
                else
                    j = 0;

            }

            System.out.println(fullText);

            fileInputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
