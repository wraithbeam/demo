package com.example.demo.controllers;

import com.example.demo.models.FileFX;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private File archive;
    /**
     * Массив байтов архива
     */
    private byte[] allBytes;
    /**
     * bool-параметр: есть ли в архиве хотя бы 2 файла
     */
    private boolean isInFileMoreThanTwoFiles = false;
    /**
     * Ключ для поиска скрытого файла
     */
    private final byte[] keyStartFile = {78, 70, 1, 2};
    /**
     * Ключ zip local file header
     */
    private final byte[] bytesH = {80, 75, 1 ,2};
    /**
     * Ключ zip central directory
     */
    private final byte[] bytesCD = {80, 75, 5 ,6};
    private final Charset charset = StandardCharsets.ISO_8859_1;


    /**
     * Запускает форму выбора файла, а затем отображает ее в таблице, инициализируя archive и allBytes
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
        filesFX.addAll(getHiddenFilesFromArchive(false));

        //Заполнение таблицы + активация кнопок управления
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

    /**
     * @param needReturnContent - bool-параметр, отражающий, нужно ли обработать содержимое файла.
     *                          Требуется для извлечения, но не требуется для отображения в таблице.
     * @return ArrayList<FileFX> - коллекция всех скрытых файлов
     */
    private ArrayList<FileFX> getHiddenFilesFromArchive(boolean needReturnContent){
        ArrayList<FileFX> filesFX = new ArrayList<>();

        for (int i = 0; i < allBytes.length - 4; i++) {
            //Если найден файл по ключу
            if ((allBytes[i] == keyStartFile[0]) & (allBytes[i + 1] == keyStartFile[1]) & (allBytes[i + 2] == keyStartFile[2]) & (allBytes[i + 3] == keyStartFile[3])) {

                FileFX fileFX = new FileFX();

                i+=4;   //Сдвигаем ключ
                int length = allBytes[i++]; //Получаем размер названия
                byte[] nameFile = new byte[length];
                for (int j = 0; j < length; j++, i++){
                    nameFile[j] = allBytes[i]; //Записываем название
                }

                length = allBytes[i++]; //Получаем размер записи о последнем изменении
                byte[] lastUpdate = new byte[length];
                for (int j = 0; j < length; j++, i++){
                    lastUpdate[j] = allBytes[i]; //Записываем
                }

                length = allBytes[i++]; //Получаем размер записи о весе файла
                byte[] size = new byte[length];
                for (int j = 0; j < length; j++, i++){
                    size[j] = allBytes[i];  //Записываем
                }
                if (needReturnContent) {    //Если требуется записать содержимое
                    //Создаем буфер, высчитывающий размер содержимого из 4 байт
                    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                    for(int j = 0; j < Integer.BYTES; j++, i++){
                        buffer.put(allBytes[i]);
                    }
                    length = buffer.flip().getInt();
                    System.out.print(length);

                    //Записываем содержимое
                    byte[] content = new byte[length];
                    for (int j = 0; j < length; j++, i++){
                        content[j] = allBytes[i];
                    }
                    //Записываем содержимое в класс
                    fileFX.setContent(content);
                }
                //Записываем в класс
                fileFX.setName(new String(nameFile, charset));
                fileFX.setScope("hidden");
                fileFX.setLastUpdate(new String(lastUpdate, charset));
                fileFX.setSize(Double.parseDouble(new String(size, charset)));
                filesFX.add(fileFX);
                //Цикл на последнем шаге делает лишнюю итерацию, уберем ее
                //Т.к. ее сделает и основной цикл, и перескочит байт
                i--;
            }
        }
        return filesFX;
    }


    /**
     * Добавляет скрытые файлы в архив
     */
    @FXML
    void addFiles(MouseEvent event) {
        try {
            //Выбираем файл для записи
            File file = selectFile(true);

            byte[] byteCodeOfUserSelectedFile = Files.readAllBytes(file.toPath());
            int fileSeparator; //Индекс, по которому в архив будут записаны данные

            //Место, с которого будет производиться запись
            if(isInFileMoreThanTwoFiles){
                //Запишет после первого файла
                fileSeparator = findStartIndexForByteSequence(new byte[]{80, 75, 3, 4}, 1);
            }
            else {
                //Запишет перед central directory, т.е. после единственного файла
                fileSeparator = findStartIndexForByteSequence(new byte[]{80, 75, 1,2});
            }

            byteCodeOfUserSelectedFile = addInfoIntoByteCode(byteCodeOfUserSelectedFile, file); //Дополняет код содержимого информацией
            writeFileIntoArchive(byteCodeOfUserSelectedFile, fileSeparator);    //Записывает код файла в архив
            changeOffsets(byteCodeOfUserSelectedFile.length, 1); //Передвигает offsets архива

            filesFX.add(new FileFX(file, true));

        }
        catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Записывает данные о файлы внутрь его кода.
     * @param bytes - Массив байтов, куда нужно записать данные о файле
     * @param file - Файл, который будет описан в массиве байт.
     * @return массив байт с записанной информацией о файле
     */
    private byte[] addInfoIntoByteCode(byte[] bytes, File file){
        byte[] nameFile = file.getName().getBytes(charset);
        byte[] lastUpdate = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                new Date(file.lastModified())).getBytes(charset);
        byte[] size =  String.valueOf(file.length()).getBytes(charset);
        byte[] contentLength = ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array();

        return  joinByteArray(new byte[][]{keyStartFile,
                new byte[]{(byte) nameFile.length}, nameFile,
                new byte[]{(byte) lastUpdate.length}, lastUpdate,
                new byte[]{(byte) size.length}, size,
                contentLength, bytes
        });
    }

    /**
     * Внедряет в код архива код файла по заданному разделителю (index local header)
     * @param fileCode - байтовый массив кода файла
     * @param indexOfSeparation - индекс, по которому будет вставлен массив
     */
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

    /**
     * Находит индекс байта начала последовательности, по заданному порядковому номеру
     * @param bytes - последовательность, по которой будет найден индекс
     * @param numberOccurrence - порядковый номер
     * @return если такая последовательность не найдена вернет -1
     */
    private int findStartIndexForByteSequence(byte[] bytes, int numberOccurrence){
        int foundedFiles = -1;
        for(int i = 0; i <= allBytes.length - 4; i++){
            //Не ищем среди скрытых файлов, поэтому пропустим байты, отведенные для них
            if ((allBytes[i] == keyStartFile[0]) & (allBytes[i + 1] == keyStartFile[1]) & (allBytes[i + 2] == keyStartFile[2]) & (allBytes[i + 3] == keyStartFile[3])) {
                i = skipHiddenFiles(i);
            }
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

    /**
     * Изменяет offset каждого файла архива, после записанного скрытого файла
     * @param lengthOfFile - вес записываемого файла
     * @param numberOccurrence - номер файла, после которого был записан файл
     */
    private void changeOffsets(int lengthOfFile, int numberOccurrence){
        int numberEntry = 0;
        for(int i = 0; i < allBytes.length - 4; i++) {
            //Не изменяем данные в файле, поэтому пропустим байты, отведенные под него
            if ((allBytes[i] == keyStartFile[0]) & (allBytes[i + 1] == keyStartFile[1]) & (allBytes[i + 2] == keyStartFile[2]) & (allBytes[i + 3] == keyStartFile[3])) {
                i += skipHiddenFiles(i);
            }

            if ((allBytes[i] == bytesH[0]) & (allBytes[i + 1] == bytesH[1]) & (allBytes[i + 2] == bytesH[2]) & (allBytes[i + 3] == bytesH[3])) {
                if (numberEntry < numberOccurrence) {
                    numberEntry++;
                } else {
                    i += 42; //Перейдем к offset
                    int hex = lengthOfFile; //Изначальный отступ прибавляется к весу файла, поэтому инициализируем начало, как вес файла
                    for (int j = 0; j < 4; j++) {
                        hex += allBytes[i++]; //Посчитаем его значение
                    }

                    //Запишем новое значение
                    i -= 4;
                    byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(hex).array();
                    for (int j = 3; j >= 0; j--, i++) {
                        allBytes[i] = bytes[j];
                    }
                }
            }

            if ((allBytes[i] == bytesCD[0]) & (allBytes[i + 1] == bytesCD[1]) & (allBytes[i + 2] == bytesCD[2]) & (allBytes[i + 3] == bytesCD[3])) {

                i += 16;//Перейдем к offset
                int hex = lengthOfFile;//Изначальный отступ прибавляется к весу файла, поэтому инициализируем начало, как вес файла
                for (int j = 0; j < 4; j++) {
                    hex += allBytes[i++];//Посчитаем его значение
                }

                //Запишем новое значение
                i -= 4;
                byte[] bytes = ByteBuffer.allocate(4).putInt(hex).array();
                for (int j = 3; j >= 0; j--, i++) {
                    allBytes[i] = bytes[j];
                }
                //Дальше считать не имеет смысла
                break;
            }
        }
        save();
    }

    /**
     * Пропускает скрытые файлы, возвращая индекс их конца
     * @return индекс конца файлов
     */
    private int skipHiddenFiles(int i){
        i += 4;
        for (int j = 0; j < 3; j++){
            int length = allBytes[i++];
            i += length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        for (int j = 0; j < Integer.BYTES; j++, i++) {
            buffer.put(allBytes[i]);
        }
        i += buffer.flip().getInt();
        return i;
    }




    /**
     * Извлекает файлы в выбранную директорию
     */
    @FXML
    void extractFiles(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");
        File directory = directoryChooser.showDialog(new Stage());

        ArrayList<FileFX> filesFX = getHiddenFilesFromArchive(true);
        for (FileFX fileFX : filesFX){
            File file = new File(directory.getAbsolutePath() + "\\" + fileFX.getName());
            try {
                Files.write(file.toPath(), fileFX.getContent());
            }catch (IOException e){
                throw new RuntimeException(e.getMessage());
            }
        }

        Alert a = new Alert(Alert.AlertType.NONE);

        // set alert type
        a.setAlertType(Alert.AlertType.INFORMATION);
        a.setTitle("Extract completed!");
        a.setHeaderText("Extract completed!");

        // show the dialog
        a.show();

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
        fileChooser.setTitle("Select File");

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
     * @return byte[] - соединенный массив
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
