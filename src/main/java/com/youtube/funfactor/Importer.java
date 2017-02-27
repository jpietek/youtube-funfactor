package com.youtube.funfactor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.api.client.util.Charsets;
import com.google.common.collect.Sets;
import com.youtube.funfactor.dao.MovieDao;

public class Importer {

	private static Logger logger = Logger.getLogger(Importer.class);
	private static final String trainfilePath = "comp-updated2";
	private static final String testfilePath = "comedy_comparisons.test";

	private static MovieDao movieDao = new MovieDao();
	public Importer() {

	}

	private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

		// 1. Convert Map to List of Map
		List<Map.Entry<String, Integer>> list =
				new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// 2. Sort list with Collections.sort(), provide a custom Comparator
		//    Try switch the o1 o2 position for a different order
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static void fetch(String filePath, boolean isTest) {
		Set<String> ids = FileParser.getUniqueIds(filePath);
		logger.info("ids size: " + ids.size());

		int i = 0;


		for(String id: ids) {		
			logger.info("processing id: " + i);
			Movie movie = Youtube.generateMovie(id, isTest);
			if(movie == null) {
				logger.info("empty item");
				continue;
			}
			logger.info(movie.toString());

			try {
				movieDao.open();
				movieDao.save(movie);
			} catch(Exception e) {
				logger.error("can't save " + e.getMessage());
			} finally {
				movieDao.close();
			}
			i++;
		}


	}

	private static void incScore(Map<String, Integer> map, Set<String> keys) {
		for(String key: keys) {
			Integer score = map.get(key);
			if(score == null) {
				map.put(key, 0);
			} else {
				map.put(key, ++score);
			}
		}
	}

	private static void decScore(Map<String, Integer> map, Set<String> keys) {
		for(String key: keys) {
			Integer score = map.get(key);
			if(score == null) {
				map.put(key, -1);
			} else {
				map.put(key, --score);
			}
		}
	}

	public static void scoreTags(String filePath) throws IOException {
		Stream<String> lines = Files.lines(Paths.get(filePath));

		Iterator<String> it = lines.iterator();
		int i = 0;

		Map<String, Integer> scoredTags = new HashMap<String, Integer>();

		Set<String> ids = FileParser.getUniqueIds(filePath);

		Map<String, Set<String>> movieTags = new HashMap<String, Set<String>>();

		movieDao.open();

		for(String id: ids) {
			Set<String> tags = movieDao.getTags(id);
			movieTags.put(id, tags);
		}

		movieDao.close();

		while(it.hasNext()) {
			i++;
			logger.info("at line: " + i);
			String s = it.next();
			String id1 = s.split(",")[0];
			String id2 = s.split(",")[1];
			String side = s.split(",")[2];

			Set<String> leftTags = movieTags.get(id1);
			Set<String> rightTags = movieTags.get(id2);

			Set<String> leftUniq = Sets.difference(leftTags, rightTags);
			Set<String> rightUniq = Sets.difference(rightTags, leftTags);
			if(side.equalsIgnoreCase("left")) {
				incScore(scoredTags, leftUniq);
				decScore(scoredTags, rightUniq);
			} else if(side.equalsIgnoreCase("right")){
				incScore(scoredTags, rightUniq);
				decScore(scoredTags, leftUniq);
			}
		}

		Map<String, Integer> sortedTags = sortByValue(scoredTags);
		logger.info("tags len: " + sortedTags.size());

		movieDao.open();

		for(Entry<String, Integer> e: sortedTags.entrySet()) {
			//logger.info("tag score: " + e.getKey() + " " + e.getValue());
			movieDao.updateTagScore(e.getKey(), e.getValue());
		}
		movieDao.close();

		lines.close();
		System.exit(0);
	}

	public static void purgeMovies(String filePath, String purgedOutput)
			throws IOException {
		Stream<String> lines = Files.lines(Paths.get(filePath));
		Iterator<String> it = lines.iterator();

		movieDao.open();
		List<String> dbIds = movieDao.getIds();
		movieDao.close();
		logger.info("db ids size: " + dbIds.size());
		List<String> updatedLines = new ArrayList<String>();
		while(it.hasNext()) {
			String line = it.next();
			if(dbIds.contains(line.split(",")[0])
					&& dbIds.contains(line.split(",")[1])) {
				updatedLines.add(line);
			}
		}

		logger.info("updated lines size: " + updatedLines.size());

		Files.write(Paths.get(purgedOutput), updatedLines, Charsets.UTF_8);

		lines.close();
	}

	public static void scoreMovies(String filePath) throws IOException {
		Stream<String> lines = Files.lines(Paths.get(filePath));

		Iterator<String> it = lines.iterator();
		int i = 0;

		Map<String, Integer> movieScore = new HashMap<String, Integer>();

		while(it.hasNext()) {
			i++;
			logger.info("at line: " + i);
			String s = it.next();

			String side = s.split(",")[2];

			String id = null;
			if(side.equalsIgnoreCase("left")) {
				id = s.split(",")[0];
			} else if(side.equalsIgnoreCase("right")) {
				id = s.split(",")[1];
			}

			Integer score = movieScore.get(id);

			if(score == null) {
				movieScore.put(id, 0);
			} else {
				score++;
				movieScore.put(id, score);
			}
		}
		logger.info("size: " + movieScore.size());

		Map<String, Integer> sortedScore = sortByValue(movieScore);

		for(Entry<String, Integer> entry: sortedScore.entrySet()) {
			logger.info("movie id: " + entry.getKey() + " score: " + entry.getValue());
			movieDao.updateScore(entry.getKey(), entry.getValue());
		}

		lines.close();
	}

	public static void testMovies() throws IOException {

		Stream<String> lines = Files.lines(Paths.get("test-updated"));

		Iterator<String> it = lines.iterator();

		movieDao.open();
		while(it.hasNext()) {
			String s = it.next();
			String id1 = s.split(",")[0];
			String id2 = s.split(",")[1];
			String side = s.split(",")[2]; 

			Integer s1 = Integer.valueOf(movieDao.getTagsScore(id1).toString());
			Integer s2 = Integer.valueOf(movieDao.getTagsScore(id2).toString());

			if(s1 > s2 && side.equalsIgnoreCase("left")
					|| s1 < s2 && side.equalsIgnoreCase("right")) {
				logger.info("hit");
			} else {
				logger.info("miss");
			}
		}
	}

	public static void main(String [] args) throws IOException {
		//scoreTags();
		//movieDao.commitAndclose();
		//purgeMovies(testfilePath);

		//fetch(testfilePath, true);

		//purgeMovies(testfilePath, "test-updated");
		
		//testMovies();
		
		String comments = Youtube.getComments("W9y6nwBwwyQ");
		logger.info(comments);

		System.exit(0);
	}
}
