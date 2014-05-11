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
	private static String indexPage = "/var/www/demeter/index.html";
	private static String currentUser = "";
	private static File index;
	private static Crawler crawler;
	private static HashMap<String, Status> currentTimeline;

	/**
	 * Initialize timeline and load index page
	 * @param username twitter username
	 */
	public Compositer(){
		crawler = new Crawler();
		index = new File(indexPage);
		currentTimeline = new HashMap<String, Status>();
		crawler.stream(new String[]{"@FAZ_NET","@tagesschau","@dpa","@SZ", "@SPIEGELONLINE"},
				new long[]{114508061,18016521,5734902,40227292,2834511});
	}
	
	/**
	 * Creates timeline view
	 * @return mainpage html string
	 * @throws IOException
	 */
	public String buildPage() throws IOException {
		Document doc = Jsoup.parse(index, "UTF-8");
		Element mainView = doc.body().appendElement("div").addClass("row");
		Element timelineView = mainView.appendElement("div")
				.addClass("col-md-4") 
				.appendElement("div")
				.addClass("panel panel-default");
		buildHeading(timelineView);
		return doc.toString();
	}
	
	public String buildPage(String username) throws IOException {
		currentUser = username;
		Document doc = Jsoup.parse(index, "UTF-8");
		Element mainView = doc.body().appendElement("div").addClass("row");
		Element timelineView = mainView.appendElement("div")
				.addClass("col-md-4") 
				.appendElement("div")
				.addClass("panel panel-default");
		buildHeading(timelineView);
		buildTimeline(timelineView);
		return doc.toString();
	}
	
	/**
	 * Creates and combines timeline and detail to overall view
	 * @param tweetID
	 * @return
	 * @throws IOException
	 */
	public String buildPage(String username,String tweetID) throws IOException {
		currentUser = username;
		Document doc = Jsoup.parse(index, "UTF-8");
		Element mainView = doc.body().appendElement("div").addClass("row");
		Element timelineView = mainView.appendElement("div")
				.addClass("col-md-4") 
				.appendElement("div")
				.addClass("panel panel-default");
		buildHeading(timelineView);
		buildTimeline(timelineView);
		buildDetail(mainView, tweetID);
		return doc.toString();
	}
	
	/**
	 * Creates the heading of the timeline
	 * @param timelineView timeline html element
	 */
	private void buildHeading(Element timelineView) {
		// create header and selection menu for twitter accounts
		Element heading = timelineView.appendElement("div")
		.addClass("panel-heading")
		.appendElement("h3")
		.text("Timeline: @" + currentUser);
		
		heading
		.appendElement("form")
		.attr("action", "/")
		.appendElement("select")
		.addClass("form-control")
		.attr("name", "username")
		.appendElement("option")
		.text("FAZ_NET")
		.val("FAZ_NET")
		.appendElement("option")
		.text("dpa")
		.val("dpa")
		.appendElement("option")
		.text("tagesschau")
		.val("tagesschau")
		.appendElement("option")
		.text("SZ")
		.val("SZ")
		.appendElement("option")
		.text("SPIEGELONLINE")
		.val("SPIEGELONLINE")
		.appendElement("option")
		.text("tazgezwitscher")
		.val("tazgezwitscher")
		.appendElement("input")
		.addClass("btn btn-primary")
		.attr("type", "submit") ;
	}
	
	/**
	 * Creates the timeline view front page
	 * @return
	 * @throws IOException
	 */
	private void buildTimeline(Element timelineView){
		List<Status> timeline = crawler.fetchUserTimeline(currentUser);
		
		for(Status s: timeline) {
			if(!currentTimeline.containsKey(Long.toString(s.getId())))
				currentTimeline.put(Long.toString(s.getId()), s);
		}
	
		
		Element tweetList = timelineView
				.appendElement("div")
				.addClass("panel-body")
				.appendElement("table")
				.addClass("table table-striped timeline-body") ;
		
		for(Status status: timeline) {
			Element tableRow = tweetList.appendElement("tr");
			tableRow
			.appendElement("form")
			.attr("action", "/")
			.appendElement("input")
			.attr("value", status.getUser().getScreenName())
			.attr("name", "username")
			.attr("type", "hidden")
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
	 * Extracts the text before the url
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
	 * Builds the detail view of a given tweet
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
		
		detailList.appendElement("dt").text("time");
		detailList.appendElement("dl").text(currentStatus.getCreatedAt().toString());
		
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
		
		detailList.appendElement("dt").text("found statuses for " + extractText(currentStatus.getText()));
		detailList.appendElement("dl").text(Integer.toString(statuses.size()));
		
		//Search for hashtags
		String hashtagQuery = "";
		
		for(HashtagEntity hashtag : currentStatus.getHashtagEntities()) {
			hashtagQuery = hashtagQuery + "#" + hashtag.getText() + " ";
		}
		
		if(!hashtagQuery.equals("")) {
			List<Status> hashtagStatuses = crawler.search(hashtagQuery);
			detailList.appendElement("dt").text("found statuses for " + hashtagQuery);
			detailList.appendElement("dl").text(Integer.toString(hashtagStatuses.size()));
		}
	}
}