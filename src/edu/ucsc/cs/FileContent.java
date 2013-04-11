package edu.ucsc.cs;

import java.util.HashMap;

class FileContent {
	static HashMap<Integer, FileContent> previousContent = new HashMap<Integer, FileContent>();
	int commitID;
	String content;
	FileContent(int commitID, String content) {
		this.commitID = commitID;
		this.content = content;
	}
}
