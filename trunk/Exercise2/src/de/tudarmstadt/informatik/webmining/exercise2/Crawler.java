package de.tudarmstadt.informatik.webmining.exercise2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

public class Crawler implements Runnable{
	private static class CrawledURI{
		private URI mUri = null;
		private int mContainingUris = 0;
		private int mUnvisitedUris = 0;
		private int mOccurrence = 0;
		private Language mLanguage = null;
		
		public CrawledURI(URI uri) {
			this.mUri = uri;
		}

		public URI getUri() {
			return mUri;
		}

		public void setUri(URI uri) {
			this.mUri = uri;
		}

		public int getContainingUris() {
			return mContainingUris;
		}

		public void setContainingUris(int containingUris) {
			this.mContainingUris = containingUris;
		}

		public int getUnvisitedUris() {
			return mUnvisitedUris;
		}

		public void setUnvistitedUris(int newUris) {
			this.mUnvisitedUris = newUris;
		}

		public int getOccurrence() {
			return mOccurrence;
		}

		public void setOccurrence(int occurrence) {
			this.mOccurrence = occurrence;
		}
		
		public Language getLanguage() {
			return mLanguage;
		}
		
		public void setLanguage(Language mLanguage) {
			this.mLanguage = mLanguage;
		}
		
		@Override
		public String toString() {
			return "{CrawledUri|" + mUri + "," + mOccurrence + "," + mContainingUris + "," + mUnvisitedUris + "," + mLanguage + "}";
		}
	}

	public static void main(String[] args) {
		Crawler crawler = new Crawler(false);
		crawler.run();
		crawler.createCVS(null);
	}
	
	private HttpClient mHttpClient = null;
	private LinkedList<URI> mToCrawlQueue = null;
	private HashSet<URI> mVistitedURIs = null;
	private HashMap<URI, CrawledURI> mCrawledURIs = null;
	private ArrayList<HashSet<String>> mStopWordsList = null;
	
	private int mMaxCrawlingCount = 30;
	private boolean isDepthFirst = false;
	private boolean isDone = false;
	
	public Crawler(boolean useDepthFirst) {
		this.isDepthFirst = useDepthFirst;
		
		mHttpClient = new DefaultHttpClient();
		mToCrawlQueue = new LinkedList<URI>();
		mVistitedURIs = new HashSet<URI>();
		mCrawledURIs = new HashMap<URI, Crawler.CrawledURI>();
		mStopWordsList = new ArrayList<HashSet<String>>();
		
		initStopWordsList();
		
		try {
			mToCrawlQueue.add(new URI("http://www.google.de/?q=webmining#sclient=psy&hl=de&site=&source=hp&q=webmining&btnG=Google-Suche&aq=f&aqi=&aql=&oq=webmining&pbx=1&bav=on.2,or.r_gc.r_pw.&fp=5ce04943f314f78e"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (!isDone && mMaxCrawlingCount > mCrawledURIs.size() && 0 < mToCrawlQueue.size()) {
			System.out.println("In queue: " + mToCrawlQueue.size() + " visited: " + mVistitedURIs.size() + " crawled: " + mCrawledURIs.size());
			URI currentUri = mToCrawlQueue.pop();
			System.out.println("current: " + currentUri);
			mVistitedURIs.add(currentUri);
			HttpResponse response = null;
			
			if(!"http".equalsIgnoreCase(currentUri.getScheme()))
				continue;
			
			try {
				response = mHttpClient.execute(new HttpGet(currentUri));
			} catch (Throwable t) {
				continue;
			} 
			
			if(null != response){
				int code = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				String contentType = entity.getContentType().getValue();
			
				if(200 == code){
					CrawledURI crawledURI = new CrawledURI(currentUri);
					crawledURI.setOccurrence(1);
					
					try {
						String html = readStreamToString(entity.getContent());
						List<URI> containingLinks = parseLinks(html);
						crawledURI.setLanguage(getLanguageOfHTML(html));
						crawledURI.setContainingUris(containingLinks.size());
						int unvisitedURIs = 0;
						
						for (URI link : containingLinks) {
							link = new URI(link.getScheme(), link.getHost(), link.getPath(), null);
							if(null != link.getHost() && "http".equalsIgnoreCase(link.getScheme()) && isOnlineAndHTML(link)){
								if(mCrawledURIs.containsKey(link)){
									CrawledURI cu = mCrawledURIs.get(link);
									cu.setOccurrence(cu.getOccurrence()+1);
								}
								else{
									if(!mVistitedURIs.contains(link) && !mToCrawlQueue.contains(link)){
										System.out.println("\t" + link);
										unvisitedURIs++;
										
										if(isDepthFirst)
											mToCrawlQueue.addFirst(link);
										else
											mToCrawlQueue.addLast(link);
									}
								}
							}
						}
						
						crawledURI.setUnvistitedUris(unvisitedURIs);
						mCrawledURIs.put(crawledURI.getUri(), crawledURI);
					} catch (Throwable t) {
						System.err.println("failed to read " + currentUri + " cause: " + t.getLocalizedMessage());
						try {entity.getContent().close();} catch (Throwable t1) {/* skip */} 
						continue;
					} 
				}
				else{
					try {entity.getContent().close();} catch (Throwable t1) {/* skip */}  
				}
			}
			
			System.out.println("finish: " + currentUri);
		}
	}
	
	/**
	 * Returns the language of an html documment
	 * 
	 * @param html
	 * @return
	 */
	public Language getLanguageOfHTML(String html){  
		int[] hits = new int[mStopWordsList.size()];
		Arrays.fill(hits, 0);
		
		// get text from paragraphs an split between words
		String pText = getTextFromParagraphs(html);		
		html.replaceAll("[.,!?'\"\\/|()]", "");
		String[] words = pText.split("[ \t\n\f\r]");
		
		// calculate stopword hits
		for (String word : words) {			
			for (int i = 0; i < mStopWordsList.size(); i++) {
				if(mStopWordsList.get(i).contains(word))
					hits[i]++;
			}
		}
		
		// get the index with the highest hitcount
		int maxHitIndex = 0;
		for (int i = 0; i < hits.length; i++)
			if(hits[maxHitIndex] < hits[i])
				maxHitIndex = i;
		
		if(0 == hits[maxHitIndex])
			return null;
		
		return Language.values()[maxHitIndex];
	}
	
	/**
	 * Checks if an URI is online and contains a html documment
	 * 
	 * @param uri
	 * @return
	 */
	public boolean isOnlineAndHTML(URI uri){
		try {
			HttpResponse response = mHttpClient.execute(new HttpHead(uri));
			
			if(200 == response.getStatusLine().getStatusCode()){
				Header header = response.getFirstHeader("Content-Type");
				
				if(null != header){					
					return header.getValue().contains("text/html");				
				}
			}
		} catch (Throwable t) {
			// ignore
		}
		
		return false;
	}
	
	/**
	 * Returns a list of links from a html documment
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private List<URI> parseLinks(String html) throws IOException{
		final List<URI> result = new ArrayList<URI>();
		
		ParserDelegator delegator = new ParserDelegator();
		ParserCallback callback = new ParserCallback(){
			@Override
			public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
				if(t.equals(HTML.Tag.A)){
					try {
						String href = (String) a.getAttribute(Attribute.HREF);
						
						if(null != href){
							URI uri = new URI(href);
							if(null != uri.getHost())
								result.add(uri);
						}
						
					} catch (URISyntaxException e) {
						// ignore this link
					}
				}
			}
		};
		
		delegator.parse(new StringReader(html), callback, true);
		return result;
	}
	
	/**
	 * Create a CSV file from the crawled data
	 * @param file
	 */
	private void createCVS(File file){
		try {
			//BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			String LF = System.getProperty("line.separator");
			
			for (CrawledURI cu : mCrawledURIs.values()) {
				writer.write("\"" + cu.getUri() + "\"," + 
					"\"" + cu.getUri().getHost() + "\"," + 
					cu.getOccurrence() + "," + 
					cu.getContainingUris() + "," + 
					cu.getUnvisitedUris() + "," +
					cu.getLanguage() + 
					LF
				);
			}		
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads data from a stream an writes it to a string 
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private String readStreamToString(InputStream input) throws IOException{
		StringBuilder sb = new StringBuilder();
		String line = "";

		BufferedReader br = new BufferedReader(
			new InputStreamReader(
					input));
		
		while(null != (line = br.readLine())){
			sb.append(line);
		}
		
		br.close();
		
		return sb.toString();
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
	
	/**
	 * Gets the Text from all P-elemnts in a HTML documment
	 * 
	 * @param html
	 * @return
	 */
	private String getTextFromParagraphs(String html){
		final StringBuilder sb = new StringBuilder();
		
		ParserDelegator delegator = new ParserDelegator();
		ParserCallback callback = new ParserCallback(){
			private boolean isParserInP = false;
			
			@Override
			public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
				if(t.equals(HTML.Tag.P))
					isParserInP = true;
			}
			
			public void handleText(char[] data, int pos) {
				if(isParserInP)
					sb.append(new String(data) + " ");
			};
			
			@Override
			public void handleEndTag(Tag t, int pos) {
				if(isParserInP && t.equals(HTML.Tag.P))
					isParserInP = false;
			}
		};
		
		try {
			delegator.parse(new StringReader(html), callback, true);
		} catch (IOException e) {
			// ignore
		}
		
		return sb.toString().replaceAll("\\<.*?\\>;", "");
	}
}
