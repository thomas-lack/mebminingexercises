package webminig.webcrawler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class Parser {	
	public Pair<List<String>,String> parse(String html) {
		final List<String> result = new ArrayList<String>();
		final StringBuilder textBuilder = new StringBuilder();
		
		ParserDelegator delegator = new ParserDelegator();
		ParserCallback callback = new ParserCallback(){
			private boolean isParserInP = false;
			
			@Override
			public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
				if(t.equals(HTML.Tag.A)){
					String href = (String) a.getAttribute(Attribute.HREF);
						
					if(null != href){
						result.add(href);
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
			delegator.parse(new StringReader(html), callback, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new Pair<List<String>, String>(result, textBuilder.toString());
	}
}
