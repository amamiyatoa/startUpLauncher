import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public final class Main extends JFrame{
    
    private Path appPath;
    private List<String> apps;
    private final int winWidth = 854;
    private final int winHeight = 480;
    private final Path launchAppFile = setPath("launch.lcr");
    private boolean startUp = new Config().getConfig().isStartUpEnabled;
    
    private final String version = "0.0.1";
    
    private JPanel mainPanel;
    
    private final FileSystemView fsv = FileSystemView.getFileSystemView();
    private final JFileChooser chooser = new JFileChooser();
    private final Desktop desktop = Desktop.getDesktop();
    
    private final JMenuBar menubar = new JMenuBar();
    private final JMenu file = new JMenu("File(_F)");
    private final JMenuItem openFile = new JMenuItem("Open File");
    private final JMenuItem ver = new JMenuItem("Version");
    private final JCheckBoxMenuItem start = new JCheckBoxMenuItem("StartUp");
    
    
    private Main() {
        init();
        startUp();
    }
    
    public void init(){
        setTitle("StartUp Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(winWidth, winHeight);
        setResizable(false);
        setLocationRelativeTo(null);
        
        Config conf = new Config();
        try {
            conf.ReadConfig("config.lcr");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        
        
        mainPanel = new JPanel();
        
        try {
            if(!Files.exists(launchAppFile)) {
                System.err.println("[Fatal] Launch file was not exist");
                System.exit(0);
            }
            mainPanel.setLayout(new GridLayout(0,4));
            
            apps = Files.readAllLines(launchAppFile);
            setApps(apps);
            refreshDisplay(apps);
            add(mainPanel);
        } catch (IOException fileEx) {
            System.err.println("[Fatal] " + fileEx.getMessage());
        }
        
        openFile.addActionListener(e -> {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int fileChooserResult = chooser.showOpenDialog(null);
            
            if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
                try {
                    String selectedFilePath = chooser.getSelectedFile().getPath();
                    writer(selectedFilePath, true);
                    apps = Files.readAllLines(getPath());
                    refreshDisplay(apps);
                } catch (IOException readErr) {
                    System.err.println("[Fatal]" + readErr.getMessage());
                }
            }
        });
        
        start.addActionListener(e -> {
            startUp = start.getState();
        });
        
        ver.addActionListener(e -> {
            try {
                Config config = new Config();
                config.ReadConfig("config.lcr");
                startUp = conf.getConfig().isStartUpEnabled;
                System.out.println(startUp);
                JOptionPane.showMessageDialog(
                        this,
                        "Version: " + version + "\nStartUp: " + startUp,
                        "Version Information",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        });
        
        file.add(openFile);
        file.add(start);
        file.add(ver);
        menubar.add(file);
        setJMenuBar(menubar);
        
        setVisible(true);
    }
    
    public void writer(String wroteData, boolean overwrite) {
        try {
            Path launcherAppPath = getPath();
            String pathString = launcherAppPath.toString();
            
            try(FileWriter writer = new FileWriter(pathString, overwrite)) {
                String writeData = wroteData + "\n";
                writer.write(writeData);
            } catch(IOException fileEx) {
                System.err.println("[Fatal]" + fileEx.getMessage());
            }
            
        } catch (HeadlessException hEx) {
            System.err.println("[Fatal]" + hEx.getMessage());
        }
    }
    
    public Path setPath(String filePath) {
        appPath = Paths.get(filePath);
        return appPath;
    }
    public Path getPath() {
        return appPath;
    }
    
    public void setApps(List<String> applications) {
        apps = applications;
    }
    public List<String> getApps() {
        return apps;
    }
    
    public void startUp() {
        if(startUp) {
            Path launchAppPath = getPath();
            try {
                List<String> paths = Files.readAllLines(launchAppPath);
                for (String path : paths) {
                    try {
                        fileOpener(path);
                    } catch (IOException e) {
                        System.err.println("[Fatal] " + e.getMessage());
                    }
                }
            } catch (IOException fileEx) {
                System.err.println("[Fatal] " + fileEx.getMessage());
            }
        }
        
    }
    
    public void refreshDisplay(List<String> appList) {
        mainPanel.removeAll();
        setApps(appList);
        for(String app : appList){
            Icon icon = fsv.getSystemIcon(new File(app), 64, 64);
            JLabel label = new JLabel(icon);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e){
                    if(e.getButton() == MouseEvent.BUTTON1) {
                        try {
                            fileOpener(app);
                            System.out.println("[Start] " + app);
                        } catch (IOException appErr) {
                            System.err.println("[Fatal] " + appErr.getMessage());
                        }
                    } else if(e.getButton() == MouseEvent.BUTTON3) {
                        try {
                            removePath(appList, app);
                        } catch (Exception appErr) {
                            System.err.println("[Fatal] " + appErr.getMessage());
                        }
                    }
                }
            });
            mainPanel.add(label);
            System.out.println("[Add] " + app);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
        System.out.println("再表示しました");
    }
    
    public void removePath(List<String> appList,String appPath){
        writer("",false);
        appList.remove(appPath);
        setApps(appList);
        for(String application : appList) {
            writer(application,true);
        }
        refreshDisplay(appList);
        System.out.println("[Remove] " + appPath + "was removed");
    }
    
    public void fileOpener(String openFileName) throws IOException {
        if(Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.OPEN)) {
            File desktopOpenFile = new File(openFileName);
            var fileExtension = desktopOpenFile.getName().substring(desktopOpenFile.getName().lastIndexOf(".") + 1);
            if("EXE".equalsIgnoreCase(fileExtension)) {
                new ProcessBuilder(openFileName).start();
            } else {
                desktop.open(desktopOpenFile);
            }
            /*
            try {
                desktop.open(desktopOpenFile);
            } catch (IOException e) {
                System.err.println("[Fatal] " + e.getMessage());
            }
             */
        }
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(Main::new);
    }
}