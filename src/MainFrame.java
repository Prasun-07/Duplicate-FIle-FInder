import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Duplicate File Finder");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center window

        JLabel label = new JLabel("Welcome to Duplicate File Finder");
        add(label);
    }
}