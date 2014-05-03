package demeter.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import demeter.worker.Crawler;

public class Compositer {
	private static String indexPage = "resources/public/index.html";
	private static JSONArray userTimeline = new JSONArray();
	private static String currentUser = "";
	private static File index;

	/**
	 * Initialize timeline and load index page
	 * @param username
	 */
	public Compositer(String username){
		try {
			userTimeline = Crawler.fetchTimelineTweet(username);
			currentUser = username;
			index = new File(indexPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create timeline view
	 * @return
	 * @throws IOException
	 */
	public String buildPage() throws IOException {
		Document doc = Jsoup.parse(index, "UTF-8");
		Element mainView = doc.body().appendElement("div").addClass("row");
		buildTimeline(mainView);
		return doc.toString();
	}
	
	/**
	 * Create and combine timeline and detail to overall view
	 * @param tweetID
	 * @return
	 * @throws IOException
	 */
	public String buildPage(String tweetID) throws IOException {
		Document doc = Jsoup.parse(index, "UTF-8");
		Element mainView = doc.body().appendElement("div").addClass("row");
		buildTimeline(mainView);
		buildDetail(mainView, tweetID);
		return doc.toString();
	}
	
	/**
	 * Creates the timeline view front page
	 * @return
	 * @throws IOException
	 */
	private void buildTimeline(Element mainView){
		Element timelineView = mainView.appendElement("div")
				.addClass("col-md-4") 
				.appendElement("div")
				.addClass("panel panel-default");
		
		timelineView.appendElement("div")
		.addClass("panel-heading")
		.appendElement("h3")
		.text("Timeline: @" + currentUser);
		
		Element tweetList = timelineView
				.appendElement("div")
				.addClass("panel-body")
				.appendElement("table")
				.addClass("table table-striped");
		
		for(Object t: userTimeline) {
			Element tableRow = tweetList.appendElement("tr");
			String tweet = ((JSONObject)t).get("text").toString();
			String tweetID = ((JSONObject)t).get("id_str").toString();
			tableRow
			.appendElement("form")
			.attr("action", "/detail")
			.appendElement("td")
			.text(tweet)
			.appendElement("td")
			.appendElement("input")
			.addClass("btn btn-defalut btn-xs")
			.attr("type", "submit")
			.attr("name", "tweetID")
			.attr("value", tweetID);
		}
	}
	
	
	private String extractTextFromTweet(JSONObject tweet) {
		Pattern p = Pattern.compile("([\\w\\W\\d\\D\\s]+)(?=http://)");
		Matcher m = p.matcher(tweet.get("text").toString());
		String result = "";
		while(m.find()) {
			result = m.group(0);
		}
		return result;
	}

	/**
	 * Build the detail view of a given tweet
	 * @param mainView
	 * @param tweetID
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	private void buildDetail(Element mainView, String tweetID) throws NumberFormatException, IOException {
		JSONObject tweet = Crawler.fetchTweet(Long.decode(tweetID));
		ArrayList<String> relevantKeys = new ArrayList<String>();
		relevantKeys.add("favorited");
		relevantKeys.add("retweet_count");
		relevantKeys.add("favorite_count");
		
		String tweetText = extractTextFromTweet(tweet);
		JSONObject twitterSearch = Crawler.searchTwitter(tweetText);
		
		Element detailView = mainView.appendElement("div")
			.addClass("col-md-8")
			.appendElement("div")
			.addClass("panel panel-default");
		
		detailView.appendElement("div")
		.addClass("panel-heading")
		.appendElement("h3")
		.text("Detail of " + tweetID);
		
		Element detailList = detailView
				.appendElement("div")
				.addClass("panel-body")
				.appendElement("dl");
		
		detailList.appendElement("dt").text("text");
		detailList.appendElement("dl").text(tweetText);
		
		detailList.appendElement("dt").text("hashtags");
		Element hashtags = detailList.appendElement("dl");
		
		for(Object t: (JSONArray)((JSONObject)tweet.get("entities")).get("hashtags")) {
			JSONObject tag = (JSONObject)t;
			hashtags.appendText(tag.get("text").toString() + " ");
		}
		
		
		detailList.appendElement("dt").text("url");
		Element urls = detailList.appendElement("dl");
		
		for(Object u: (JSONArray)((JSONObject)tweet.get("entities")).get("urls")) {
			String url = ((JSONObject)u).get("url").toString();
			urls.appendElement("a").attr("href",url).text(url);
		}
		
		for(String key: relevantKeys) {
			detailList.appendElement("dt").text(key);
			detailList.appendElement("dl").text(tweet.get(key).toString());
		}
		
		detailList.appendElement("dt").text("found statuses");
		detailList.appendElement("dl").text((Integer.toString(((JSONArray)twitterSearch.get("statuses")).size())));
	}
}