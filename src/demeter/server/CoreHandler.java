package demeter.server;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class CoreHandler extends AbstractHandler{
	private static String _body;
	private static Compositer compositer = null;
	 
	public CoreHandler() {
		_body = null;
	}
	 
	 
	public CoreHandler(String username) {
		compositer = new Compositer(username);
		_body = null;
	}
	 
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		if(request.getParameter("tweetID") != null) {
			_body = compositer.buildPage(request.getParameter("tweetID"));
		} else {
			_body = compositer.buildPage();
		}
		if (_body != null) response.getWriter().println(_body);
	}
}
