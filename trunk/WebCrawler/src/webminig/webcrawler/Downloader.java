package webminig.webcrawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Downloader extends Thread {
	private IDownloadCallback mCallback = null;
	private HttpClient mHttpClient = null;
	private boolean isRunning = true;
	
	public static interface IDownloadCallback{
		public boolean hasNext();
		public URI getNext();
		public void onDownloadFinished(URI uri, String data);
	}
	
	public Downloader(IDownloadCallback callback) {
		mCallback = callback;
		mHttpClient = new DefaultHttpClient();
	}
	
	public void setRunning(boolean isRunning) {
		synchronized (this) {			
			this.isRunning = isRunning;
		}
	}
	
	@Override
	public void run() {
		if(null == mCallback || null == mHttpClient)
			return;
		
		while (isRunning) {
			if(mCallback.hasNext()){
				URI uri = mCallback.getNext();
				
				if(null == uri)
					continue;
				
				try {
					//System.out.println("Downloader " + getId() + ": " + uri);
					HttpGet get = new HttpGet(uri);
					HttpResponse response = mHttpClient.execute(get);
					
					int status = response.getStatusLine().getStatusCode();
					HttpEntity entity = response.getEntity();
					if(200 == status){
						if(entity.getContentType().getValue().contains("text/html")){
							BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
							StringBuilder sb = new StringBuilder();
							String line = "";
							
							while (null != (line = br.readLine())) {
								sb.append(line).append("\n");
							}
							
							mCallback.onDownloadFinished(uri, sb.toString());
						}
						else{
							entity.getContent().close();
						}
					}
					else{
						entity.getContent().close();
					}
				} catch (Throwable e) {
					System.err.println("Downloader " + getId() + ": " + uri + " - " + e.getMessage());
				}
				
			}
			
			try {
				sleep(WebCrawler.THREAD_WAIT_TIME_MILLIS);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
}
