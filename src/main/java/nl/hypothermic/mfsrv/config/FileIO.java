package nl.hypothermic.mfsrv.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
	
	public static final void serialize(File path, Serializable obj) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(obj);
		out.close();
		fos.close();
	}
	
	public static Object deserialize(File path) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(path);
		ObjectInputStream in = new ObjectInputStream(fis);
		Serializable ser = (Serializable) in.readObject();
		in.close();
		fis.close();
		return ser;
	}
	
	public static final String serializeToString(Serializable obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(obj);
		out.close();
		return baos.toString();
	}
	
	public static final Serializable deserializeFromString(String str) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
		ObjectInputStream in = new ObjectInputStream(bais);
		Serializable ser = (Serializable) in.readObject();
		in.close();
		return ser;
	}
}
