package stage6;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

@SuppressWarnings("restriction")
public class Server {
	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		try {
			HttpsServer server = HttpsServer.create(
					new InetSocketAddress(8000), 0);

			HttpContext secureCtx = server.createContext("/secure",
					new ClientHandlerSecure());
			HttpContext privateCtx = server.createContext("/secure/private",
					new ClientHandlerSecurePrivate());

			SSLParameters params = null;

			SSLContext sslCtx = SSLContext.getDefault();
			params = sslCtx.getDefaultSSLParameters();
			params.setNeedClientAuth(true);

			HttpsConfigurator configurator = new HttpsConfigurator(sslCtx) {
				@Override
				public void configure(HttpsParameters params) {
					SSLContext context;
					SSLParameters sslparams;

					context = getSSLContext();
					context.getServerSessionContext();
					sslparams = context.getDefaultSSLParameters();
					sslparams.setNeedClientAuth(true);
					params.setSSLParameters(sslparams);
				}
			};

			server.setHttpsConfigurator(configurator);
			server.setExecutor(null);
			server.start();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
