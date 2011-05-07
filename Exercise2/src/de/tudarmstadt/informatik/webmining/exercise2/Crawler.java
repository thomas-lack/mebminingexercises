package de.tudarmstadt.informatik.webmining.exercise2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.HashMap;
import java.util.HashSet;
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
		private Language mLanguage = Language.UNKNOWN;
		
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
	}
	
	private HttpClient mHttpClient = null;
	private LinkedList<URI> mToCrawlQueue = null;
	private HashSet<URI> mVistitedURIs = null;
	private HashMap<URI, CrawledURI> mCrawledURIs = null;
	
	private int mMaxCrawlingCount = 200;
	private boolean isDepthFirst = false;
	private boolean isDone = false;
	
	public Crawler(boolean useDepthFirst) {
		this.isDepthFirst = useDepthFirst;
		
		mHttpClient = new DefaultHttpClient();
		mToCrawlQueue = new LinkedList<URI>();
		mVistitedURIs = new HashSet<URI>();
		mCrawledURIs = new HashMap<URI, Crawler.CrawledURI>();
		
		try {
			mToCrawlQueue.add(new URI("http://de.wikipedia.org/wiki/Bensheim"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("start " + System.currentTimeMillis());
		while (!isDone && mMaxCrawlingCount > mCrawledURIs.size() && 0 < mToCrawlQueue.size()) {
			URI currentUri = mToCrawlQueue.pop();
			mVistitedURIs.add(currentUri);
			HttpResponse response = null;
			
			if(!"http".equalsIgnoreCase(currentUri.getScheme()))
				continue;
			
			System.out.println(mCrawledURIs.size() + " " + currentUri);
			
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
						List<URI> containingLinks = parseLinks(new StringReader(html));
						crawledURI.setLanguage(getLanguageOfHTML(html));
						crawledURI.setContainingUris(containingLinks.size());
						int unvisitedURIs = 0;
						
						for (URI link : containingLinks) {
							link = new URI(link.getScheme(), link.getHost(), link.getPath(), null);
							if(null != link.getHost() && "http".equalsIgnoreCase(link.getScheme()) && isOnlineAndHTML(link)){
								System.out.println("\t" + link);
								if(mCrawledURIs.containsKey(link)){
									CrawledURI cu = mCrawledURIs.get(link);
									cu.setOccurrence(cu.getOccurrence()+1);
								}
								else{
									if(!mVistitedURIs.contains(link)){
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
		}
		System.out.println("done " + System.currentTimeMillis());
	}
	
	public Language getLanguageOfHTML(String html){
		int[] counts = new int[Language.values().length];  
		html.replaceAll("[.,!?'\"\\/|()]", "");
		String[] words = html.split("[ \t\n\f\r]");
		
		// TODO
		
		return Language.ENGLISH;
	}
	
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
