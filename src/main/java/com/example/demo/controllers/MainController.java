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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
    private Button buttonRepair;

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

    /**
     * Отображаемый архив
     */
    private File archive;

    /**
     * Запускает форму выбора файла, а затем отображеат ее в таблице
     */
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
        //Запонение таблицы + активация кнопок управления
        tableInfo.setItems(filesFX);
        buttonAdd.setDisable(false);
        buttonExtract.setDisable(false);
        buttonRepair.setDisable(false);
    }

    /**
     * Достает данные из архива, помещая их в ArrayList
     * @return ArrayList<FileFX> - содержит всю информацию для отображения в таблице
     */
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

    /**
     * Достает СКРЫТЫЕ данные из архива, помещая их в ArrayList
     * @return ArrayList<FileFX> - содержит всю информацию для отображения в таблице
     */
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


    /**
     * Отображает форму множественного выбора файлов,
     * затем с помощью массивов byte встраивает информацию в архив
     */
    @FXML
    void addFiles(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        Charset charset = StandardCharsets.ISO_8859_1; //Кодировка для передачи файлов

        if (files != null) {
            byte[] finalByteCode = "Hk7t5nPyL5cNcHi".getBytes(StandardCharsets.ISO_8859_1); //Ключ для поиска данных
            String keySeparator = "zr8ZTm"; //Ключ, разделяющий поля, описывающий файл: название, вес и т.п.
            for (File file : files) {
                try {
                    FileFX fileFX = new FileFX(file, true);


                    //Данные, разграниченные ключом
                    byte[] fileName = (file.getName() + keySeparator).getBytes(charset);
                    byte[] lastUpdate = (file.lastModified() + keySeparator).getBytes(charset);
                    byte[] fileSize = (file.length() + keySeparator).getBytes(charset);
                    byte[] fileContent = Files.readAllBytes(file.toPath());

                    //Объединение данных в один массив для внедрения в архив
                    byte[] fileInfo = new byte[1];
                    fileInfo = joinByteArray(fileName, lastUpdate);
                    fileInfo = joinByteArray(fileInfo, fileSize);
                    fileInfo = joinByteArray(fileInfo, fileContent);

                    //Формирование кода вставки
                    finalByteCode = joinByteArray(finalByteCode, fileInfo);
                    filesFX.add(fileFX); //Добавление файла в таблицу
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                //Внедрение байтового кода в архив
                Files.write(archive.toPath(), finalByteCode, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Соеденяет 2 массива byte в один. В порядке отправления
     * @param byte1
     * @param byte2
     * @return byte[] - соедененный массив
     */
    private byte[] joinByteArray(byte[] byte1, byte[] byte2) {

        return ByteBuffer.allocate(byte1.length + byte2.length)
                .put(byte1)
                .put(byte2)
                .array();
    }


    /**
     * Извлекает файлы в выбранную директорию
     */
    @FXML
    void extractFiles(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File directory = directoryChooser.showDialog(new Stage());

        String[] filesInfo = getInformationAboutHiddenFiles();
        String keySeparator = "zr8ZTm";

        //Разделяет информацию о файле по ключу. [[имя,дата обновления,размер],...]
        for (String file : filesInfo){
            String[] params = file.split(keySeparator);
            File extractableFile = new File(directory.getAbsolutePath()+ "\\" + params[0]);
            try {
                Files.writeString((extractableFile.toPath()), params[3], StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * @return String[] - информация о файле [файл1, файл2, ...]
     */
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

    /**
     * Сохраняет открытый массив в новый, либо перезаписывает выбранный
     */
    @FXML
    void saveAs(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to save");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip")
        );
        File file = fileChooser.showOpenDialog(new Stage());

        try {
            Files.write(file.toPath(), Files.readAllBytes(archive.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Отчищает архив от скрытых файлов и восстанавливает его работу
     */
    @FXML
    void repairArchive(ActionEvent event) {
        try {
            String content = new String(Files.readAllBytes(archive.toPath()), StandardCharsets.ISO_8859_1);
            String[] filesInfo = content.split("Hk7t5nPyL5cNcHi");
            filesInfo = ArrayUtils.remove(filesInfo, 1);
            Files.write(archive.toPath(), filesInfo[0].getBytes(StandardCharsets.ISO_8859_1));
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @FXML
    public void initialize() {
        tableColumnScope.setCellValueFactory(new PropertyValueFactory<FileFX, String>("scope"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<FileFX, String>("name"));
        tableColumnSize.setCellValueFactory(new PropertyValueFactory<FileFX, String>("size"));
        tableColumnLastUpdate.setCellValueFactory(new PropertyValueFactory<FileFX, String>("lastUpdate"));

        menuBtnOpenArchive.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        menuBtnSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }

}
