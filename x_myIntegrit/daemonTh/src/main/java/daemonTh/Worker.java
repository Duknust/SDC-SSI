package daemonTh;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.logging.Logger;

public class Worker implements Runnable {
	HashSet<String> allFiles = new HashSet<>();
	HashMap<String,FileInfo> validFilesInfo = null;
	HashMap<String,FileInfo> actualFilesInfo = null;
	String path = "";
	boolean valid = true,firstStart;

	public Worker(String path,HashMap<String,FileInfo> validFilesInfo,boolean firstStart) {
		this.path = path;
		actualFilesInfo = new HashMap<>();
		this.validFilesInfo = validFilesInfo;
		this.firstStart = firstStart;
	}

	@Override
	public void run() {
		boolean remainsValid = false;
		while(valid) {
			//path = "C:/Fraps/";
			remainsValid = true;
			listAllFiles(path, allFiles);
			for (String filename : allFiles) {
				//byte[] newHash = new byte[1024];
				try {
					//newHash = createChecksum(filename);
					FileInfo info = getInfo(filename);
					if(firstStart)
						saveFileInfo(info);
					boolean ok = checkFileInfo(info);
					if(ok==false){
						remainsValid = false;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			System.out.println("DONE, State:"+remainsValid);
			if(firstStart)
				firstStart=false;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		notify();
	}

	private boolean checkFileInfo(FileInfo info) {

		FileInfo infoBd = this.validFilesInfo.get(info.getFilename());
		if(infoBd==null)
			return false;

		if(infoBd.equals(info))
			return true;

		// Check Permissions

		// File Modified and canWrite=False
		if(info.canWrite==false && info.getLastModified()!=infoBd.getLastModified())
			return false; // Alert !!!
		// File Modified and canWrite=True
		else if(info.canWrite==true && info.getLastModified()!=infoBd.getLastModified())
			return true;
		else
			return false;



	}


	public FileInfo getInfo(String filename){
		File f = new File(filename);Random rm;
		if(f==null)
			return null;
		byte[] array = null;
		try {
			array = createChecksum(filename);
		}catch (Exception e){
			System.out.println("Error while creating File's Checksum -> "+filename);
		}

		FileInfo fi = new FileInfo(filename,array,f.canRead(),f.canWrite(),f.canExecute(),f.lastModified());
		return fi;
	}

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

	public void saveMaps(){
		// Replace existing Data for this path
		Main.validPathsInfo.put(path, validFilesInfo);
	}

	public void saveFileInfo(FileInfo fi){
		// Replace existing Data for this path
		HashMap<String,FileInfo> map = Main.validPathsInfo.get(path);
		if(map==null)
			Main.validPathsInfo.put(path, new HashMap<>());
		Main.validPathsInfo.get(path).put(fi.getFilename(), fi);
	}
}
