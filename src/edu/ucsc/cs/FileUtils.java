package edu.ucsc.cs;

import java.io.*;

public class FileUtils {
	public static File javaFileFromString(String fileName, String content) {
		File temp = null;
		try {
			temp = File.createTempFile(fileName, ".java");
			temp.deleteOnExit();
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(content);
			out.close();			
		} catch (Exception e) {
			LogManager.getLogger().severe(e.toString());
			System.exit(1);
		}
		return temp;
	}
}
