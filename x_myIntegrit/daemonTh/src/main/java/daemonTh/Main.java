package daemonTh;

import java.io.*;
import java.nio.charset.Charset;
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
	static boolean inter = false,goReload=false,pauseResume=false,validfileOK=false;
	static String pidfile, conffile, validfile,savefile;

	static public void shutdown()
	{
		shutdownRequested = true;
/*
		try
		{
			wk.wait();
		}
		catch(InterruptedException e)
		{
			System.out.println("Interrupted which waiting on main daemon thread to complete.");
		}*/
		System.out.println("[SYSTEM] Stoping Threads");
		for(Thread t : pathThread.values())
			if(t.isAlive())
				t.interrupt();
		System.out.println("[SYSTEM] Threads Stopped");
		if(saveData(validPathsInfo,savefile)==false)
			System.out.println("[SYSTEM] Save Failed");
		else
			System.out.println("[SYSTEM] Save OK");
		System.out.println("TOTAL VALIDOS:"+validPathsInfo.size());
		System.out.println("[myIntegrit] Ended");
	}

	static public boolean isShutdownRequested()
	{
		return shutdownRequested;
	}

	public static void main(String[] args) {

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("{SYSTEM] MAIN INTERRUPT");
				System.out.println("[SYSTEM] Stopping Threads");
				shutdownRequested = true;
				for (Thread t : pathThread.values())
					if (t.isAlive())
						t.interrupt();


			}
		});


		//SIGTRAP - Pause/Resume
		Signal.handle(new Signal("TRAP"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				//Save
				//saveMaps();

				if (Main.pauseResume == false) {
					pauseResume = true;
					System.out.println("{[SYSTEM] Pausing");
				} else {
					pauseResume = false;
					System.out.println("[SYSTEM] Resuming");
				}

				//pauseResume();
				return;
			}
		});

		System.out.println("[myIntegrit] Started");

		daemonize();
		if(loadData()==false)
			{System.out.println("[SYSTEM] Valid File Load Failed");
			 validfileOK=false;
			 System.out.println("[SYSTEM] Loading Configuration File");
			 loadConf();}
		else
			{System.out.println("[SYSTEM] Valid File Load OK");
			 validfileOK=true;}

		addDaemonShutdownHook();
		pathThread = new HashMap<>();

		while(!isShutdownRequested())
		{
			System.out.println("");
			if(goReload)
				{reloadConf();goReload=false;}
			else if(pauseResume)
				pauseResume();
			else
				for (String path : validPathsInfo.keySet()) {
					try {
						Thread.sleep(500);
						Thread t1;
						t1 = pathThread.get(path);
						if(t1==null) {

							Worker wk = new Worker(validPathsInfo.get(path), validfileOK,inter);
							t1 = new Thread(wk, "t1");
							pathThread.put(path, t1);
							//t1.setDaemon(true);
							t1.start();
						}
						else if(t1.isAlive()==false){
							Worker wk = new Worker(validPathsInfo.get(path), validfileOK,inter);
							t1 = new Thread(wk, "t1");
							pathThread.put(path, t1);
							//t1.setDaemon(true);
							t1.start();
						}
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					System.out.print('.');
				}
		}
		System.out.println("[SYSTEM] Shutting Down");
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
	private static boolean saveData(Object data,String file) {
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

	static public void daemonize(){

		pidfile=System.getProperty("daemon.pidfile");
		//pidfile="C:/Fraps/pidfile.txt";
		if(pidfile==null)
			pidfile = "~/pidfile.txt";

		conffile=System.getProperty("daemon.conf");
		//conffile="C:/Fraps/myintegrit.conf";
		if(conffile==null)
			conffile = "~/myintegrit.conf";

		savefile=System.getProperty("daemon.savefile");
		//savefile="C:/Fraps/myIsave.data";
		if(savefile==null)
			savefile = "~myIsave.data";

		validfile=System.getProperty("daemon.validfile");
		//validfile="C:/Fraps/myIvalid.data";
		if(validfile==null)
			validfile = "~/myIvalid.data";

		String interactive=System.getProperty("daemon.interactive");

		int interi = 1;
		try {
		interi = Integer.parseInt(interactive);}
		catch (NumberFormatException nfe){
			;
		}
		inter = interi!=0;


		File f = new File(pidfile);f.deleteOnExit();



		System.out.println("Config     = " + conffile);
		System.out.println("Pid File   = " + pidfile);
		System.out.println("Valid File = " + validfile);
		System.out.println("Save File  = " + savefile);
	}

	public static void pauseResume(){

		if(pauseResume==true) {
			for (Thread t : pathThread.values())
				if (t.isAlive())
					t.interrupt();
			pathThread.clear();
			System.out.println("{SYSTEM] Paused");
			while(pauseResume) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}else
		{
			System.out.println("[SYSTEM] Resumed");
		}
	}


	public static void reloadConf(){

		System.out.println("[SYSTEM] Config Reloaded");
		for(Thread t:pathThread.values())
			if(t.isAlive())
				t.interrupt();

		loadConf();
	}

	public static void loadConf(){
		File c = new File(conffile);

		InputStream fis = null;

		try {
			fis = new FileInputStream(c);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			String line;
			Pattern p = Pattern.compile("(\\d+) (.+)");
			while ((line = br.readLine()) != null) {
				// For every path
				//path = line;

				int ms = -1;
				File fi = null;
				String path = "",msecs="";
				Matcher m = p.matcher(line);
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
					fi = new File(path);}

				if(fi!=null && ms > 0)
				{PathInfo pi = new PathInfo(path,new HashMap<>(),ms*1000);
					validPathsInfo.put(path, pi);
					System.out.println("[PATH] ADDED   -> "+ msecs +"s "+ path);}
				else
					System.out.println("[PATH] INVALID -> "+ line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
