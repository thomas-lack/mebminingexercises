package webminig.webcrawler;

import java.net.URI;

import webminig.webcrawler.LanguageDetector.Language;

public class CrawledPage{
	private URI mUri = null;
	private int mLinkCount = 0;
	private int mNewLinks = 0;
	private Language mLanguage = null;
	
	public CrawledPage(URI uri) {
		this.mUri = uri;
	}

	public URI getUri() {
		return mUri;
	}

	public void setUri(URI uri) {
		this.mUri = uri;
	}

	public int getLinkCount() {
		return mLinkCount;
	}

	public void setLinkCount(int linkCount) {
		this.mLinkCount = linkCount;
	}

	public int getNewLinks() {
		return mNewLinks;
	}

	public void setNewLinks(int newLinks) {
		this.mNewLinks = newLinks;
	}
	
	public Language getLanguage() {
		return mLanguage;
	}
	
	public void setLanguage(Language mLanguage) {
		this.mLanguage = mLanguage;
	}
	
	@Override
	public String toString() {
		return "{CrawledPage|" + mUri + "," + mLinkCount + "," + mNewLinks + "," + mLanguage + "}";
	}
}
