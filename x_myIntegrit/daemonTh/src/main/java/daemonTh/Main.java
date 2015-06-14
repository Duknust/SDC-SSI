package daemonTh;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Main {

	static boolean remainsValid;
	static HashMap<String,PathInfo> validPathsInfo = null;
	static protected boolean shutdownRequested = false;
	static Worker wk =null;
	static HashMap<String,Thread> pathThread = null;
	static boolean inter = false,goReload=false,pauseResume=false,validfileOK=false,shuttingdown=false;
	static String pidfile, conffile, validfile,savefile;

	static public void shutdown()
	{
		shutdownRequested = true;
		shuttingdown=true;
		
		alert("Stoping Threads");
		for(Thread t : pathThread.values())
			if(t.isAlive())
				t.interrupt();
		alert("Threads Stopped");

		if(saveData(validPathsInfo,savefile)==false)
			alert("Save Failed");
		else
			alert("Save OK");

		alert("Ended");
	}

	static public boolean isShutdownRequested()
	{
		return shutdownRequested;
	}

	public static void main(String[] args) {


		//SIGTRAP - Pause/Resume
		Signal.handle(new Signal("TRAP"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				//Save
				//savePath();

				if (Main.pauseResume == false) {
					pauseResume = true;
					alert("Pausing");
				} else {
					pauseResume = false;
					alert("Resuming");
				}

				//pauseResume();
				return;
			}
		});

		alert("Started");


		if(loadConf()==false) {
			alert("Valid File Load Failed");
			 validfileOK=false;
			alert("Loading Configuration File");}
		else {
			alert("Valid File Load OK");
			 validfileOK=true;}

		addDaemonShutdownHook();
		pathThread = new HashMap<>();

		while(!isShutdownRequested())
		{
			if(goReload)
				{reloadConf();goReload=false;}
			else if(pauseResume)
				pauseResume();
			else {
				for (String path : validPathsInfo.keySet()) {
					try {
						Thread.sleep(500);
						Thread t1;
						t1 = pathThread.get(path);
						if (t1 == null) {

							Worker wk = new Worker(validPathsInfo.get(path), validfileOK, inter);
							t1 = new Thread(wk, "t1");
							pathThread.put(path, t1);
							//t1.setDaemon(true);
							t1.start();
						} else if (t1.isAlive() == false) {
							Worker wk = new Worker(validPathsInfo.get(path), validfileOK, inter);
							t1 = new Thread(wk, "t1");
							pathThread.put(path, t1);
							//t1.setDaemon(true);
							t1.start();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//System.out.print('.');
				}
				saveData(validPathsInfo,savefile);
			}
		}
		alert("Shutting Down");
		if (shuttingdown==false)
			shutdown();


	}

	// Load Data from File
	private static boolean loadData() {
		FileInputStream fin = null;
		File fi = new File(validfile);
		HashMap<String,PathInfo> map=null;
		try {
			if(fi.exists()==false)
			fi.createNewFile();
			fin = new FileInputStream(fi);
			ObjectInputStream ois = new ObjectInputStream(fin);
			map = (HashMap) ois.readObject();
			fin.close();
		} catch (FileNotFoundException ex) {
			validPathsInfo = new HashMap<String,PathInfo>();
			return false;
		} catch (IOException | ClassNotFoundException ex) {
			validPathsInfo = new HashMap<String,PathInfo>();
			return false;
		}


		if(map==null)
			validPathsInfo = new HashMap<String,PathInfo>();
		else
			validPathsInfo = map;

		return true;
	}

	// Save Data to File
	public static boolean saveData(Object data,String file) {
		FileOutputStream fout = null;
		File fi = new File(file);
		try {
			if(fi.exists()==false)
				fi.createNewFile();
			fout = new FileOutputStream(fi);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(data);
			fout.flush();
			fout.close();
		} catch (Exception ex) {
			//ex.printStackTrace();
			return false;
		}
		return true;
	}

	static protected void addDaemonShutdownHook() {
		Runtime.getRuntime().addShutdownHook( new Thread() { public void run() { Main.shutdown(); }});
	}


	public static void pauseResume(){

		if(pauseResume==true) {
			for (Thread t : pathThread.values())
				if (t.isAlive())
					t.interrupt();
			pathThread.clear();
			alert("Paused");
			while(pauseResume) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}else
		{
			alert("Resumed");
		}
	}


	public static void reloadConf(){

		alert("Reloading Configuration File");
		for(Thread t:pathThread.values())
			if(t.isAlive())
				t.interrupt();

		loadConf();
	}

	public static boolean loadConf(){

		pidfile = "~/pidfile.txt";
		savefile = "~/bcIsave.data";
		validfile = "~/bcIvalid.data";
		conffile=System.getProperty("conf");
		if(conffile==null)
			conffile = "~/bcintegrit.conf";

		File c = new File(conffile);
			if(c.exists()==false)
				try {
					c.createNewFile();
				} catch (IOException e) {
					alert("Can't Create Configuration File in - "+conffile);
				}


		InputStream fis = null;
		ArrayList<PathInfo> PIlist = new ArrayList<>();

		try {
			fis = new FileInputStream(c);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			String line;
			Pattern p = Pattern.compile("(\\d+) (.+)");
			Pattern savep = Pattern.compile("savefile=(.+)");
			Pattern intep = Pattern.compile("interactive.*");
			Pattern valip = Pattern.compile("validfile=(.+)");
			Pattern pidfp = Pattern.compile("pidfile=(.+)");

			while ((line = br.readLine()) != null) {
				// For every path
				//path = line;

				int ms = -1;
				File fi = null;
				String path = "",msecs="";
				Matcher m = p.matcher(line);
				Matcher savem = savep.matcher(line);
				Matcher intem = intep.matcher(line);
				Matcher valim = valip.matcher(line);
				Matcher pidfm = pidfp.matcher(line);

				if (m.matches())
				{
					msecs = m.group(1);
					try{ms = Integer.parseInt(msecs);}
					catch (NumberFormatException exp){
						;
					}
					path = m.group(2);
					// New Entry
					HashMap<String, FileInfo> map = new HashMap<>();
					fi = new File(path);

					if(fi!=null && ms >= 1)
					{
						PathInfo pi = new PathInfo(path,new HashMap<>(),ms*1000);
						PIlist.add(pi);
						alert("Path Valid - " + msecs + "s " + path);}
					else
						alert("Path Invalid - " + line);
				}
				else if(savem.matches()){
					path = savem.group(1);
					fi = new File(path);
					if(fi!=null)
						savefile = path;
					else
						alert("Save File Invalid, using - "+savefile);
				}
				else if(valim.matches()){
					path = valim.group(1);
					fi = new File(path);
					if(fi!=null)
						validfile = path;
					else
						alert("Valid File Invalid, using - "+validfile);
				}
				else if(pidfm.matches()){
					path = pidfm.group(1);
					fi = new File(path);
					if(fi!=null)
						pidfile = path;
					else
						alert("Pid File Invalid, using - "+pidfile);
				}
				else if(intem.matches()){
					inter = true;
				}


			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File f = new File(pidfile);f.deleteOnExit();

		if(inter){
			System.out.println("Config     = " + conffile);
			System.out.println("Pid File   = " + pidfile);
			System.out.println("Valid File = " + validfile);
			System.out.println("Save File  = " + savefile);
			System.out.println("Interactive= " + (inter ? " ON":"OFF"));}


		boolean ret = loadData();
		if(ret==false)
			for(PathInfo pi : PIlist)
				validPathsInfo.put(pi.getPath(),pi);
		return ret;
	}

	public static void alert(String message){
		try {
			Process p = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "logger -t \"BC Integrit\" -i -p \"CRIT\" " + message});
			if(inter)
				System.out.println(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
