package daemonTh;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

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

	private PathInfo pathInfo;

	private HashMap<String,FileInfo> actualFilesInfo = null;
	private boolean valid = true,firstStart,printS=false;
	private long tStart = System.currentTimeMillis();

	public Worker(PathInfo pi,boolean firstStart,boolean printS) {
		actualFilesInfo = new HashMap<>();
		this.pathInfo = pi;
		this.firstStart = !firstStart;
		this.printS = printS;
	}

	@Override
	public void run() {

		//System.out.println("[THREAD] Started - " + pathInfo.getPath());
/*
		// SIGTERM
		Signal.handle(new Signal("TERM"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[THREAD] Stopping - " + pathInfo.getPath());
				//Save
				//savePath();
				Main.shutdownRequested = true;
				//Main.shutdown();
				return;
			}
		});
*/


		//SIGUSR1 - Reload Config File
		Signal.handle(new Signal("USR2"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				//Save
				//savePath();
				Main.goReload = true;
				//Main.shutdown();
				return;
			}
		});


/*

		//SIGTRAP - Pause/Resume
		Signal.handle(new Signal("TRAP"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[THREAD] Pausing - " + pathInfo.getPath());
				//Save
				//savePath();

				Main.pauseResume=true;
				//Main.shutdown();

				return;
			}
		});
*/
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
					{
						saveFileInfo(info);}
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

			//if(remainsValid)
				savePath();

			if(printS)
			{	printStats(actualFilesInfo);
				if(remainsValid)
					System.out.println("State:"+ANSI_CYAN + " VALID" + ANSI_RESET+"\n\n");
				else
					System.out.println("State:"+ANSI_PURPLE + " NOT VALID" + ANSI_RESET +"\n\n");

			}

			//System.out.println("\n"+this.pathInfo.getValidFilesInfo().size() + " " + this.pathInfo.toString()+"\n\n");
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



		FileInfo fipai = pathInfo.getValidFilesInfo().get(fpai.getAbsolutePath());

		if(fipai==null)
			return true;

		boolean folder = fipai.isDirectory(),
				cw=fipai.CanWrite(),
				cr=fipai.CanRead(),
				ce=fipai.CanExecute();



		if(infoBd==null){

			info.setState(FileInfo.State.NEW);
			if(fipai.CanWrite() == false)// Parent Folder canWrite=false and a new File Appeared
				return false;

			return true;}


		if(infoBd.isDirectory())
			return true;

		if(infoBd.equals(info))
			return true;

		// Check Permissions

		if(info.CanWrite()==false && info.getLastModified()!=infoBd.getLastModified())// File Modified and canWrite=False
			return false; // Alert !!!

		else if(fipai.CanWrite() == false && info.getLastModified()!=infoBd.getLastModified())// Parent Folder canWrite=false and A new File Appeared
			return false;

		else if(info.CanWrite()==true && fipai.CanWrite() == true && info.getLastModified()!=infoBd.getLastModified())// File Modified and canWrite=True
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
		boolean isDir = false;
		if(f.exists()) {
			isDir = f.isDirectory();
			if(isDir==false)
				{try {
					array = createChecksum(filename);
					} catch (Exception e) {
						System.out.println("Error while creating File's Checksum -> " + filename);
					}
				}
		}
		else
			state = FileInfo.State.DELETED;
		FileInfo fi = new FileInfo(filename,array,f.canRead(),f.canWrite(),f.canExecute(),f.lastModified(),state,false,isDir);
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
				result.add(listOfFiles[i].getAbsolutePath());
				if (listOfFiles[i].isFile()) {
					//result.add(listOfFiles[i].getAbsolutePath());
					//System.out.println("File " + listOfFiles[i].getName());
				} else if (listOfFiles[i].isDirectory()) {
					listAllFiles(listOfFiles[i].getAbsolutePath(), result);
					//System.out.println("Directory " + listOfFiles[i].getName());
				}
			}
	}

	public void savePath(){
		// Replace existing Data for this path
		//Main.validPathsInfo.put(pathInfo.getPath(), pathInfo);
		Main.validPathsInfo.put(pathInfo.getPath(),pathInfo);
		//System.out.println("SAVING "+pathInfo.getPath()+":"+pathInfo.getValidFilesInfo().size());
	}

	public void saveFileInfo(FileInfo fi){
		// Replace existing Data for this path
		/*HashMap<String,FileInfo> map = Main.validPathsInfo.get(path);
		if(map==null)
			Main.validPathsInfo.put(path, new HashMap<>());
		Main.validPathsInfo.get(path).put(fi.getFilename(), fi);*/

		this.pathInfo.putValidFileInfo(fi.getFilename(), fi);
	}

	public void printStats(HashMap<String,FileInfo> map){
		//clearConsole();
		printTime();
		System.out.println("\nPATH = " + pathInfo.getPath());
		for(FileInfo info : map.values()) {
			if(info.isValid())
				System.out.print(ANSI_GREEN + " [   VALID   ] " + ANSI_RESET + info.CanRead() + "_" + info.CanWrite() + "_" + info.CanExecute());
			else
				{System.out.print(ANSI_RED + " [ NOT VALID ] " + ANSI_RESET + info.CanRead() + "_" + info.CanWrite() + "_" + info.CanExecute());
				 Main.alert("NOT VALID - " +info.getFilename());}

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
