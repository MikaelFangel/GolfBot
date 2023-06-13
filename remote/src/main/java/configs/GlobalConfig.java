package configs;

import java.io.FileInputStream;
import java.util.Properties;

public class GlobalConfig {
    private static GlobalConfig globalConfig;
    private static Properties configProperties;

    private GlobalConfig() {
        var path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        configProperties = new Properties();
        try {
            FileInputStream read = new FileInputStream(path + "config.xml");
            configProperties.loadFromXML(read);
            read.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static Properties getConfigProperties() {
        if (globalConfig == null) {
            globalConfig = new GlobalConfig();
        }
        return configProperties;
    }
}
