package daemonTh;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Worker implements Runnable {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	PathInfo pathInfo;

	HashMap<String,FileInfo> actualFilesInfo = null;
	boolean valid = true,firstStart,printS=false;
	long tStart = System.currentTimeMillis();

	public Worker(PathInfo pi,boolean firstStart,boolean printS) {
		actualFilesInfo = new HashMap<>();
		this.pathInfo = pi;
		this.firstStart = firstStart;
		this.printS = printS;
	}

	@Override
	public void run() {

		System.out.println("[THREAD] Started - " + pathInfo.getPath());

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[THREAD] Stoping - " + pathInfo.getPath());
				//Save
				//saveMaps();
				Main.shutdownRequested = true;
				//Main.shutdown();
				return;
			}
		});
		boolean remainsValid = false;
		HashSet<String> allFiles = new HashSet<>();
		while(valid) {
			//path = "C:/Fraps/";
			remainsValid = true;
			listAllFiles(pathInfo.getPath(), allFiles);
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
					info.setValid(ok);

					actualFilesInfo.put(info.getFilename(), info); // Update Actual FileInfo data
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if(printS)
			{	printStats(actualFilesInfo);
				if(remainsValid)
					System.out.println("State:"+ANSI_CYAN + " VALID - " + ANSI_RESET +pathInfo.getPath()+"\n\n");
				else
					System.out.println("State:"+ANSI_PURPLE + " NOT VALID - " + ANSI_RESET +pathInfo.getPath()+"\n\n");

			}

			if(firstStart)
				firstStart=false;
			try {
				Thread.sleep(pathInfo.getCheckSeconds());
			} catch (InterruptedException e) {
				return;
			}
		}
		//notify();
	}

	private boolean checkFileInfo(FileInfo info) {

		FileInfo infoBd = this.pathInfo.getValidFilesInfo().get(info.getFilename());

		File finfo = new File(info.getFilename());
		File fpai = finfo.getParentFile();
		boolean folder = fpai.isDirectory(),cw=fpai.canWrite(),cr=fpai.canRead(),ce=fpai.canExecute();

		if(infoBd==null){
			info.setState(FileInfo.State.NEW);
			if(finfo.getParentFile().canWrite() == false)// Parent Folder canWrite=false and A new File Appeared
				return false;

			return true;}

		if(infoBd.equals(info))
			return true;

		// Check Permissions

		if(info.CanWrite()==false && info.getLastModified()!=infoBd.getLastModified())// File Modified and canWrite=False
			return false; // Alert !!!

		else if(finfo.getParentFile().canWrite() == false && info.getLastModified()!=infoBd.getLastModified())// Parent Folder canWrite=false and A new File Appeared
			return false;

		else if(info.CanWrite()==true && finfo.getParentFile().canWrite() == true && info.getLastModified()!=infoBd.getLastModified())// File Modified and canWrite=True
			{ info.setState(FileInfo.State.UPDATED);
			  return true;}


			return true;
	}


	public FileInfo getInfo(String filename){
		File f = new File(filename);
		FileInfo.State state = FileInfo.State.OK;
		if(f==null)
			return null;
		byte[] array = null;
		if(f.exists())
			try {
				array = createChecksum(filename);
				state = FileInfo.State.OK;
			}catch (Exception e){
				System.out.println("Error while creating File's Checksum -> "+filename);
			}
		else
			state = FileInfo.State.DELETED;
		FileInfo fi = new FileInfo(filename,array,f.canRead(),f.canWrite(),f.canExecute(),f.lastModified(),state,false);
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
		if(listOfFiles!=null)
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
		Main.validPathsInfo.put(pathInfo.getPath(), pathInfo);
	}

	public void saveFileInfo(FileInfo fi){
		// Replace existing Data for this path
		/*HashMap<String,FileInfo> map = Main.validPathsInfo.get(path);
		if(map==null)
			Main.validPathsInfo.put(path, new HashMap<>());
		Main.validPathsInfo.get(path).put(fi.getFilename(), fi);*/

		this.pathInfo.getValidFilesInfo().put(fi.getFilename(), fi);
	}

	public void printStats(HashMap<String,FileInfo> map){
		//clearConsole();
		printTime();
		System.out.println("\nPATH = " + pathInfo.getPath());
		for(FileInfo info : map.values()) {
			if(info.isValid())
				System.out.print(ANSI_GREEN + " [   VALID   ] " + ANSI_RESET);
			else
				System.out.print(ANSI_RED + " [ NOT VALID ] " + ANSI_RESET);

			System.out.println(info.getFilename());
			/*
			switch (info.getState()) {
				case NEW:
					System.out.println(info.getFilename());
					break;
				case DELETED:
					System.out.println(ANSI_RED + info.getFilename() + ANSI_RESET);
					break;
				case OK:
					System.out.println(ANSI_BLUE + info.getFilename() + ANSI_RESET);
					break;
				case UPDATED:
					System.out.println(ANSI_YELLOW + info.getFilename() + ANSI_RESET);
					break;
			}*/
		}
	}

	public final void printTime() {

		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		tDelta/=1000; // to Seconds
		int hours = (int)(tDelta / 3600);
		int mins = (int)((tDelta % 3600) / 60);
		int secs = (int)(tDelta % 60);

		System.out.println("\n\nElapsed Time : "+hours+"h "+mins+"m "+secs+"s");
	}


	public final void clearConsole() {

			for (int clear = 0; clear < 50; clear++) {
				System.out.println("\b");
			}
	}
}
