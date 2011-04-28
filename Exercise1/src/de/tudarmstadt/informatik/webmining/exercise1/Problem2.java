package de.tudarmstadt.informatik.webmining.exercise1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;


public class Problem2 {	
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
	}
	
	public static void main(String[] args) {	
		Options options = createCLOptions();
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine line = parser.parse(options, args);
			String[] addArgs = line.getArgs();
			
			if(1 == addArgs.length){
				Problem2 p2 = new Problem2();
				String file = addArgs[0];
				
				if(line.hasOption("s")){
					String stopwordsFile = line.getOptionValue("s");
					p2.run(file, stopwordsFile);
				}
				else{
					p2.run(file);
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
		
		List<String> words = TextUtils.splitWords(data);
		List<Pair> uniqueWords = countUniqueWords(words);
		printPairs(uniqueWords);
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
		List<Pair> uniqueWords = countUniqueWords(words);
		printPairs(uniqueWords);
	}

	private List<Pair> countUniqueWords(List<String> words) {
		List<Pair> result = new ArrayList<Problem2.Pair>();
		Collections.sort(words);
		
		String lastWord = words.get(0);
		int count = 0;
		
		for (String word : words) {
			if(lastWord.equals(word)){
				count++;
			}
			else{
				result.add(new Pair(lastWord, count));
				lastWord = word;
				count = 1;
			}
		}
		Collections.sort(result);
		Collections.reverse(result);
		
		return result;
	}
	
	private void printPairs(List<Pair> uniqueWords) {
		for (Pair pair : uniqueWords) {
			System.out.println(pair.count + "\t" +pair.word);
		}
	}
}
