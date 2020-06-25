package editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    private String filePathAndName;
    private String text = "";
    public static final java.util.List<int[]> indexes = new ArrayList<>();
    int searchResultIterator = 0;

    public TextEditor() {
//FRAME CREATING
        super("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setResizable(false);
        //setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        //setLayout(new FlowLayout());

//TEXT AREA CREATING
        JTextArea textArea = new JTextArea(30, 85);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setName("TextArea");

//TEXT FIELD CREATING
        JTextField searchField = new JTextField(35);
        searchField.setName("SearchField");

//SCROLL PANE CREATING
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setName("ScrollPane");

//PANEL CREATING
        JPanel upperPanel = new JPanel();
        add(upperPanel);
        upperPanel.setLayout(new FlowLayout());

//CHECKBOX CREATING
        JCheckBox isRegexp = new JCheckBox("Use regex");
        isRegexp.setName("UseRegExCheckbox");

//FILE_CHOOSER CREATING
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        fileChooser.setVisible(false);
        upperPanel.add(fileChooser);

//BUTTONS CREATING
        JButton save = new JButton("Save");
        save.setName("SaveButton");
        save.addActionListener(e -> {
            fileChooser.setVisible(true);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                filePathAndName = (String.valueOf(fileChooser.getSelectedFile().toPath()));
            }
            fileChooser.setVisible(false);

            File textFile = new File(filePathAndName);
            text = textArea.getText();
            try {
                FileOutputStream fos = new FileOutputStream(textFile);
                fos.write(text.getBytes());
            } catch (IOException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });


        JButton open = new JButton("Open");
        open.setName("OpenButton");
        open.addActionListener(e -> {
            fileChooser.setVisible(true);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                filePathAndName = (String.valueOf(fileChooser.getSelectedFile().toPath()));
            }
            fileChooser.setVisible(false);

            File textFile = new File(filePathAndName);

            try {
                FileInputStream fis = new FileInputStream(textFile);
                textArea.setText(new String(fis.readAllBytes()));
            } catch (IOException fileNotFoundException) {
                textArea.setText("");
                fileNotFoundException.printStackTrace();
            }
        });

        JButton search = new JButton("Search");
        search.setName("StartSearchButton");

        search.addActionListener(e -> {
            String searchText = searchField.getText();

            if (!isRegexp.isSelected()) {
                searchText = searchText.replaceAll("([\\\\()+\\-*.|&$?])", "\\\\$1");
            }

            searchResultIterator = 0;

            indexes.clear();
            Thread thread = new SearchInText(textArea.getText(), searchText);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            //new SearchInText(textArea.getText(), searchText).start();
            //indexes = SearchInText.searcher(textArea.getText(), searchText);
            textArea.select(indexes.get(searchResultIterator)[0], indexes.get(searchResultIterator)[1]);
            textArea.grabFocus();
        });

        JButton next = new JButton("Next");
        next.setName("NextMatchButton");
        next.addActionListener(e -> {

            String searchText = searchField.getText();

            if (!isRegexp.isSelected()) {
                searchText = searchText.replaceAll("([\\\\()+\\-*.|&$?])", "\\\\$1");
            }

            searchResultIterator++;

            indexes.clear();
            Thread thread = new SearchInText(textArea.getText(), searchText);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            if (indexes.size() <= 1 || searchResultIterator >= indexes.size()) {
                searchResultIterator--;
            }
            textArea.select(indexes.get(searchResultIterator)[0], indexes.get(searchResultIterator)[1]);
            textArea.grabFocus();
        });

        JButton previous = new JButton("Previous");
        previous.setName("PreviousMatchButton");
        previous.addActionListener(e -> {

            String searchText = searchField.getText();

            if (!isRegexp.isSelected()) {
                searchText = searchText.replaceAll("([\\\\()+\\-*.|&$?])", "\\\\$1");
            }

            searchResultIterator--;

            indexes.clear();
            Thread thread = new SearchInText(textArea.getText(), searchText);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            if (searchResultIterator < 0) {
                searchResultIterator = indexes.size() - 1;
            }

            if (indexes.size() <= 1) {
                searchResultIterator++;
            }
            textArea.select(indexes.get(searchResultIterator)[0], indexes.get(searchResultIterator)[1]);
            textArea.grabFocus();
        });

//MENU creating & adding
        //JMENUBAR
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        //JMENU
        JMenu fileMenu = new JMenu("File"); //FILE menu
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenu searchMenu = new JMenu("Search"); //SEARCH menu
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(searchMenu);

        //SUBMENU (JMenuItem)
        JMenuItem useRegexpMenuItem = new JMenuItem("Use regular expressions");
        useRegexpMenuItem.setName("MenuUseRegExp");
        useRegexpMenuItem.addActionListener(e -> isRegexp.doClick());

        JMenuItem nextMatchMenuItem = new JMenuItem("Next match");
        nextMatchMenuItem.setName("MenuNextMatch");
        nextMatchMenuItem.addActionListener(e -> next.doClick());

        JMenuItem previousMatchMenuItem = new JMenuItem("Previous search");
        previousMatchMenuItem.setName("MenuPreviousMatch");
        previousMatchMenuItem.addActionListener(e -> previous.doClick());

        JMenuItem startSearchMenuItem = new JMenuItem("Start search");
        startSearchMenuItem.setName("MenuStartSearch");
        startSearchMenuItem.addActionListener(e -> search.doClick());

        JMenuItem openMenuItem = new JMenuItem("Load");
        openMenuItem.setName("MenuOpen");
        openMenuItem.setMnemonic(KeyEvent.VK_L);
        openMenuItem.addActionListener(e -> open.doClick());

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setName("MenuSave");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(e -> save.doClick());

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setName("MenuExit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.addActionListener(e -> System.exit(0));

        //adding in right order
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        searchMenu.add(startSearchMenuItem);
        searchMenu.add(previousMatchMenuItem);
        searchMenu.add(nextMatchMenuItem);
        searchMenu.add(useRegexpMenuItem);

//adding components
        upperPanel.add(save);
        upperPanel.add(open);
        upperPanel.add(searchField);
        upperPanel.add(search);
        upperPanel.add(previous);
        upperPanel.add(next);
        upperPanel.add(isRegexp);
        upperPanel.add(scrollPane);
        upperPanel.add(new JScrollPane(textArea));
        setVisible(true);
    }
}

//this class uses only for searching purposes.
class SearchInText extends Thread {
    final String text;
    final String searchLine;

    public SearchInText(String text, String searchLine) {
        this.text = text;
        this.searchLine = searchLine;
    }

    @Override
    public void run() {
        searcher(this.text, this.searchLine);
    }
//this method simply goes through given text and stack in List of int [] arrays first and last indexes of founded matches
    void searcher(String text, String searchLine) {
        try {
            Pattern pattern = Pattern.compile(searchLine);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find())
                TextEditor.indexes.add(new int[]{matcher.start(), matcher.end()});

        } catch (Exception e) {
            System.out.println("SearchInText.searcher exception");
        }
    }
}