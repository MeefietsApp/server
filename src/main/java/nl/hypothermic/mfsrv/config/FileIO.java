package nl.hypothermic.mfsrv.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public final class FileIO {
	
	public static final String readFileContents(File file) throws IOException {
		BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
		byte[] buffer = new byte[(int) file.length()];
		stream.read(buffer);
		stream.close();
		return new String(buffer);
	}
	
	public static final void writeFileContents(File file, String contents) throws IOException {
		FileWriter fw = new FileWriter(file, false);
		fw.write(contents);
		fw.close();
	}
	
	// Gebruik alleen als het zeker is dat het bestand bestaat + r rechten
	public static final String readFileContentsUnsafe(File file) {
		try {
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
			byte[] buffer = new byte[(int) file.length()];
			stream.read(buffer);
			stream.close();
			return new String(buffer);
		} catch (IOException iox) {
			throw new RuntimeException("FileIO#readFileContents: " + file.getAbsolutePath());
		}
	}

}
