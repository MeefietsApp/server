package nl.hypothermic.mfsrv.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigHandler {
	
	public static ConfigHandler instance;
	
	//public static final File configPath = new File("~/.mfsrv/");
	public static final File configPath = new File(System.getProperty("user.home") + "/.mfsrv/");
	public static final File configFile = new File(configPath, "mfsrv.cfg");
	public static final File dbPath = new File(configPath, "db/");
	
	private final Properties properties = new Properties();
	
	public ConfigHandler() throws IOException {
		instance = this;
		configPath.mkdirs();
		dbPath.mkdir();
		
		configFile.createNewFile();
		properties.load(new FileInputStream(configFile));
	}
	
	public String getString(String key, String def) {
		if (properties.containsKey(key)) {
			return (String) properties.get(key);
		} else {
			return def;
		}
	}
	
	public String getStringOrCrash(String key) {
		if (properties.containsKey(key)) {
			return (String) properties.get(key);
		} else {
			throw new RuntimeException("Could not retrieve key: " + key);
		}
	}
}
