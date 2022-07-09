package com.example.demo.controllers;

import com.example.demo.models.FileFX;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
    private TableView<FileFX> tableInfo;

    @FXML
    private TableColumn<FileFX, String> tableColumnLastUpdate;

    @FXML
    private TableColumn<FileFX, String> tableColumnName;

    @FXML
    private TableColumn<FileFX, String> tableColumnScope;

    @FXML
    private TableColumn<FileFX, String> tableColumnSize;

    private ObservableList<FileFX> filesFX = FXCollections.observableArrayList();

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

        tableShowArchive();
    }
    private void tableShowArchive(){

        tableInfo.getItems().clear();

        for (FileFX fileFX : getFilesFromArchive()){
            filesFX.add(fileFX);
        }
        for (FileFX fileFX : getHiddenFilesFromArchive()){
            filesFX.add(fileFX);
        }

        tableInfo.setItems(filesFX);
        buttonAdd.setDisable(false);
        buttonExtract.setDisable(false);
    }

    private void tableAddElement(FileFX fileFX){
        filesFX.add(fileFX);
        tableInfo.setItems(filesFX);
    }


    private ArrayList<FileFX> getFilesFromArchive(){
        try(ZipInputStream zin = new ZipInputStream(new FileInputStream(archive)))
        {
            ArrayList<FileFX> filesFX = new ArrayList<>();
            ZipEntry entry;
            while((entry=zin.getNextEntry())!=null){
                filesFX.add(new FileFX(entry, false));
            }
            return filesFX;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<FileFX> getHiddenFilesFromArchive(){
        ArrayList<FileFX> filesFX = new ArrayList<>();
        for (String info : getInformationAboutHiddenFiles()){
            String[] params = info.split("zr8ZTm");

            FileFX fileFX = new FileFX();
            fileFX.setName(params[0]);
            fileFX.setLastUpdate(new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(Long.parseLong(params[1])));
            fileFX.setSize(Double.parseDouble(params[2]));
            fileFX.setScope("hidden");

            filesFX.add(fileFX);
        }
        return filesFX;
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
                    FileFX fileFX = new FileFX(file, true);
                    filesFX.add(fileFX);

                    byte[] fileName = (file.getName() + "zr8ZTm").getBytes(charset);
                    byte[] lastUpdate = (file.lastModified() + "zr8ZTm").getBytes(charset);
                    byte[] fileSize = (file.length() + "zr8ZTm").getBytes(charset);
                    byte[] fileContent = Files.readAllBytes(file.toPath());

                    byte[] fileInfo = new byte[1];
                    fileInfo = joinByteArray(fileName, lastUpdate);
                    fileInfo = joinByteArray(fileInfo, fileSize);
                    fileInfo = joinByteArray(fileInfo, fileContent);

                    finalByteCode = joinByteArray(finalByteCode, fileInfo);
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

        String[] filesInfo = getInformationAboutHiddenFiles();

        for (String file : filesInfo){
            String[] params = file.split("zr8ZTm");
            File extractableFile = new File(directory.getAbsolutePath()+ "\\" + params[0]);
            try {
                Files.writeString((extractableFile.toPath()), params[3], StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String[] getInformationAboutHiddenFiles(){
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

    @FXML
    public void initialize() {
        tableColumnScope.setCellValueFactory(new PropertyValueFactory<FileFX, String>("scope"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<FileFX, String>("name"));
        tableColumnSize.setCellValueFactory(new PropertyValueFactory<FileFX, String>("size"));
        tableColumnLastUpdate.setCellValueFactory(new PropertyValueFactory<FileFX, String>("lastUpdate"));
    }

}
