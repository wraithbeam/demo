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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    /**
     * Отображаемый архив
     */
    private File archive = new File("C:\\Users\\vlad\\Desktop\\p.zip");
    private byte[] allBytes;
    private boolean isInFileMoreThanTwoFiles = false;
    private final byte[] keyStartFile = {78, 70, 1, 2};
    private final byte[] keyVariableSeparator = {78, 70, 3, 4};
    private final byte[] bytesH = {80, 75, 1 ,2};
    private final byte[] bytesCD = {80, 75, 5 ,6};
    private final Charset charset = StandardCharsets.ISO_8859_1;


    /**
     * Запускает форму выбора файла, а затем отображеат ее в таблице
     */
    @FXML
    void openArchive(ActionEvent event) {
        try {
            archive = selectFile(false);
            allBytes = Files.readAllBytes(archive.toPath());
            tableShowArchive();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void tableShowArchive(){

        tableInfo.getItems().clear();

        filesFX.addAll(getFilesFromArchive());
        filesFX.addAll(getHiddenFilesFromArchive());

        //Запонение таблицы + активация кнопок управления
        tableInfo.setItems(filesFX);
        buttonAdd.setDisable(false);
        buttonExtract.setDisable(false);
    }

    /**
     * Достает данные из архива, помещая их в ArrayList
     * @return ArrayList<FileFX> - содержит всю информацию для отображения в таблице
     */
    private ArrayList<FileFX> getFilesFromArchive(){
        int numberOfFiles = 0;
        try(ZipInputStream zin = new ZipInputStream(new FileInputStream(archive)))
        {
            ArrayList<FileFX> filesFX = new ArrayList<>();
            ZipEntry entry;
            while((entry=zin.getNextEntry())!=null){
                filesFX.add(new FileFX(entry, false));
                numberOfFiles += 1;
            }
            if(numberOfFiles >= 2){
                isInFileMoreThanTwoFiles = true;
            }
            return filesFX;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private ArrayList<FileFX> getHiddenFilesFromArchive(){
        ArrayList<FileFX> filesFX = new ArrayList<>();

        for (int i = 0; i < allBytes.length - 4; i++) {
            if ((allBytes[i] == keyStartFile[0]) & (allBytes[i + 1] == keyStartFile[1]) & (allBytes[i + 2] == keyStartFile[2]) & (allBytes[i + 3] == keyStartFile[3])) {

                FileFX fileFX = new FileFX();

                i+=4;
                int length = allBytes[i++];
                byte[] nameFile = new byte[length];
                for (int j = 0; j < length; j++, i++){
                    nameFile[j] = allBytes[i];
                }
                System.out.print(new String(nameFile, charset));

                i++;
                length = allBytes[i++];
                byte[] lastUpdate = new byte[length];
                for (int j = 0; j < length; j++, i++){
                    lastUpdate[j] = allBytes[i];
                }
                System.out.print(new String(lastUpdate, charset));

                i++;
                length = allBytes[i++];
                byte[] size = new byte[length];
                for (int j = 0; j < length; j++, i++){
                    lastUpdate[j] = allBytes[i];
                }
                i++;

                fileFX.setName(new String(nameFile, charset));
                fileFX.setScope("hidden");
                fileFX.setLastUpdate(new String(lastUpdate, charset));
                fileFX.setSize(ByteBuffer.wrap(size).getDouble());
                filesFX.add(fileFX);
            }
        }
        return filesFX;
    }
    private boolean notSeparator(int i) {
        return !((allBytes[i] == keyVariableSeparator[0]) & (allBytes[i + 1] == keyVariableSeparator[1])
                & (allBytes[i + 2] == keyVariableSeparator[2]) & (allBytes[i + 3] == keyVariableSeparator[3]));
    }


    @FXML
    void addFiles(MouseEvent event) {
        try {
            //Запись файла в архив
            File file = selectFile(true);
            filesFX.add(new FileFX(file, true));
            byte[] byteCodeOfUserSelectedFile = Files.readAllBytes(file.toPath());
            int fileSeparator; //Индекс, по которому в архив будут записаны данные

            if(isInFileMoreThanTwoFiles){
                fileSeparator = findStartIndexForByteSequence(new byte[]{80, 75, 3, 4}, 1);
            }
            else {
                fileSeparator = findStartIndexForByteSequence(new byte[]{80, 75, 1,2});
            }
            byteCodeOfUserSelectedFile = addInfoIntoByteCode(byteCodeOfUserSelectedFile, file);
            writeFileIntoArchive(byteCodeOfUserSelectedFile, fileSeparator);
            changeOffsets(byteCodeOfUserSelectedFile.length, 1);
        }
        catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private byte[] addInfoIntoByteCode(byte[] bytes, File file){
        byte[] nameFile = file.getName().getBytes(charset);
        byte[] lastUpdate = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                new Date(file.lastModified())).getBytes(charset);
        byte[] size = String.valueOf(file.length()).getBytes(charset);

        return joinByteArray(new byte[][]{keyStartFile,
                new byte[]{(byte) nameFile.length}, nameFile,
                new byte[]{(byte) lastUpdate.length}, lastUpdate,
                new byte[]{(byte) size.length}, size,
                new byte[]{(byte) bytes.length}, bytes
        });
    }

    private void writeFileIntoArchive(byte[] fileCode, int indexOfSeparation){

        byte[] finalArchiveCode = new byte[ fileCode.length + allBytes.length ];
        for (int i = 0; i < indexOfSeparation; i++) {
            finalArchiveCode[i] = allBytes[i];
        }

        int j = 0;
        for (int i = indexOfSeparation; i < fileCode.length + indexOfSeparation; i++){
            finalArchiveCode[i] = fileCode[j++];
        }

        j = indexOfSeparation;
        for(int i = indexOfSeparation + fileCode.length; i < finalArchiveCode.length; i++){
            finalArchiveCode[i] = allBytes[j++];
        }
        allBytes = finalArchiveCode;
        save();
    }
    private int findStartIndexForByteSequence(byte[] bytes, int numberOccurrence){
        int foundedFiles = -1;
        for(int i = 0; i <= allBytes.length - 4; i++){
            if ((allBytes[i] == bytes[0]) & (allBytes[i+1] == bytes[1]) &(allBytes[i+2] == bytes[2] ) & (allBytes[i+3] == bytes[3])) {
                foundedFiles += 1;
                if(foundedFiles == numberOccurrence)
                    return i;
            }
        }
        return -1;
    }
    private int findStartIndexForByteSequence(byte[] bytes){
        return findStartIndexForByteSequence(bytes, 0);
    }

    private void changeOffsets(int lengthOfFile, int numberOccurrence){
        int numberEntry = 0;
        for(int i = 0; i < allBytes.length - 4; i++) {
            if ((allBytes[i] == bytesH[0]) & (allBytes[i + 1] == bytesH[1]) & (allBytes[i + 2] == bytesH[2]) & (allBytes[i + 3] == bytesH[3])) {
                if (numberEntry < numberOccurrence) {
                    numberEntry++;
                } else {
                    i += 42;
                    int hex = lengthOfFile;
                    for (int j = 0; j < 4; j++) {
                        hex += allBytes[i++];
                    }
                    i -= 4;
                    byte[] bytes = ByteBuffer.allocate(4).putInt(hex).array();
                    for (int j = 3; j >= 0; j--, i++) {
                        allBytes[i] = bytes[j];
                    }
                }
            }

            if ((allBytes[i] == bytesCD[0]) & (allBytes[i + 1] == bytesCD[1]) & (allBytes[i + 2] == bytesCD[2]) & (allBytes[i + 3] == bytesCD[3])) {
                i += 16;
                int hex = lengthOfFile;
                for (int j = 0; j < 4; j++) {
                    hex += allBytes[i++];
                }
                i -= 4;
                byte[] bytes = ByteBuffer.allocate(4).putInt(hex).array();
                for (int j = 3; j >= 0; j--, i++) {
                    allBytes[i] = bytes[j];
                }
                break;
            }
        }
        save();
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

    @FXML
    public void initialize() throws IOException {
        tableColumnScope.setCellValueFactory(new PropertyValueFactory<>("scope"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        tableColumnLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));

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

    private void save(){
        try {
            Files.write(archive.toPath(), allBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Соединяет все массивы в один
     * @param bytes - двумерный массив byte[][]
     * @return byte[] - соедененный массив
     */
    private byte[] joinByteArray(byte[][] bytes) {
        int length = 0;
        for(byte[] b : bytes){
            length += b.length;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        for(byte[] b : bytes){
            byteBuffer.put(b);
        }

        return byteBuffer.array();

    }

}
