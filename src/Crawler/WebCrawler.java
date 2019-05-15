package Crawler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class WebCrawler extends JFrame {
    private JTextField urlTextField, depthTextField, workersTextField;
    private JToggleButton runButton;
    private JCheckBox depthCheckBox, timeLimitCheckBox;
    private JLabel parsedPagesLabel, elapsedTime;
    private JTextField exportUrlTextField, timeLimitTextField;
    private DefaultTableModel tableModel;
    private int parsedPages = 0;
    private boolean noNewTasks = false;
    private boolean stop = false;

    private LocalTime time = LocalTime.MIN;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("m:ss");
    private int seconds = 0;


    WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 290);
        setLayout(null);
        setTitle("Web Crawler");

        JLabel nameLabelUrl = new JLabel("Start URL: ");
        nameLabelUrl.setBounds(10, 10, 110, 25);
        nameLabelUrl.setVisible(true);
        add(nameLabelUrl);

        urlTextField = new JTextField();
        urlTextField.setName("urlTextField");
        urlTextField.setBounds(135, 10, 470, 25);
        urlTextField.setVisible(true);
        add(urlTextField);


        JLabel nameLabelWorkers = new JLabel("Workers:");
        nameLabelWorkers.setBounds(10, 45, 110, 25);
        nameLabelWorkers.setVisible(true);
        add(nameLabelWorkers);

        workersTextField = new JTextField();
        workersTextField.setBounds(135, 45, 605, 25);
        workersTextField.setVisible(true);
        add(workersTextField);

        JLabel nameMaxDepth = new JLabel("Maximum depth:");
        nameMaxDepth.setBounds(10, 80, 130, 25);
        nameMaxDepth.setVisible(true);
        add(nameMaxDepth);

        depthTextField = new JTextField();
        depthTextField.setName("depthTextField");
        depthTextField.setBounds(135, 80, 470, 25);
        depthTextField.setVisible(true);
        add(depthTextField);

        depthCheckBox = new JCheckBox("Enabled");
        depthCheckBox.setName("DepthCheckBox");
        depthCheckBox.setBounds(620, 75, 120, 35);
        depthCheckBox.setVisible(true);
        add(depthCheckBox);

        JLabel nameTimeLimit = new JLabel("Time limit:");
        nameTimeLimit.setBounds(10, 115, 110, 25);
        nameTimeLimit.setVisible(true);
        add(nameTimeLimit);

        timeLimitTextField = new JTextField();
        timeLimitTextField.setBounds(135, 115, 400, 25);
        timeLimitTextField.setVisible(true);
        add(timeLimitTextField);

        JLabel nameTimeUnit = new JLabel("seconds");
        nameTimeUnit.setBounds(545, 115, 80, 25);
        nameTimeUnit.setVisible(true);
        add(nameTimeUnit);

        timeLimitCheckBox = new JCheckBox("Enabled");
        timeLimitCheckBox.setBounds(620, 115, 120, 25);
        timeLimitCheckBox.setVisible(true);
        add(timeLimitCheckBox);

        JLabel nameElapsedTime = new JLabel("Elapsed time:");
        nameElapsedTime.setBounds(10, 150, 110, 25);
        nameElapsedTime.setVisible(true);
        add(nameElapsedTime);

        elapsedTime = new JLabel();
        elapsedTime.setBounds(135, 150, 140, 25);
        elapsedTime.setText(time.format(formatter));
        elapsedTime.setVisible(true);
        add(elapsedTime);

        JLabel nameParsedPages = new JLabel("Parsed pages:");
        nameParsedPages.setBounds(10, 185, 110, 25);
        nameParsedPages.setVisible(true);
        add(nameParsedPages);

        parsedPagesLabel = new JLabel();
        parsedPagesLabel.setName("parsedPagesLabel");
        parsedPagesLabel.setBounds(135, 185, 505, 25);
        parsedPagesLabel.setText(String.valueOf(parsedPages));
        add(parsedPagesLabel);

        tableModel = new DefaultTableModel();
        String[] tableHeader = {"URL", "Title"};
        tableModel.setColumnIdentifiers(tableHeader);
        JTable titlesTable = new JTable(tableModel);
        titlesTable.setName("TitlesTable");
        titlesTable.setEnabled(false);

        runButton = new JToggleButton("Run");
        runButton.setName("runButton");
        runButton.setBounds(620, 5, 120, 30);
        runButton.setVisible(true);
        runButton.addActionListener(new RunButtonAction());
        add(runButton);

        exportUrlTextField = new JTextField();
        exportUrlTextField.setName("exportUrlTextField");
        exportUrlTextField.setBounds(135, 220, 475, 25);
        exportUrlTextField.setVisible(true);
        getContentPane().add(exportUrlTextField);

        JLabel nameLabelExport = new JLabel("Export:");
        nameLabelExport.setBounds(10, 220, 110, 25);
        nameLabelExport.setVisible(true);
        getContentPane().add(nameLabelExport);

        JButton exportButton = new JButton("Save");
        exportButton.setName("ExportButton");
        exportButton.setBounds(620, 215, 120, 35);
        exportButton.setVisible(true);
        exportButton.addActionListener(actionEvent -> {
            final String path = exportUrlTextField.getText();

            File file = new File(path);

            try (PrintWriter printWriter = new PrintWriter(file)) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    printWriter.println(tableModel.getValueAt(i, 0));
                    printWriter.println(tableModel.getValueAt(i, 1));
                }

            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                exportUrlTextField.setText("File not found! Try again");
            }
        });
        getContentPane().add(exportButton);

        setVisible(true);
    }

    private String getSiteText(String url) {

        String LINE_SEPARATOR = System.getProperty("line.separator");
        String siteText = "URL unavailable";

        try {

            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
            if (urlConnection.getContentLength() == -1) {
                return "URL unavailable";
            }
            if (urlConnection.getContentType().equals("text/html")) {
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();


                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                    stringBuilder.append(LINE_SEPARATOR);
                }

                siteText = stringBuilder.toString();


                return siteText;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return siteText;
        }
        return siteText;
    }

    private String findTitle(String siteText) {

        String regex = ".*(<title.*>|title=\")([a-zA-Z() 1-9-_]*)(</title>|\").*";
        Matcher titleMatcher = Pattern.compile(regex).matcher(siteText);
        String result = "";

        if (titleMatcher.find()) {
            result = result.concat(titleMatcher.group(2));
        }


        return result;
    }

    private List<String> findLinks(String siteUrl, String siteText) {
        List<String> result = Collections.synchronizedList(new ArrayList<>());

        String regex = "<a (target=[\"']_blank[\"'] +|.* )?href=[\"'](https?://(www.|localhost:[0-9]+/)?|www.|//)?([a-zA-Z./_0-9:\\-]+(/[a-zA-Z./_0-9:\\-]+.html)?)[\"']>";

        Matcher matcher = Pattern.compile(regex).matcher(siteText);

        String regexForSite = "https?://(localhost:[0-9]+/)?";
        Matcher matcherForSite = Pattern.compile(regexForSite).matcher(siteUrl);
        matcherForSite.find();

        while (matcher.find()) {

            try {
                if (matcher.group(3).matches("(www.)?")) {
                    result.add(matcherForSite.group(0) + matcher.group(4));
                } else if (matcher.group(3).matches("//")) {
                    result.add(matcherForSite.group(0) + matcher.group(4));
                } else if (matcher.group(3).matches("https?://[a-zA-Z0-9.:/_]*")) {
                    result.add(matcher.group(3) + matcher.group(4));
                } else {
                    result.add(matcher.group(3) + matcher.group(4));
                }
            } catch (NullPointerException npe) {
                if (matcher.group(4).matches("\\w+\\.html")) {
                    result.add(siteUrl.substring(0, siteUrl.lastIndexOf("/") + 1) + matcher.group(4));
                } else {
                    result.add(matcherForSite.group(0) + matcher.group(4));
                }
            }

        }


        return result;
    }

    class Task implements Callable<PairOfLinksAndDepth> {
        String url;
        int depth;

        Task(String url, int depth) {

            this.url = url;
            this.depth = depth;
        }

        @Override
        public PairOfLinksAndDepth call() {

            List<String> siteLinks = Collections.synchronizedList(findLinks(url, getSiteText(url)));

            return new PairOfLinksAndDepth(siteLinks, depth);
        }
    }

    private Timer clockTimer = new Timer(1000, et -> {
        seconds++;
        elapsedTime.setText(time.plusSeconds(seconds).format(formatter));
        parsedPagesLabel.setText(String.valueOf(parsedPages));
        if (timeLimitCheckBox.isSelected()) {
            int endTime = Integer.parseInt(timeLimitTextField.getText());
            if (seconds >= endTime) {
                noNewTasks = true;
            }
        }

    });

    class RunButtonAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("Run")) {
                clockTimer.restart();
                runButton.setText("Stop");
                runButton.setSelected(false);

                tableModel.getDataVector().removeAllElements();
                tableModel.fireTableDataChanged();


                Thread crawler = new Thread(new StartCrawling());
                crawler.start();


            }

            if (e.getActionCommand().equals("Stop")) {
                stop = true;

                clockTimer.stop();
                runButton.setText("Finished");
                runButton.setSelected(true);
                parsedPagesLabel.setText(String.valueOf(parsedPages));

            }

            if (e.getActionCommand().equals("Finished")) {
                runButton.setText("Run");
                runButton.setSelected(false);

                seconds = 0;
                parsedPages = 0;

                elapsedTime.setText(time.plusSeconds(seconds).format(formatter));
                parsedPagesLabel.setText(String.valueOf(parsedPages));

                noNewTasks = false;
                stop = false;
            }

        }

    }

    class StartCrawling implements Runnable {

        public void run() {

            String startUrl = urlTextField.getText();
            int numOfWorkers;
            if (workersTextField.getText().matches("\\d+")) {
                numOfWorkers = Integer.parseInt(workersTextField.getText());
            } else {
                numOfWorkers = 5;
                workersTextField.setText("5");
            }

            int depth = 0;
            int remainingFutures = 0;
            Future<PairOfLinksAndDepth> completedFuture;
            List<String> newUrls;

            ExecutorService executor = Executors.newFixedThreadPool(numOfWorkers);
            CompletionService<PairOfLinksAndDepth> workers = new ExecutorCompletionService<>(executor);
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

            if (!noNewTasks) {
                workers.submit(new Task(startUrl, depth));
                remainingFutures++;
            }

            while (remainingFutures > 0) {
                if (stop) {
                    threadPoolExecutor.getQueue().clear();
                }
                try {
                    completedFuture = workers.take();
                    remainingFutures--;
                    //System.out.println("while: " + threadPoolExecutor.toString() + " :" + completedFuture.get().depth);
                    depth = completedFuture.get().depth;

                    if (depthCheckBox.isSelected()) {
                        int endDepth = Integer.parseInt(depthTextField.getText());
                        if (depth == endDepth) {
                            noNewTasks = true;
                        }
                    }


                    if (!noNewTasks) {
                        newUrls = completedFuture.get().urls;
                        if (newUrls.isEmpty()) {
                            continue;
                        }
                        depth++;
                        boolean twinFound;

                        for (String url : newUrls) {
                            //System.out.println("url: " + url);
                            twinFound = false;
                            if (noNewTasks) {
                                break;
                            }
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                if (tableModel.getValueAt(i, 0).equals(url)) {
                                    twinFound = true;
                                }
                            }

                            if (!twinFound) {
                                //System.out.println("add: " + threadPoolExecutor.toString() + " : " + url);
                                tableModel.addRow(new String[]{url, findTitle(getSiteText(url))});
                                tableModel.fireTableDataChanged();
                                parsedPages++;
                                workers.submit(new Task(url, depth));
                                remainingFutures++;
                            }
                        }
                    }

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            clockTimer.stop();
            runButton.setText("Finished");
            runButton.setSelected(true);
            parsedPagesLabel.setText(String.valueOf(parsedPages));


        }
    }


    class PairOfLinksAndDepth {
        List<String> urls;
        int depth;

        PairOfLinksAndDepth(List<String> list, int depth) {
            this.urls = list;
            this.depth = depth;
        }
    }


}



