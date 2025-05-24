import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MainFrame extends JFrame {
    private File selectedFolder;
    private JLabel selectedPathLabel;
    private JButton scanButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton deleteButton;
    public MainFrame() {
        //setting the layout
        setTitle("Duplicate FIle Finder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);                            //we set the default operation onclcicking close
        setSize(500, 500);
        setLayout(new BorderLayout());                                      //borderlayout function (start(NORTH), end(SOUTH), left, right, center)

        //label to show path at the top of the window
        JLabel titleLabel = new JLabel("Select the file");              //creates the label
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);           //aligns the label in the center
        add(titleLabel, BorderLayout.NORTH);                                //we place this label to the top(north) section of the window

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        //shows the selected folder path (initially no path selected)
        selectedPathLabel= new JLabel("No File Selected");
        selectedPathLabel.setHorizontalAlignment((SwingConstants.CENTER));
        //add(selectedPathLabel, BorderLayout.CENTER);                        //we place this label to the top(north) section of the window

        //browsing button adding
        JButton browseButton = new JButton("Browse Files");
        browseButton.addActionListener(this::onBrowseClicked);              //when button is clicked, it will render the onBrowseCLicked() function
        buttonPanel.add(browseButton);

        scanButton = new JButton("Scan for duplicates");
        scanButton.setEnabled(false);                                       //keeps the button disabled until user selects a folder
        scanButton.addActionListener(this::onScanClicked);              //when button is clicked, it will render the onBrowseCLicked() function
        buttonPanel.add(scanButton);
        add(buttonPanel, BorderLayout.SOUTH);

        deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(this::onDeleteClicked);
        buttonPanel.add(deleteButton);
        deleteButton.setEnabled(false);


        //Inititalizing the table to show the duplicate files
        String[] columnNames = {"Delete?", "Group", "File Path"};                      //creating colums for the table, adding delete option
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel){
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;                                     //only lets edit checkbox for column
            }
        };

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(selectedPathLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    //function for when browse button is clicked
    private void onBrowseClicked(ActionEvent e){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            selectedFolder = chooser.getSelectedFile();
            selectedPathLabel.setText("Selected: "+selectedFolder.getAbsolutePath());
            scanButton.setEnabled(true);                                            //enables the scanning button
        }
    }
    private void onScanClicked(ActionEvent e) {
        if (selectedFolder == null) {
            JOptionPane.showMessageDialog(this, "Select a Folder");
            return;
        }

        tableModel.setRowCount(0); // Clear previous results
        scanButton.setEnabled(false); // Prevent multiple scans
        deleteButton.setEnabled(true);

        // Run scan in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Map<String, List<File>> duplicateFiles = findDuplicateFiles(selectedFolder);

                int count = 1;
                for (Map.Entry<String, List<File>> entry : duplicateFiles.entrySet()) {
                    List<File> files = entry.getValue();
                    if (files.size() > 1) {
                        for (File file : files) {
                            tableModel.addRow(new Object[]{false, "Group " + count, file.getAbsolutePath()});
                        }
                        count++;
                    }
                }
                if (count == 1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "No Duplicates Found");
                }
                return null;
            }
            @Override
            protected void done() {
                scanButton.setEnabled(true); // Re-enable after scan
            }
        };

        worker.execute(); // Start thread  in background
    }
    private Map<String, List<File>> findDuplicateFiles(File folder) {
        Map<String, List<File>> hashToFileList = new HashMap<>();

        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) {
                    String name = dir.toString().toLowerCase();
                    if (name.contains("$recycle.bin") ||
                            name.contains("system volume information") ||
                            name.contains("config.msi") ||
                            name.contains("pagefile.sys") ||
                            name.contains("hiberfil.sys") ||
                            name.contains("swapfile.sys")) {
                        System.out.println("Skipping protected system folder: " + name);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) {
                    if (Files.isRegularFile(path) && Files.isReadable(path)) {
                        try {
                            File file = path.toFile();
                            String hash = getFileHash(file);
                            hashToFileList
                                    .computeIfAbsent(hash, k -> new ArrayList<>())
                                    .add(file);
                        } catch (IOException | NoSuchAlgorithmException ex) {
                            System.err.println("Failed to hash file: " + path + " (" + ex.getMessage() + ")");
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(java.nio.file.Path file, IOException exc) {
                    System.err.println("Cannot access: " + file + " -> " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hashToFileList;
    }
    private String getFileHash(File file) throws IOException, NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try(InputStream ipstream = new FileInputStream(file)){
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = ipstream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashByte = digest.digest();
        StringBuilder sb = new StringBuilder();
        for(byte b : hashByte){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    private void onDeleteClicked(ActionEvent e) {
        int rowCount = tableModel.getRowCount();
        List<Integer> rowsToRemove = new ArrayList<>();
        int deletedCount = 0;

        for (int i = 0; i < rowCount; i++) {
            Boolean toDelete = (Boolean) tableModel.getValueAt(i, 0);
            if (Boolean.TRUE.equals(toDelete)) {
                String path = (String) tableModel.getValueAt(i, 2);
                File file = new File(path);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this file?\n" + path,
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION && file.delete()) {
                    rowsToRemove.add(i);
                    deletedCount++;
                } else if (!file.exists()) {
                    JOptionPane.showMessageDialog(this, "File already deleted: " + path);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete: " + path);
                }
            }
        }

        for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
            tableModel.removeRow(rowsToRemove.get(i));
        }

        JOptionPane.showMessageDialog(this, "Deleted " + deletedCount + " files.");
    }
}