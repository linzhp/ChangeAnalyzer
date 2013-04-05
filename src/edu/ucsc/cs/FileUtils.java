package edu.ucsc.cs;

import java.io.*;

public class FileUtils {
	public static File javaFileFromString(String fileName, String content)
			throws Exception {
		File temp = File.createTempFile(fileName, ".java");
		temp.deleteOnExit();
		BufferedWriter out = new BufferedWriter(new FileWriter(temp));
		out.write(content);
		out.close();
		return temp;
	}
}
