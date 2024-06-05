package com.example.wordtogpt;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class HelloController {

    @FXML
    private TextField fontText;
    @FXML
    private TextField fontSizeText;
    @FXML
    private Label leftStatus;
    @FXML
    private Text finaloutText;
    @FXML
    private Text outText;
    @FXML
    private ProgressIndicator progressBar;
    @FXML
    private TextArea promtTextArea;
    @FXML
    private Text rightStatus;
    @FXML
    private Button runButton;
    @FXML
    private Button selectFileButton;
    @FXML
    private Text selectedFileText;
    @FXML
    private TextField wordsText;

    @FXML
    protected void selectFile(ActionEvent event) {
        Stage stage = (Stage) selectFileButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл .docx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Документы Word (*.docx)", "*.docx"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedFileText.setText(selectedFile.getAbsolutePath());
        } else {
            selectedFileText.setText("Файл не выбран.");
        }
    }

    @FXML
    protected void runProgram(ActionEvent event) {
        Path targetPath = null;
        try {
            Path sourcePath = new File(selectedFileText.getText()).toPath();
            String copyFileName = "копия_" + sourcePath.getFileName().toString();
            targetPath = sourcePath.resolveSibling(copyFileName);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Копия файла создана: " + targetPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        progressBar.setVisible(true);
        ThreadMain myThread = new ThreadMain(outText, finaloutText, promtTextArea.getText(), targetPath.toString(),
                Integer.parseInt(wordsText.getText()), fontText.getText(), Float.parseFloat(fontSizeText.getText()),
                progressBar, rightStatus);
        myThread.start();
        rightStatus.setText("Выполнение");
        progressBar.setVisible(false);
    }


    public static boolean isFileEmpty(String filePath) {
        File file = new File(filePath);
        return file.length() == 0;
    }
}
