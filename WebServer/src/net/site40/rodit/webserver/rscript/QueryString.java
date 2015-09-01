package net.site40.rodit.webserver.rscript;

import java.net.URI;
import java.util.HashMap;
import java.util.regex.Pattern;

public class QueryString {

	private URI uri;
	private HashMap<String, String> params;

	public QueryString(URI uri){
		this.uri = uri;
		this.params = new HashMap<String, String>();
		for(String qs : (uri.getQuery() != null ? uri.getQuery().split(Pattern.quote("&")) : new String[0])){
			String[] p = qs.split(Pattern.quote("="));
			if(p.length > 1)
				params.put(p[0], p[1]);
		}
	}

	public URI getUrl(){
		return uri;
	}

	public String get(String key){
		return params.get(key) != null ? params.get(key) : "";
	}
}
