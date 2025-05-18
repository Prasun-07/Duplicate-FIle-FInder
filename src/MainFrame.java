import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MainFrame extends JFrame {

    private JLabel selectedPathLabel;
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

        setLocationRelativeTo(null);
        setVisible(true);


    }

    //function for when browse button is clicked
    public void onBrowseClicked(ActionEvent e){
        JFileChooser chooser = new JFileChooser();                      //navigate through files ( dialog box)
        chooser.setDialogTitle("Select A Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);    //sets the file selection to be able to choose only directories
        chooser.setAcceptAllFileFilterUsed(false);                      //blocks from selecting all files

        int chosenOption = chooser.showOpenDialog(this);
        if(chosenOption == JFileChooser.APPROVE_OPTION){
            File chosenFolder = chooser.getSelectedFile();
            selectedPathLabel.setText("Selected: "+chosenFolder.getAbsolutePath());
        }
    }
}