package daemonTh;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Main {

	public static boolean remainsValid;
	static HashMap<String,byte[]> validFilesHash = null;
	static protected boolean shutdownRequested = false;
	static Worker wk =null;
	static String path="";
	static public void shutdown()
	{
		shutdownRequested = true;

		try
		{
			wk.wait();
		}
		catch(InterruptedException e)
		{
			System.out.println("Interrupted which waiting on main daemon thread to complete.");
		}
	}

	static public boolean isShutdownRequested()
	{
		return shutdownRequested;
	}

	public static void main(String[] args) {

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[System] Stoping");
			}
		});
/*
		Signal.handle(new Signal("HUP"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[System] Reloading configuration files");
			}
		});*/
/*
		try
		{
			// do sanity checks and startup actions
			daemonize();
		}
		catch (Throwable e)
		{
			System.err.println("Startup failed.");
			e.printStackTrace();
		}*/

		System.out.println("[myIntegrit] Started");

		daemonize();

		while(!isShutdownRequested())
		{
			try {
				Thread.sleep(2000);

				Worker wk = new Worker(validFilesHash, path);
				Thread t1 = new Thread(wk,"t1");
				t1.start();

					try{
						System.out.println("Waiting for thread to complete...");
						t1.join();
					}catch(InterruptedException e){
						e.printStackTrace();
					}

					System.out.println("DONE, State:"+remainsValid);



				if (!remainsValid) {
					System.out.println("[System] Stoping");
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.print('.');
		}


	}

	static protected void addDaemonShutdownHook() {
		Runtime.getRuntime().addShutdownHook( new Thread() { public void run() { Main.shutdown(); }});
	}

	static public void daemonize(){

		String pidfile=System.getProperty("daemon.pidfile");
		//String pidfile="C:/Fraps/pidfile.txt";
		String conffile=System.getProperty("daemon.conf");
		//String conffile="C:/Fraps/myintegrit.conf";
		File f = new File(pidfile);f.deleteOnExit();
		File c = new File(conffile);

		InputStream fis = null;

		try {
			fis = new FileInputStream(c);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				path = line;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		System.out.println("Config="+conffile);
		System.out.println("Config path="+path);
		System.out.println("Pid file=" + pidfile);


		//System.out.close();
		//System.err.close();
	}
}
