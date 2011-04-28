package de.tudarmstadt.informatik.webmining.exercise1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TextUtils {
	public static final String WORD_SEPERATORS = " ,.?!:;\"'\\/[](){}&%$=+*~#<>|-\n\t\r";
	public static final String LF = System.getProperty("line.separator");
	
	public static List<String> splitWords(String text){
		List<String> result = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(text, WORD_SEPERATORS);
		
		while (tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();
			
			if(!token.isEmpty())
				result.add(token);
		}
		
		return result;
	}
	
	public static List<String> splitAndFilterWords(String text, List<String> stopWords){
		List<String> result = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(text, WORD_SEPERATORS);
		
		while (tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();
			
			if(!token.isEmpty() && !stopWords.contains(token))
				result.add(token);
		}
		
		return result;
	}
}
