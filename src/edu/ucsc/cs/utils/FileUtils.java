package edu.ucsc.cs.utils;

import java.io.*;

public class FileUtils {
	public static File javaFileFromString(String content, String fileName) {
		File temp = null;
		try {
			temp = File.createTempFile(fileName, ".java");
			temp.deleteOnExit();
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(content);
			out.close();			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return temp;
	}
}
