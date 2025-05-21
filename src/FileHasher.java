import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.*;

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

    }
}
