package daemonTh;

import java.util.ArrayList;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Main {

	public static boolean remainsValid;
	static ArrayList<byte[]> validFilesHash = null;

	public static void Main(String[] args) {

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[System] Stoping");
			}
		});

		Signal.handle(new Signal("HUP"), new SignalHandler() {
			@Override
			public void handle(Signal sig) {

				System.out.println("[System] Reloading configuration files");
			}
		});

		System.out.print("working");
		for (; remainsValid;) {

			try {
				Thread.sleep(1000);

				Worker wk = new Worker(validFilesHash, args[1]);
				wk.run();
				wk.wait();

				if (!remainsValid) {
					System.out.println("[System] Stoping");
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.print('.');
		}
	}
}
