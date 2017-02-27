package com.youtube.funfactor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.api.client.util.Charsets;
import com.jayway.jsonpath.JsonPath;

public class Youtube {

	private static final String key = "AIzaSyCs1sjLcUShqUE6d6lBv9H9uk58CUCbHQU";

	private static long parseDuration(String duration) {
		return Duration.parse(duration).get(java.time.temporal.ChronoUnit.SECONDS);
	}

	public static String getComments(String id) {

		String thread = ""
				+ "https://www.googleapis.com/youtube/v3/commentThreads?"
				+ "key=" + key
				+ "&part=snippet&videoId=W9y6nwBwwyQ&maxResults=100";
		
		String url = "https://www.googleapis.com/youtube/v3/comments?key=" + key + 
				"videoId=" + id + "&maxResults=100&textFormat=plainText&part=snippet";

		String resp = null;
		try {
			resp = IOUtils.toString(new URL(url), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resp;
	}

	public static Movie generateMovie(String id, boolean isTest) {

		String url = "https://www.googleapis.com/youtube/v3/videos?key=" + key +
				"&fields=items(snippet(title,description,tags,categoryId,channelId),"
				+ "statistics,contentDetails(duration))"
				+ "&part=snippet,statistics,contentDetails&id=" + id;

		try {
			String movie = IOUtils.toString(new URL(url), Charsets.UTF_8);

			List<String> items = JsonPath.read(movie, "$.items");
			if(items.isEmpty()) {
				return null;
			}

			String title = JsonPath.read(movie, "$.items[0].snippet.title");
			String description = JsonPath.read(movie, "$.items[0].snippet.description");

			List<String> tagNames = JsonPath.read(movie, "$.items[0].snippet.tags");
			Set<Tag> tags = new HashSet<Tag>();
			for(String tagName: tagNames) {
				if(tagName != null && !tagName.isEmpty()) {
					tags.add(Tag.builder().name(tagName).build());
				}
			}

			String channelId = JsonPath.read(movie, "$.items[0].snippet.channelId");
			int category = Integer.valueOf(
					JsonPath.read(movie, "$.items[0].snippet.categoryId"));

			String durationString = JsonPath.read(movie, "$.items[0].contentDetails.duration");
			long duration = parseDuration(durationString);

			long viewCount = Long.valueOf(JsonPath.read(movie, "$.items[0].statistics.viewCount"));
			int likeCount = Integer.valueOf(JsonPath.read(movie, "$.items[0].statistics.likeCount"));
			int dislikeCount = Integer.valueOf(JsonPath.read(movie, "$.items[0].statistics.dislikeCount"));
			int favoriteCount = Integer.valueOf(JsonPath.read(movie, "$.items[0].statistics.favoriteCount"));
			int commentCount = Integer.valueOf(JsonPath.read(movie, "$.items[0].statistics.commentCount"));

			return Movie.builder()
					.youtubeId(id)
					.title(title)
					.description(description)
					.tags(tags)
					.channelId(channelId)
					.category(category)
					.duration(duration)
					.viewCount(viewCount)
					.likeCount(likeCount)
					.dislikeCount(dislikeCount)
					.favoriteCount(favoriteCount)
					.commentCount(commentCount)
					.isTest(isTest)
					.build();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
