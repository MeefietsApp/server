package nl.hypothermic.mfsrv.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import nl.hypothermic.mfsrv.MFServer;

public class ConfigHandler {
	
	//public static final File configPath = new File("~/.mfsrv/");
	public static final File configPath = new File(System.getProperty("user.home") + "/.mfsrv/");
	public static final File dbPath = new File(configPath, "db/");
	
	public ConfigHandler() {
		configPath.mkdirs();
		dbPath.mkdir();
	}
}
