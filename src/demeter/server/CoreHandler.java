package demeter.server;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CoreHandler extends AbstractHandler{
	final String _greeting;
	final String _body;
	 
	public CoreHandler() {
		_greeting = "Hello World";
		_body = null;
	}
	 
	public CoreHandler(String greeting) {
		_greeting = greeting;
		_body = null;
	}
	 
	public CoreHandler(String greeting, String body) {
		_greeting = greeting;
		_body = body;
	}
	
	 
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		if (_body != null) response.getWriter().println(_body);
	}
}
