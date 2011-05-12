package webminig.webcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class LanguageDetector {
	public static enum Language {
		DANISH,
		DUTCH,
		ENGLISH,
		FINNISH,
		FRENCH,
		GERMAN,
		HUNGARIAN,
		ITALIAN,
		NORWEGIAN,
		PORTOGUESE,
		RUSSIAN,
		SPAINISH,
		SWEDISH,
		TURKISH,
	}
	
	private ArrayList<HashSet<String>> mStopWordsList = new ArrayList<HashSet<String>>();
	
	
	public LanguageDetector() {
		initStopWordsList();
	}
	
	/**
	 * Init the stopwords list for language detection
	 */
	private void initStopWordsList(){
		mStopWordsList.add(Language.DANISH.ordinal(), readStopWordsFile(new File("stopwords/danish")));
		mStopWordsList.add(Language.DUTCH.ordinal(), readStopWordsFile(new File("stopwords/dutch")));
		mStopWordsList.add(Language.ENGLISH.ordinal(), readStopWordsFile(new File("stopwords/english")));
		mStopWordsList.add(Language.FINNISH.ordinal(), readStopWordsFile(new File("stopwords/finnish")));
		mStopWordsList.add(Language.FRENCH.ordinal(), readStopWordsFile(new File("stopwords/french")));
		mStopWordsList.add(Language.GERMAN.ordinal(), readStopWordsFile(new File("stopwords/german")));
		mStopWordsList.add(Language.HUNGARIAN.ordinal(), readStopWordsFile(new File("stopwords/hungarian")));
		mStopWordsList.add(Language.ITALIAN.ordinal(), readStopWordsFile(new File("stopwords/italian")));
		mStopWordsList.add(Language.NORWEGIAN.ordinal(), readStopWordsFile(new File("stopwords/norwegian")));
		mStopWordsList.add(Language.PORTOGUESE.ordinal(), readStopWordsFile(new File("stopwords/portuguese")));
		mStopWordsList.add(Language.RUSSIAN.ordinal(), readStopWordsFile(new File("stopwords/russian")));
		mStopWordsList.add(Language.SPAINISH.ordinal(), readStopWordsFile(new File("stopwords/spanish")));
		mStopWordsList.add(Language.SWEDISH.ordinal(), readStopWordsFile(new File("stopwords/swedish")));
		mStopWordsList.add(Language.TURKISH.ordinal(), readStopWordsFile(new File("stopwords/turkish")));
	}
	 
	/**
	 * Reads a stopword file
	 * 
	 * @param file
	 * @return
	 */
	private HashSet<String> readStopWordsFile(File file){
		HashSet<String> result = new HashSet<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			
			while (null != (line = br.readLine()))
				if(!line.isEmpty())
					result.add(line);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
