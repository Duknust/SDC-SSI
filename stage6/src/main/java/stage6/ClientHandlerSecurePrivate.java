package stage6;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class ClientHandlerSecurePrivate extends ClientHandlerSecure implements
		HttpHandler {
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = "This is the response in private";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();

	}
}
