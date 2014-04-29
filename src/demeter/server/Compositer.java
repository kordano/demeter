package demeter.server;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Compositer {
	private static String indexPage = "resources/public/index.html";

	public Compositer(){
	}
	
	/**
	 * Creates the standard front page
	 * @return
	 * @throws IOException
	 */
	public String buildIndexPage() throws IOException {
		File index = new File(indexPage);
		Document doc = Jsoup.parse(index, "UTF-8");
		Element mainContainer = doc.body().appendElement("div").addClass("container");
		Element panel = mainContainer.appendElement("div").addClass("jumbotron");
		panel.appendElement("h1").text("DEMETER");
		return doc.toString();
	}
	
}
