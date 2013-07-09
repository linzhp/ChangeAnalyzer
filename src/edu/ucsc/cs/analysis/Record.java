package edu.ucsc.cs.analysis;

class Record {
	int training = 0;
	int test = 0;
	
	void increase(String setName, int by) {
		switch (setName) {
		case "training":
			training += by;
			break;
		case "test":
			test += by;
			break;
		}
	}
}
