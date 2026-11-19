import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class Config {
    private final Configuration conf;
    public Config() {
        this.conf = new Configuration();
    }
    public Configuration getConfig() {
        return conf;
    }
    
    public void ReadConfig(String filePath) throws IOException {
        Properties prop = PropertiesHandler.load(filePath);
        String startUp = prop.getProperty("StartUp");
        if("TRUE".equalsIgnoreCase(startUp)) {
            conf.setConfig(true);
            System.out.println("startup: " + conf.isStartUpEnabled);
        } else if("FALSE".equalsIgnoreCase(startUp)) {
            conf.setConfig(false);
            System.out.println("startup: " + conf.isStartUpEnabled);
        } else {
            System.err.println("[Fatal]: 設定の読み込みができませんでした。設定項目を確認してください。");
        }
    }
    
    public static class Configuration{
        boolean isStartUpEnabled;
        Path configFilePath;
        Configuration() {
            this.isStartUpEnabled = false;
            this.configFilePath = Paths.get("config.lcr");
        }
        public boolean getStartUpEnabled() {
            return isStartUpEnabled;
        }
        Path getConfigFilePath() {
            return configFilePath.toAbsolutePath();
        }
        
        public void setConfig(boolean enabled) {
            this.isStartUpEnabled = enabled;
        }
    }
    
    public class PropertiesHandler {
        public static Properties load(File file) throws IOException {
            return load(file.getPath());
        }
        
        public static Properties load(String filePath) throws IOException {
            Properties properties = new Properties();
            FileInputStream fis = null;
            InputStreamReader isr = null;
            try {
                fis = new FileInputStream(filePath);
                isr = new InputStreamReader(fis, Charset.defaultCharset());
                properties.load(isr);
            } finally {
                if(isr != null) {
                    if(fis != null) {
                        fis.close();
                    }
                    isr.close();
                }
            }
            return properties;
        }
    }
}