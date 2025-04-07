import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TagExtractorGUI extends JFrame {

    private JTextArea tagTextArea;
    private JLabel sourceFileNameLabel;
    private JFileChooser fileChooser;
    private JButton extractTagsButton;
    private JButton saveTagsButton;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openTextFileItem;
    private JMenuItem openStopWordFileItem;
    private JMenuItem exitMenuItem;
    private File textFile;
    private File stopWordFile;
    private Map<String, Integer> tagFrequencies;

    public TagExtractorGUI() {
        setTitle("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tagTextArea = new JTextArea();
        tagTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(tagTextArea);
        sourceFileNameLabel = new JLabel("No file selected");
        fileChooser = new JFileChooser();
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(textFilter);

        extractTagsButton = new JButton("Extract Tags");
        saveTagsButton = new JButton("Save Tags");
        saveTagsButton.setEnabled(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(sourceFileNameLabel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        buttonPanel.add(extractTagsButton);
        buttonPanel.add(saveTagsButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        openTextFileItem = new JMenuItem("Open Text File...");
        openStopWordFileItem = new JMenuItem("Open Stop Word File...");
        exitMenuItem = new JMenuItem("Exit");

        fileMenu.add(openTextFileItem);
        fileMenu.add(openStopWordFileItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        openTextFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTextFile();
            }
        });

        openStopWordFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openStopWordFile();
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        extractTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extractTags();
            }
        });

        saveTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTags();
            }
        });

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void openTextFile() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textFile = fileChooser.getSelectedFile();
            sourceFileNameLabel.setText("Extracting from: " + textFile.getName());
            tagTextArea.setText("");
            saveTagsButton.setEnabled(false);
        }
    }

    private void openStopWordFile() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            stopWordFile = fileChooser.getSelectedFile();
        }
    }

    private void extractTags() {
        if (textFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a text file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (stopWordFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a stop word file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Set<String> stopWords = loadStopWords(stopWordFile);
            tagFrequencies = getTagFrequencies(textFile, stopWords);
            displayTagFrequencies();
            saveTagsButton.setEnabled(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Set<String> loadStopWords(File file) throws IOException {
        Set<String> stopWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
        }
        return stopWords;
    }

    private Map<String, Integer> getTagFrequencies(File file, Set<String> stopWords) throws IOException {
        Map<String, Integer> frequencies = new TreeMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.replaceAll("[^a-zA-Z\\s]", "").toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.isEmpty() || stopWords.contains(word)) {
                        continue;
                    }
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
        }
        return frequencies;
    }

    private void displayTagFrequencies() {
        tagTextArea.setText("");
        for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
            tagTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    private void saveTags() {
        JFileChooser saveChooser = new JFileChooser();
        int returnVal = saveChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileToSave = saveChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Tags saved successfully to " + fileToSave.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tags: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TagExtractorGUI();
            }
        });
    }
}

