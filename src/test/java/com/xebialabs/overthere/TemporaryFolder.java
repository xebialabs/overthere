package com.xebialabs.overthere;

import java.io.File;
import java.io.IOException;

public class TemporaryFolder {
	
	private File tempRoot;
	
	public void create() throws IOException {
		tempRoot = File.createTempFile("testng", ".tmp");
		tempRoot.delete();
		tempRoot.mkdir();
	}

	public void delete() {
		recursiveDelete(tempRoot);
	}
	
	public File getRoot() {
		return tempRoot;
	}

	private void recursiveDelete(File file) {
		File[] files= file.listFiles();
		if (files != null)
			for (File each : files)
				recursiveDelete(each);
		file.delete();
	}

	public File newFile(String s) throws IOException {
		File f = new File(tempRoot, s);
		f.createNewFile();
		return f;
	}

	public File newFolder(String s) {
		File folder = new File(tempRoot, s);
		folder.mkdir();
		return folder;
	}
}
