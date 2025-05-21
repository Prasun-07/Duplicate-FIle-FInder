import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class FileHasher extends JFrame{
    private File selectedFolder;
    private JButton selectButton;
    private JLabel selectedPathLabel;
    private JButton scanButton;

    public FileHasher() {
        setTitle("Duplicate FIle Finder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setLayout(new BorderLayout());

        //shows the selected folder path (initially no path selected)
        selectedPathLabel= new JLabel("No File Selected");
        selectedPathLabel.setHorizontalAlignment((SwingConstants.CENTER));
        add(selectedPathLabel, BorderLayout.NORTH);                        //we place this label to the top(north) section of the window

        //browsing button adding
        JButton selectButton = new JButton("Browse Files");
        selectButton.addActionListener(this::onBrowseClicked);              //when button is clicked, it will render the onBrowseCLicked() function
        add(selectButton, BorderLayout.WEST);

        JButton scanButton = new JButton("Browse Files");
        scanButton.setEnabled(false);                                       //keeps the button disabled until user selects a folder
        scanButton.addActionListener(this::onScanClicked);              //when button is clicked, it will render the onBrowseCLicked() function
        add(scanButton, BorderLayout.EAST);

        setVisible(true);
    }
    private void onBrowseClicked(ActionEvent e){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            File chosenFolder = chooser.getSelectedFile();
            selectedPathLabel.setText("Selected: "+chosenFolder.getAbsolutePath());
            scanButton.setEnabled(true);                                            //enables the scanning button
        }
    }
    private void onScanClicked(ActionEvent e){

        //DO not proceed until a folder is select
        if(selectedFolder == null){
            JOptionPane.showMessageDialog(this,"Select a Folder");      //if no folder is selected
            return;
        }

        //Creating map to hash the folders
        Map<String, List<File>> duplicateFiles = findDuplicateFiles(selectedFolder);

        System.out.println("\nDuplicate Files: ");

        int count=1;
        for(Map.Entry<String, List<File>> entry : duplicateFiles.entrySet()){               //iterating over each group of file with the same hash
            List<File> files = entry.getValue();
            if(files.size() > 1){
                System.out.println("Group "+count+": ");
                for(File file : files)  System.out.println(" "+file.getAbsolutePath());     //if group has more than one file (from for loop), we print it as a duplicate file
                count++;
            }
        }
        if(count == 1){
            System.out.println("No Duplicates Found");
        }
    }

    private Map<String, List<File>> findDuplicateFiles(File folder){
        Map<String, List<File>> hashToFileList = new HashMap<>();

        try{
            Files.walk(folder.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try{
                            String hash = getFileHash(path.toFile());
                            hashToFileList
                                    .computeIfAbsent(hash, k -> new ArrayList<>())
                                    .add(path.toFile());
                        }catch(IOException | NoSuchAlgorithmException ex){
                            ex.printStackTrace();
                        }
                    });
        }catch(IOException e){
            e.printStackTrace();
        }

        return hashToFileList;
    }

    private String getFileHash(File file) throws IOException, NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try(InputStream ipstream = new FileInputStream(file)){
            byte[] buffer = new byte[4096];
            int byteRead = ipstream.read(buffer);
            while(byteRead != -1){
                digest.update(buffer, 0, byteRead);
            }
        }
        byte[] hashByte = digest.digest();
        StringBuilder sb = new StringBuilder();
        for(byte b : hashByte){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
