package com.youtube.funfactor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileParser {
	
	public static Set<String> getUniqueIds(String filePath) {		
		try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
			Set<String> ids = lines
				.map(line -> line.split(","))
				.flatMap(Arrays::stream)
				.filter(id -> id.matches("[^,]{11}"))
				.distinct()
				.collect(Collectors.toSet());
			
			SortedSet<String> sortedIds = new TreeSet<String>();
			sortedIds.addAll(ids);
			return sortedIds;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
