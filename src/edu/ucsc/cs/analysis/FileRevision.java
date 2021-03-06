package edu.ucsc.cs.analysis;

public class FileRevision {
	int commitId;
	int fileId;
	String content;
	String sourceLevel;
	
	public FileRevision(int commitId, int fileId, String content) {
		this.commitId = commitId;
		this.content = content;
		this.fileId = fileId;
	}
	
	FileRevision(int commitId, int fileId, String content, String sourceLevel) {
		this.commitId = commitId;
		this.content = content;
		this.fileId = fileId;
		this.sourceLevel = sourceLevel;
	}
	
	@Override
	public String toString() {
		return "File " + fileId + " @ commit " + commitId + ' ';
	}
}
