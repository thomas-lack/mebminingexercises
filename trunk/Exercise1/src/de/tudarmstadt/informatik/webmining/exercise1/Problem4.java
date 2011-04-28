package de.tudarmstadt.informatik.webmining.exercise1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

public class Problem4 {
	public static class Pair implements Comparable<Pair>{
		String word;
		Integer count;
		
		public Pair(String word, int count) {
			this.word = word;
			this.count = count;
		}

		@Override
		public int compareTo(Pair o) {
			return count.compareTo(o.count);
		}
		
		@Override
		public boolean equals(Object obj) {
			return word.equals(obj);
		}
		
		@Override
		public String toString() {
			return "{Pair|" + word + ", " + count + "}";
		}
	}
	
	public static void main(String[] args) {	
		Options options = createCLOptions();
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine line = parser.parse(options, args);
			String[] addArgs = line.getArgs();
			
			if(1 == addArgs.length){
				Problem4 p4 = new Problem4();
				String file = addArgs[0];
				
				if(line.hasOption("s")){
					String stopwordsFile = line.getOptionValue("s");
					p4.run(file, stopwordsFile);
				}
				else{
					p4.run(file);
				}
			}
			else{
				printHelp();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static Options createCLOptions(){
		Options options = new Options();
		options.addOption("s","stopwords", true, "File with stopwords to filter them");	
		return options;
	}
	
	public static void printHelp(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Problem2 [Options] <FILE>", createCLOptions());
	}
	
	public void run(String file){
		String data;
		try {
			data = FileUtils.readFileToString(new File(file));
		} catch (IOException e) {
			System.err.println("failed to read " + file);
			return;
		}
		
		countCharecters(data);
		//List<String> words = TextUtils.splitWords(data);
		//List<Pair> letters = countLetters(words);
		//printPairs(letters);
	}

	public void run(String file, String stopwordsFile){
		String textData;
		String stopWordsData;
		try {
			textData = FileUtils.readFileToString(new File(file));
			stopWordsData = FileUtils.readFileToString(new File(stopwordsFile));
		} catch (IOException e) {
			System.err.println("failed to read file:" + e.getMessage());
			return;
		}
		
		List<String> stopWords = TextUtils.splitWords(stopWordsData);
		List<String> words = TextUtils.splitAndFilterWords(textData, stopWords);
	}
	
	private List<Pair> countLetters(List<String> words) {
		List<Pair> result = new ArrayList<Pair>();
		Pair testPair = new Pair("", 0);
		
		for (String word : words) {
			char[] chars = word.toCharArray();
			
			for (char c : chars) {
				testPair.word = String.valueOf(c);
				if(result.contains(testPair)){
					Pair p = result.get(result.indexOf(String.valueOf(c)));
					p.count++;
				}
				else{
					result.add(new Pair(String.valueOf(c), 1));
				}
				
			}
		}
		
		return result;
	}
	
	public void countCharecters(String data){
		HashMap<Character, Integer> result = new HashMap<Character, Integer>();
		HashMap<String, Integer> result2 = new HashMap<String, Integer>();
		int count = 0;
		Character lastChar = null;
		
		System.out.println(System.currentTimeMillis());
		for (int i = 0; i < data.length(); i++) {
			Character c = data.charAt(i);
			
			if(Character.isLetter(c)){
				c = Character.toLowerCase(c);
				count = 1;
				
				if(result.containsKey(c)){
					count = result.get(c);
					count++;
				}
				
				result.put(c, count);
				
				if(null == lastChar){
					lastChar = c;
				}
				else{
					String pair = lastChar.toString() + c;
					lastChar = c;
					count = 1;
					
					if(result2.containsKey(pair)){
						count = result2.get(pair);
						count++;
					}
					
					result2.put(pair, count);
				}
			}
			else{
				lastChar = null;
			}
		}
		System.out.println(System.currentTimeMillis());
		
		System.out.println("Single letters");
		for (Iterator itr = sortByValue(result).iterator(); itr.hasNext();) {
			Character key = (Character)itr.next();
			count = result.get(key);
			System.out.println(key + " " + count);
		}
		
		System.out.println("\nLetter Pairs");
		for (Iterator itr = sortByValue(result2).iterator(); itr.hasNext();) {
			String key = (String)itr.next();
			count = result2.get(key);
			System.out.println(key + " " + count);
		}
	}
	
	private void printPairs(List<Pair> uniqueWords) {
		for (Pair pair : uniqueWords) {
			System.out.println(pair.count + "\t" +pair.word);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List sortByValue(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {
            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }
                else if (v2 instanceof Comparable) {
                    return ((Comparable) v2).compareTo(v1);
                }
                else {
                    return 0;
                }
            }
        });
        return keys;
    }
}
