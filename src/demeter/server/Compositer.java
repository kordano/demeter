package demeter.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import demeter.worker.Crawler;

public class Compositer {
	private static String indexPage = "resources/public/index.html";
	private static String currentUser = "";
	private static File index;
	private static Crawler crawler;
	private static HashMap<String, Status> currentTimeline;

	/**
	 * Initialize timeline and load index page
	 * @param username
	 */
	public Compositer(String username){
		crawler = new Crawler();
		currentUser = username;
		index = new File(indexPage);
		currentTimeline = new HashMap<String, Status>();
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
		List<Status> timeline = crawler.fetchUserTimeline(currentUser);
		for(Status s: timeline) {
			if(!currentTimeline.containsKey(Long.toString(s.getId())))
				currentTimeline.put(Long.toString(s.getId()), s);
		}
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
				.addClass("table table-striped timeline-body") ;
		
		for(Status status: timeline) {
			Element tableRow = tweetList.appendElement("tr");
			tableRow
			.appendElement("form")
			.attr("action", "/detail")
			.appendElement("td")
			.text(status.getText())
			.appendElement("td")
			.appendElement("input")
			.addClass("btn btn-defalut btn-xs")
			.attr("type", "submit")
			.attr("name", "tweetID")
			.attr("value", Long.toString(status.getId()));
		}
	}
	
	/**
	 * Extract the text before the url
	 * @param tweet
	 * @return
	 */
	private String extractText(String tweet) {
		Pattern p = Pattern.compile("([\\w\\W\\d\\D\\s]+)(?=http://)");
		Matcher m = p.matcher(tweet);
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
		Status currentStatus = currentTimeline.get(tweetID);
		List<Status> statuses = crawler.search(extractText(currentStatus.getText()));
		
		Element detailView = mainView.appendElement("div")
			.addClass("col-md-7")
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
		detailList.appendElement("dl").text(currentStatus.getText());
		
		detailList.appendElement("dt").text("hashtags");
		Element hashtags = detailList.appendElement("dl");
		
		for(HashtagEntity hashtag: currentStatus.getHashtagEntities()) {
			hashtags.appendText("#" + hashtag.getText() + " ");
		}
		
		detailList.appendElement("dt").text("url");
		Element urls = detailList.appendElement("dl");
		
		for(URLEntity url : currentStatus.getURLEntities()) {
			urls.appendElement("a").attr("href",url.getURL()).text(url.getExpandedURL());
		}
		
		detailList.appendElement("dt").text("user mentions");
		Element userMentions = detailList.appendElement("dl");
		
		for(UserMentionEntity userMention: currentStatus.getUserMentionEntities()) {
			userMentions.appendText("@" + userMention.getText() + " ");
		}
		
		detailList.appendElement("dt").text("retweet count");
		detailList.appendElement("dl").text(Integer.toString(currentStatus.getRetweetCount()));
		
		detailList.appendElement("dt").text("favorite count");
		detailList.appendElement("dl").text(Integer.toString(currentStatus.getFavoriteCount()));
		
		detailList.appendElement("dt").text("found statuses");
		detailList.appendElement("dl").text(Integer.toString(statuses.size()));
	}
}