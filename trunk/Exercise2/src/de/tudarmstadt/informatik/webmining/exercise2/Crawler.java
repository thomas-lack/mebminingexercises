package de.tudarmstadt.informatik.webmining.exercise2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

public class Crawler implements Runnable{
	private static class CrawledUri{
		private URI mUri = null;
		private int mContainingUris = 0;
		private int mUnvisitedUris = 0;
		private int mOccurrence = 0;
		private Language mLanguage = Language.UNKNOWN;
		
		public CrawledUri(URI uri) {
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
	}
	
	private HttpClient mHttpClient = null;
	private LinkedList<URI> mUriQueue = null;
	private boolean mUseDepthFirst = false;
	private HashMap<URI, CrawledUri> mCrawledUris = null;
	private boolean isDone = false;
	
	public Crawler(boolean useDepthFirst) {
		this.mUseDepthFirst = useDepthFirst;
		
		mHttpClient = new DefaultHttpClient();
		mCrawledUris = new HashMap<URI, Crawler.CrawledUri>();
		mUriQueue = new LinkedList<URI>();
		
		try {
			mUriQueue.add(new URI("http://de.wikipedia.org/wiki/Bensheim"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		int count = 0;
		while (!isDone) {
			System.out.println((count++) + " Queue size: " + mUriQueue.size() + " crawled: " + mCrawledUris.size());
			if(0 < mUriQueue.size()){
				URI uri = mUriQueue.pop();
				
				HttpGet get = new HttpGet(uri);
				HttpResponse response = null;
				try {
					response = mHttpClient.execute(get);
				} catch (Throwable t) {
					System.out.println("skip " + uri + " msg:" + t.getMessage());
					continue;
				}
								
				HttpEntity entity = response.getEntity();
				if(null != response && 
						200 == response.getStatusLine().getStatusCode() && 
						entity.getContentType().getValue().contains("text/html")){
					
					CrawledUri newUri = new CrawledUri(uri);
					newUri.setOccurrence(1);
					try {
						String html = readStreamToString(entity.getContent());
						newUri.setLanguage(getLanguageOfHTML(html));
						List<URI> links = parseLinks(new StringReader(html));
						
						newUri.setContainingUris(links.size());
						int unvisitedUris = 0;
						
						for (URI link : links) {
							if(link.isAbsolute()){
								if(!mCrawledUris.containsKey(link)){
									unvisitedUris++;
								
									if(mUseDepthFirst)
										mUriQueue.addFirst(link);
									else
										mUriQueue.addLast(link);
								}
								else{
									CrawledUri cu = mCrawledUris.get(link);
									cu.setOccurrence(cu.getOccurrence() + 1);
								}
							}
						}
							
						newUri.setUnvistitedUris(unvisitedUris);
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("failed to read " + uri);
					}
						
					mCrawledUris.put(newUri.getUri(), newUri);
				}
				else{
					try {
						entity.getContent().close();
					} catch (Throwable t) {
						// ignore
					}
					continue;
				}
			}
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
			
			if (200 == mCrawledUris.size()) {
				System.out.println("done");
				
				for (CrawledUri crawled : mCrawledUris.values()) {
					System.out.println(crawled);
				}
		
				return;
			}
		}
	}
	
	public Language getLanguageOfHTML(String html){
		int[] counts = new int[Language.values().length];  
		html.replaceAll("[.,!?'\"\\/|()]", "");
		String[] words = html.split("[ \t\n\f\r]");
		
		// TODO
		
		return Language.ENGLISH;
	}
	
	public boolean isAvailableAndHTML(URI uri){
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
	
	private List<URI> parseLinks(Reader reader) throws IOException{
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
		
		delegator.parse(reader, callback, true);
		return result;
	}
	
	private void createCVS(File file){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			String LF = System.getProperty("line.separator");
			
			for (CrawledUri cu : mCrawledUris.values()) {
				writer.write("\"" + cu.getUri() + "\"," + 
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
}
