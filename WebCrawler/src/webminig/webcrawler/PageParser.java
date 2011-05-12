package webminig.webcrawler;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class PageParser extends Thread{
	public static final int STOPPED = -1;
	public static final int IDLE = 0;
	public static final int WORKING = 1;
	
	public static interface IPageParserCallback{
		public boolean hasNext();
		public Pair<URI, String> getNext();
		public boolean isAlreadyVisited(Integer hashCode);
		public void onNewLink(URI link);
		public void onCrawlingPageFinished(CrawledPage crawledPage);
	}
	
	private IPageParserCallback mCallback = null;
	private boolean isRunning = true;
	private LanguageDetector mDetector = new LanguageDetector();
	private int mState = STOPPED;
	
	public PageParser(IPageParserCallback callback) {
		mCallback = callback;
	}
	
	public boolean isRunning() {
		synchronized (this) {
			return isRunning;
		}
	}
	
	public void setRunning(boolean isRunning) {
		synchronized (this) {			
			this.isRunning = isRunning;
		}
	}
	
	public int getPraserState() {
		synchronized (this) {
			return mState;
		}
	}
	
	public void setParserState(int mState) {
		synchronized (this) {
			this.mState = mState;
		}
	}
	
	@Override
	public void run() {
		if(null == mCallback)
			return;
		
		final ArrayList<Integer> newLinks = new ArrayList<Integer>();
		final ArrayList<Integer> oldLinks = new ArrayList<Integer>();
		final StringBuilder textBuilder = new StringBuilder();
		
		while (isRunning()) {
			if(mCallback.hasNext()){
				setParserState(WORKING);
				Pair<URI, String> dataPair = mCallback.getNext();
				
				if(null == dataPair)
					continue;
				
				textBuilder.replace(0, textBuilder.length(), "");
				newLinks.clear();
				oldLinks.clear();
				
				ParserDelegator delegator = new ParserDelegator();
				ParserCallback callback = new ParserCallback(){
					private boolean isParserInP = false;
					
					@Override
					public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
						if(t.equals(HTML.Tag.A)){
							String href = (String) a.getAttribute(Attribute.HREF);
								
							if(null != href){
								try {
									URI uri = normalizeURI(new URI(href));
									if(null != uri.getHost()){
										if(!mCallback.isAlreadyVisited(uri.hashCode())){
											newLinks.add(uri.hashCode());
											mCallback.onNewLink(uri);
										}
										else{
											oldLinks.add(uri.hashCode());
										}
									}
								} catch (URISyntaxException e) {
									return;
								}
							}
						}
						else if(t.equals(HTML.Tag.P)){
							isParserInP = true;
						}
					}
					
					@Override
					public void handleText(char[] data, int pos) {
						if(isParserInP)
							textBuilder.append(new String(data).replace("<[^>]*>", "")).append(" ");
					}
					
					@Override
					public void handleEndTag(Tag t, int pos) {
						if(t.equals(HTML.Tag.P) && isParserInP)
							isParserInP = false;
					}
				};
				
				try {
					delegator.parse(new StringReader(dataPair.getSecond()), callback, true);
					
					CrawledPage page = new CrawledPage(dataPair.getFirst());
					page.setLinkCount(oldLinks.size() + newLinks.size());
					page.setNewLinks(newLinks.size());
					page.setLanguage(mDetector.detect(dataPair.getSecond()));
					
					mCallback.onCrawlingPageFinished(page);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				setParserState(IDLE);
				
				try {
					sleep(WebCrawler.THREAD_WAIT_TIME_MILLIS);
				} catch (InterruptedException e) {/* ignore */}
			}
			
			if(isInterrupted())
				setRunning(false);
		}
		
		setParserState(STOPPED);
	}
	
	private URI normalizeURI(URI uri) throws URISyntaxException{
		String scheme = uri.getScheme();
		String host = uri.getHost();
		int port = uri.getPort();
		String path = uri.getPath();
		
		return new URI(scheme, null, host, (-1 == port)?(80):(port), path, null, null);
	}
}
