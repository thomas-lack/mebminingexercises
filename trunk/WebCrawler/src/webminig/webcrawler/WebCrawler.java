package webminig.webcrawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;

import webminig.webcrawler.Downloader.IDownloadCallback;
import webminig.webcrawler.PageParser.IPageParserCallback;

public class WebCrawler{
	public static final int NUM_DOWNLOADER = 2;
	public static final int NUM_PARSER = 2;
	public static final int MAX_CRAWLED_PAGES = 500;
	public static final long THREAD_WAIT_TIME_MILLIS = 200;
	
	
	public static void main(String[] args) {
		WebCrawler crawler = new WebCrawler();
		crawler.start("http://www.spiegel.de");
	}
	
	private LinkedList<URI> mQueue = new LinkedList<URI>();
	private LinkedList<Pair<URI, String>> mDownloadedQueue = new LinkedList<Pair<URI,String>>();
	private HashSet<Integer> mVisited = new HashSet<Integer>();
	private LinkedList<CrawledPage> mRepository = new LinkedList<CrawledPage>();
	
	private Downloader[] mDownloader = new Downloader[NUM_DOWNLOADER];
	private PageParser[] mParser = new PageParser[NUM_PARSER];
	
	long start = 0;
	
	private IDownloadCallback mDownloadCallback = new IDownloadCallback() {
		@Override
		public void onDownloadFinished(URI uri, String data) {
			synchronized (mDownloadedQueue) {
				mDownloadedQueue.add(new Pair<URI, String>(uri, data));
			}
		}
		
		@Override
		public boolean hasNext() {
			synchronized (mQueue) {
				return 0 < mQueue.size();
			}
		}
		
		@Override
		public URI getNext() {
			synchronized (mQueue) {	
				if(0 < mQueue.size())
					return mQueue.removeFirst();
				else
					return null;
			}
		}
	};
	
	private IPageParserCallback mParserCallback = new IPageParserCallback() {
		@Override
		public void onNewLink(URI link) {
			synchronized (mQueue) {
				synchronized (mVisited) {
					mVisited.add(link.hashCode());
				}
				
				// TODO Suchstrategien 
				mQueue.addLast(link);
			}
		}
		
		@Override
		public void onCrawlingPageFinished(CrawledPage crawledPage) {
			mRepository.add(crawledPage);
			System.out.println(crawledPage);
			
			if(MAX_CRAWLED_PAGES <= mRepository.size())
				stop();
		}
		
		@Override
		public boolean isAlreadyVisited(Integer hashCode) {
			synchronized (mVisited) {				
				return mVisited.contains(hashCode);
			}
		}
		
		@Override
		public boolean hasNext() {
			synchronized (mDownloadedQueue) {
				return 0 < mDownloadedQueue.size();
			}
		}
		
		@Override
		public Pair<URI, String> getNext() {
			synchronized (mDownloadedQueue) {
				if(0 < mDownloadedQueue.size())
					return mDownloadedQueue.removeFirst();
				else
					return null;
			}
		}
	};
	public void start(String seed){
		start = System.currentTimeMillis();
		try {
			mQueue.add(new URI(seed));
			mQueue.add(new URI("http://www.tagesschau.de"));
			mQueue.add(new URI("http://www.tu-darmstadt.de"));
			mQueue.add(new URI("http://de.wikipedia.org"));
			mQueue.add(new URI("http://www.p0t.de"));
			mQueue.add(new URI("http://www.heise.de"));
			mQueue.add(new URI("http://www.golem.de"));
			mQueue.add(new URI("http://www.spiegel.de"));
			mQueue.add(new URI("http://www.ebay.de"));
			mQueue.add(new URI("http://www.twitter.com"));
			mQueue.add(new URI("http://www.minecraft.net"));
			mQueue.add(new URI("http://www.facebook.com"));
			mQueue.add(new URI("http://www.wetter.com"));
			mQueue.add(new URI("http://www.openstreetmap.org"));
			mQueue.add(new URI("http://www.hessen.de"));
			mQueue.add(new URI("http://www.bensheim.de"));
		} catch (URISyntaxException e) {}
		
		for (int i = 0; i < NUM_DOWNLOADER; i++){
			mDownloader[i] = new Downloader(mDownloadCallback);
			mDownloader[i].start();
		}
		
		for (int i = 0; i < NUM_PARSER; i++) {
			mParser[i] = new PageParser(mParserCallback);
			mParser[i].start();
		}
	}
	
	public void stop(){
		for (int i = 0; i < NUM_DOWNLOADER; i++)
			mDownloader[i].setRunning(false);
	
		for(int i = 0; i < NUM_PARSER; i++)
			mParser[i].setRunning(false);
		
		System.out.println((System.currentTimeMillis() - start)/1000);
	}
}