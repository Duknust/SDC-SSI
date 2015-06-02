package daemonTh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Worker implements Runnable {
	//static ArrayList<String> allFiles = new ArrayList<>();
	static HashSet<String> allFiles = new HashSet<>();
	//static ArrayList<byte[]> validFilesHash = null;
	static HashMap<String,byte[]> validFilesHash = null;
	static String path = "";
	static boolean valid = true;

	public Worker(HashMap<String,byte[]> validFilesHash, String path) {
		this.validFilesHash = validFilesHash;
		this.path = path;
	}

	@Override
	public void run() {
		boolean remainsValid = false;
		while(valid) {
			//path = "C:/Fraps/";
			listAllFiles(path, allFiles);
			HashMap<String,byte[]> actualFilesHash = new HashMap<String,byte[]>();
			for (String filename : allFiles) {
				byte[] newHash = new byte[1024];
				try {
					newHash = createChecksum(filename);
				} catch (Exception e) {
					e.printStackTrace();
				}
				actualFilesHash.put(filename,newHash);
			}

			if (validFilesHash != null) {
				//remainsValid = compareList(validFilesHash, actualFilesHash);
				//remainsValid = validFilesHash.equals(actualFilesHash);
				remainsValid = mapsAreEqual(validFilesHash, actualFilesHash);
				//remainsValid = remainsValid;
			} else {
				validFilesHash = actualFilesHash;
				remainsValid = true;
			}
			System.out.println("DONE, State:"+remainsValid);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		notify();
	}
/*
	public static void main(String[] args) {
		path = "C:/Fraps/";
		listAllFiles(path, allFiles);
		ArrayList<byte[]> actualFilesHash = new ArrayList<byte[]>();
		for (String filename : allFiles) {
			byte[] newHash = calculateHash(filename);
			actualFilesHash.add(newHash);
		}
		boolean remainsValid = false;
		if (validFilesHash != null) {
			remainsValid = compareList(validFilesHash, actualFilesHash);
			Main.remainsValid = remainsValid;
		} else {
			validFilesHash = actualFilesHash;
			Main.remainsValid = true;
		}
	}
*/

	/*
	private static  boolean compareList(ArrayList<byte[]> validFilesHash,
										ArrayList<byte[]> actualFilesHash) {

		if (validFilesHash.size() == actualFilesHash.size()) {
			for (byte[] afh : actualFilesHash) {
				if (!validFilesHash.contains(afh)) {
					return false;
				}
			}
		}
		else
			return false;
		return true;
	}
*/

	public boolean mapsAreEqual(HashMap<String, byte []> mapA, HashMap<String, byte []> mapB) {

		try{
			for (String k : mapB.keySet())
			{
				if (Arrays.equals(mapA.get(k),mapB.get(k))==false) {
					return false;
				}
			}
			for (String y : mapA.keySet())
			{
				if (!mapB.containsKey(y)) {
					return false;
				}
			}
		} catch (NullPointerException np) {
			return false;
		}
		return true;
	}

	private static byte[] calculateHash(String filename) {
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

	public static byte[] createChecksum(String filename) throws Exception {
		InputStream fis =  new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	private static void listAllFiles(String path, HashSet<String> result) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				result.add(listOfFiles[i].getAbsolutePath());
				//System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				listAllFiles(listOfFiles[i].getAbsolutePath(), result);
				//System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
	}

}
