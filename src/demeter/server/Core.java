package demeter.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class Core {
	public static void main(String[] args) throws Exception {
        Server server = new Server(8083);
        ContextHandler context = new ContextHandler("/");
        context.setContextPath("/");
        context.setHandler(new CoreHandler("Root Hello",new Compositer().buildIndexPage()));
        ContextHandler contextFR = new ContextHandler("/fr");
        contextFR.setHandler(new CoreHandler("Bonjoir"));
        ContextHandler contextIT = new ContextHandler("/it");
        contextIT.setHandler(new CoreHandler("Bongiorno"));
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context, contextFR, contextIT});
        server.setHandler(contexts);
        server.start();
        server.join(); 
    }
}
