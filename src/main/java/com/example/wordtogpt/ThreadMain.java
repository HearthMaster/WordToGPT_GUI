package com.example.wordtogpt;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ThreadMain extends Thread {
    private Text outText;
    private ProgressIndicator progressIndicator;
    private final String fontName;
    private final float fontSize;
    private final String filePath;
    private final int wordLimit;
    private Text finaloutText;
    private String promt;
    private Text rightStatus;

    @Override
    public void run() {
        try {
            String inputFilePath = filePath;
            String outputFilePath = "parsed_text.docx";
            int chunkSize = 300;
            String requiredFont = fontName;
            int requiredFontSize = (int) fontSize;

            List<String> paragraphs = readDocxWithFontCheck(inputFilePath, requiredFont, requiredFontSize);

            // Разделение текста на куски с учетом абзацев и лимита слов
            List<String> chunks = splitTextIntoChunksWithParagraphs(paragraphs, chunkSize, promt);

            // Обработка каждого куска текста
            List<String> processedChunks = new ArrayList<>();
            for (String chunk : chunks) {
                // Запись куска текста в cache.txt
                outText.setText(chunk);
                writeToFile("cache.txt", chunk);
                // Запуск exe-файла для обработки текста
                boolean flag = true;
                while (flag) {
                    if (runNeuralNetworkExe().contains("'charmap' codec can't encode character '\\u8be5'")) {
                        JOptionPane.showMessageDialog(null, "Смените VPN!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    } else {
                        flag = false;
                    }
                }
                // Чтение обработанного текста из response.txt
                String processedChunk = readFromFile("response.txt");
                processedChunks.add(processedChunk + "\n");
                finaloutText.setText(processedChunk  + "\n");
            }
            rightStatus.setText("Готово");
            writeDocx(outputFilePath, processedChunks);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public ThreadMain(Text text, Text finaloutText, String promt, String filePath, int wordCount, String fontName,
        float fontSize, ProgressIndicator progressIndicator, Text rightStatus){
            this.outText = text;
            this.filePath = filePath;
            this.wordLimit = wordCount;
            this.fontSize = fontSize;
            this.fontName = fontName;
            this.finaloutText = finaloutText;
            this.promt = promt;
            this.progressIndicator = progressIndicator;
            this.rightStatus = rightStatus;
        }


    private static List<String> readDocxWithFontCheck(String filePath, String requiredFont, int requiredFontSize) throws IOException {
        XWPFDocument doc = new XWPFDocument(Files.newInputStream(Paths.get(filePath)));
        List<String> paragraphs = new ArrayList<>();
        for (XWPFParagraph p : doc.getParagraphs()) {
            if (paragraphMatchesFont(p, requiredFont, requiredFontSize)) {
                paragraphs.add(p.getText());
            }
        }
        doc.close();
        return paragraphs;
    }

    private static boolean paragraphMatchesFont(XWPFParagraph paragraph, String requiredFont, int requiredFontSize) {
        for (XWPFRun run : paragraph.getRuns()) {
            String fontName = run.getFontFamily();
            int fontSize = run.getFontSize();
            // Учитываем возможные значения по умолчанию
            if (fontSize == -1) fontSize = 12;  // Размер по умолчанию может отличаться в зависимости от документа
            if (!requiredFont.equals(fontName) || fontSize != requiredFontSize) {
                return false;
            }
        }
        return true;
    }

    private static List<String> splitTextIntoChunksWithParagraphs(List<String> paragraphs, int chunkSize, String prompt) {
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder(prompt).append("\n");
        int wordCount = 0;

        for (String paragraph : paragraphs) {
            int paragraphWordCount = paragraph.split("\\s+").length;

            if (wordCount + paragraphWordCount > chunkSize) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder(prompt).append("\n");
                wordCount = 0;
            }

            chunk.append(paragraph).append("\n\n");
            wordCount += paragraphWordCount;
        }

        if (chunk.length() > 0) {
            chunks.add(chunk.toString().trim());
        }

        return chunks;
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
    }

    private static String readFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }

    private static String runNeuralNetworkExe() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("prog.exe");
        builder.redirectErrorStream(true);
        Process process = builder.start();

        // Чтение вывода процесса и вывод в консоль с правильной кодировкой
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                return line;
            }
        }
        process.waitFor();
        return "aboba";
    }

    private static void writeDocx(String filePath, List<String> paragraphs) throws IOException {
        XWPFDocument doc = new XWPFDocument();
        for (String text : paragraphs) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(text);
        }
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            doc.write(out);
        }
        doc.close();
    }
}
