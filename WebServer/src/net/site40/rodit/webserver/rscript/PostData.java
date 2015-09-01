package net.site40.rodit.webserver.rscript;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.site40.rodit.webserver.util.FileUtil;

import com.sun.net.httpserver.HttpExchange;

public class PostData {

	private byte[] raw;
	private HashMap<String, String> form;

	public PostData(HttpExchange exchange){
		this.raw = FileUtil.read(exchange.getRequestBody());
		this.form = new HashMap<String, String>();
		if(exchange.getRequestMethod().toLowerCase().equals("post") && exchange.getRequestHeaders().getFirst("Content-Type") != null && exchange.getRequestHeaders().getFirst("Content-Type").equals("application/x-www-form-urlencoded")){
			String str = new String(raw);
			for(String param : str.split(Pattern.quote("&"))){
				String[] parts = param.split(Pattern.quote("="));
				if(parts.length > 1)
					form.put(URLDecoder.decode(parts[0]), URLDecoder.decode(parts[1]));
			}
		}
	}

	public byte[] getRaw(){
		return raw;
	}

	public String get(String key){
		return form.get(key) != null ? form.get(key) : "";
	}
}
