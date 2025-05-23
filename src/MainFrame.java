import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

        //shows the selected folder path (initially no path selected)
        selectedPathLabel= new JLabel("No File Selected");
        selectedPathLabel.setHorizontalAlignment((SwingConstants.CENTER));
        add(selectedPathLabel, BorderLayout.CENTER);                        //we place this label to the top(north) section of the window

        //browsing button adding
        JButton browseButton = new JButton("Browse Files");
        browseButton.addActionListener(this::onBrowseClicked);              //when button is clicked, it will render the onBrowseCLicked() function
        add(browseButton, BorderLayout.SOUTH);

        JButton scanButton = new JButton("Browse Files");


        scanButton.setEnabled(false);                                       //keeps the button disabled until user selects a folder
        scanButton.addActionListener(this::onScanClicked);              //when button is clicked, it will render the onBrowseCLicked() function
        add(scanButton, BorderLayout.EAST);

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
        Map<String, java.util.List<File>> duplicateFiles = findDuplicateFiles(selectedFolder);

        System.out.println("\nDuplicate Files: ");

        int count=1;
        for(Map.Entry<String, java.util.List<File>> entry : duplicateFiles.entrySet()){               //iterating over each group of file with the same hash
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