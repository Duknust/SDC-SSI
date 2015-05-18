package daemonTh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Worker implements Runnable {
	ArrayList<String> allFilesName = null;
	ArrayList<byte[]> validFilesHash = null;
	String path = "";

	public Worker(ArrayList<byte[]> validFilesHash, String path) {
		this.validFilesHash = validFilesHash;
		this.path = path;
	}

	@Override
	public void run() {
		listAllFiles(this.path, allFilesName);
		ArrayList<byte[]> actualFilesHash = new ArrayList<byte[]>();
		for (String filename : allFilesName) {
			byte[] newHash = calculateHash(filename);
			actualFilesHash.add(newHash);
		}
		boolean remainsValid = false;
		if (this.validFilesHash != null) {
			remainsValid = compareList(this.validFilesHash, actualFilesHash);
			Main.remainsValid = remainsValid;
		} else {
			this.validFilesHash = actualFilesHash;
			Main.remainsValid = true;
		}

	}

	private boolean compareList(ArrayList<byte[]> validFilesHash,
			ArrayList<byte[]> actualFilesHash) {
		if (validFilesHash.size() == actualFilesHash.size()) {
			for (byte[] afh : actualFilesHash) {
				if (!validFilesHash.contains(afh)) {
					return false;
				}
			}
		}
		return true;
	}

	private byte[] calculateHash(String filename) {
		MessageDigest md = null;

		try (InputStream is = Files.newInputStream(Paths.get(filename))) {
			md = MessageDigest.getInstance("MD5");
			DigestInputStream dis = new DigestInputStream(is, md);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		byte[] digest = md.digest();
		return digest;

	}

	private void listAllFiles(String path, ArrayList<String> result) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				result.add(listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				listAllFiles(path + "/" + listOfFiles[i].getName(), result);
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
	}

}
