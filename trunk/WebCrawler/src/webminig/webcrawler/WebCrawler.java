package webminig.webcrawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;

import webminig.webcrawler.Downloader.IDownloadCallback;

public class WebCrawler{
	public static final int NUM_DOWNLOADER = 4;
	
	
	public static void main(String[] args) {
		WebCrawler crawler = new WebCrawler();
		crawler.run("http://www.spiegel.de");
	}
	
	private LinkedList<URI> mQueue = new LinkedList<URI>();
	private LinkedList<Pair<URI, String>> mDownloadedQueue = new LinkedList<Pair<URI,String>>();
	private HashSet<Integer> mVisited = new HashSet<Integer>();
	
	private Downloader[] mDownloader = null;
	
	private IDownloadCallback mDownloadCallback = new IDownloadCallback() {
		@Override
		public void onDownloadFinished(URI uri, String data) {
			synchronized (mDownloadedQueue) {
				mDownloadedQueue.add(new Pair<URI, String>(uri, data));
				System.out.println(uri + " done");
				
				if(!hasNext())
					stop();
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
				return mQueue.pop();
			}
		}
	};
	
	public void run(String seed){
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
		
		mDownloader = new Downloader[NUM_DOWNLOADER];
		
		for (int i = 0; i < NUM_DOWNLOADER; i++){
			mDownloader[i] = new Downloader(mDownloadCallback);
			mDownloader[i].start();
		}
	}
	
	public void stop(){
		for (int i = 0; i < NUM_DOWNLOADER; i++)
			mDownloader[i].interrupt();
	}
	
	private void addVisited(Integer hashCode) {
		synchronized (mVisited) {
			mVisited.add(hashCode);
		}
	}
	
	private boolean isAlreadyVisited(Integer hashCode){
		synchronized (mVisited) {
			return mVisited.contains(hashCode);
		}
	}
}
