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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
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
    private File archive = new File("C:\\Users\\vlad\\Desktop\\p.zip");
    private byte[] allBytes;

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
     * Отображает форму множественного выбора файлов,
     * затем с помощью массивов byte встраивает информацию в архив
     */
    @FXML
    void addFiles(MouseEvent event) {
        try {

            /*TODO
                !!Записать файл в архив
                Сдвинуть оффсет хидера текущего
                Сдвинуть фсе офсеты
                Сдвинуть офссет центральной директории
             */


            //Запись файла в архив
            File file = selectFile(true);
            byte[] byteCodeOfUserSelectedFile = Files.readAllBytes(file.toPath());
            int indexOfSecondEntry = findIndexOfEntryHeader(1);
            writeFileIntoArchive(byteCodeOfUserSelectedFile, indexOfSecondEntry);




            //int indexOfCentralDirectory = findIndexOfByteSequence(new int[]{80, 75, 5, 6}, false);
            //int indexOfOffsetCentralDirectory = indexOfCentralDirectory + 16;
        }
        catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }


    }

    private void writeFileIntoArchive(byte[] fileCode, int indexOfSeparation){

        byte[] finalArchiveCode = new byte[ fileCode.length + allBytes.length ];
        for(int i = 0; i < indexOfSeparation; i++) {
            finalArchiveCode[i] = allBytes[i];
        }
        int j = 0;
        for (int i = indexOfSeparation; i < fileCode.length + indexOfSeparation; i++){
            finalArchiveCode[i] = fileCode[j++];
        }
        j = indexOfSeparation;
        for(int i = indexOfSeparation + fileCode.length + 1; i < finalArchiveCode.length; i++){
            finalArchiveCode[i] = allBytes[j++];
        }
        try {
            Files.write(archive.toPath(), finalArchiveCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int findIndexOfByteSequence(int[] bytes, boolean fromStartToEnd){
        if(fromStartToEnd)
            for(int i = 0; i <= allBytes.length - 4; i++){
                if ((allBytes[i] == bytes[0]) & (allBytes[i+1] == bytes[1]) & (allBytes[i+2] == bytes[2] ) & (allBytes[i+3] == bytes[3])) {
                    return i;
                }
            }
        else
            for(int i = allBytes.length - 4; i >= 0; i--){
                if((allBytes[i] == bytes[3] ) & (allBytes[i-1] == bytes[2]) & (allBytes[i-2] == bytes[1] ) & (allBytes[i-3] == bytes[0])) {
                    return i - 3;
                }
            }
        return -1;
    }

    /**
     * Обходит циклом весь файл, начниная с начала,
     * и находит порядковый номер байта нового Entry.
     * В случае отсутствия искомого Entry вернет -1
     * @param indexOfEntry - индекс файла, отсчет от нуля
     * @return индекс байта начала header`а файла
     */
    private int findIndexOfEntryHeader(int indexOfEntry){
        int[] bytes = {80, 75, 1 ,2};
        int foundedFiles = -1;
        for(int i = 0; i <= allBytes.length - 4; i++){
            if ((allBytes[i] == bytes[0]) & (allBytes[i+1] == bytes[1]) &(allBytes[i+2] == bytes[2] ) & (allBytes[i+3] == bytes[3])) {
                foundedFiles += 1;
                if(foundedFiles == indexOfEntry)
                    return i;
            }
        }
        return -1;
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
     * * Использует FileChooser
     * @return File - выбранный файл
     */
    private File selectFile(boolean showAllFiles){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to save");

        if(showAllFiles)
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
        else
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Zip Files", "*.zip"));

        return fileChooser.showOpenDialog(new Stage());
    }

    /**
     * Использует FileChooser
     * @return List<File> - выбранные файлы
     */
    private List<File> selectFiles(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to save");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip")
        );
        return fileChooser.showOpenMultipleDialog(new Stage());
    }

    @FXML
    public void initialize() throws IOException {
        tableColumnScope.setCellValueFactory(new PropertyValueFactory<FileFX, String>("scope"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<FileFX, String>("name"));
        tableColumnSize.setCellValueFactory(new PropertyValueFactory<FileFX, String>("size"));
        tableColumnLastUpdate.setCellValueFactory(new PropertyValueFactory<FileFX, String>("lastUpdate"));

        //TODO удалить
        buttonAdd.setDisable(false);
        allBytes = Files.readAllBytes(archive.toPath());

        menuBtnOpenArchive.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        menuBtnSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }

}
