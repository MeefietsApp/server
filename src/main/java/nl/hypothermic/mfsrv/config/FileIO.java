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

import org.apache.commons.codec.DecoderException;

import nl.hypothermic.mfsrv.MFLogger;

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
			throw new RuntimeException("FileIO#readFileContentsUnsafe: " + file.getAbsolutePath());
		}
	}

	// Gebruik alleen als het zeker is dat het bestand bestaat + w rechten
	public static final void writeFileContentsUnsafe(File file, String contents) {
		try {
			FileWriter fw = new FileWriter(file, false);
			fw.write(contents);
			fw.close();
		} catch (IOException iox) {
			throw new RuntimeException("FileIO#writeFileContentsUnsafe: " + file.getAbsolutePath());
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
		return org.apache.commons.codec.binary.Hex.encodeHexString(baos.toByteArray());
	}

	public static final Serializable deserializeFromString(String str)
			throws IOException, ClassNotFoundException, DecoderException {
		byte[] bytes = org.apache.commons.codec.binary.Hex.decodeHex(str);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream in = new ObjectInputStream(bais);
		Serializable ser = (Serializable) in.readObject();
		in.close();
		return ser;
	}
}
