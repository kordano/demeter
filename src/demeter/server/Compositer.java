package demeter.server;

import java.io.File;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import demeter.worker.Crawler;

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
		Element mainContainer = doc.body().appendElement("div").addClass("col-md-10 col-md-offset-1");
		Element tweetList = mainContainer.appendElement("table").addClass("table table-striped");
		JSONArray timeline = Crawler.fetchTimelineTweet("FAZ_Topnews");
		for(Object t: timeline) {
			Element tableRow = tweetList.appendElement("tr");
			String tweet = ((JSONObject)t).get("text").toString();
			tableRow.appendElement("td").text(((JSONObject)((JSONObject)t).get("user")).get("name").toString());
			tableRow.appendElement("td").text(tweet);
			tableRow.appendElement("td").text(((JSONObject)t).get("retweet_count").toString());
		}
		return doc.toString();
	}
	
}
