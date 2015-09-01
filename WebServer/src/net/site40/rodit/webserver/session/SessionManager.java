package net.site40.rodit.webserver.session;

import java.util.HashMap;
import java.util.Random;

import net.site40.rodit.webserver.Configuration;
import net.site40.rodit.webserver.util.CookieUtil;
import net.site40.rodit.webserver.util.Log;

import com.sun.net.httpserver.HttpExchange;

public class SessionManager {

	private static final String ID_COOKIE_NAME = "RSSID";
	
	private Log log = new Log("Sessions");
	private Configuration config;
	private HashMap<String, Session> sessions;
	private Random random;

	public SessionManager(Configuration config){
		this.config = config;
		this.sessions = new HashMap<String, Session>();
		this.random = new Random();
	}

	private static final int ID_LENGTH = 32;
	private static final String ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	private String createId(){
		String id = "";
		for(int i = 0; i < ID_LENGTH; i++)
			id += ID_CHARS.charAt(random.nextInt(ID_CHARS.length() - 1));
		return id;
	}

	public Session create(){
		return create(null);
	}
	
	public Session create(HttpExchange exchange){
		String id = createId();
		if(exchange != null)
			exchange.getResponseHeaders().add("Set-Cookie", ID_COOKIE_NAME + "=" + id + ";");
		if(config.readBool("server.sessions.ipcheck"))
			id = (exchange != null ? exchange.getRemoteAddress().getAddress().getHostAddress() + ":" : "") + id;
		Session session = new Session(id);
		register(session);
		return session;
	}
	
	public void destroy(Session session){
		destroy(session.getId());
	}

	public void destroy(String id){
		if(config.readBool("server.sessions"))
			sessions.remove(id);
		else
			log.w("Tried to destroy session when sessions are disabled.");
	}

	public void register(Session session){
		if(config.readBool("server.sessions"))
			sessions.put(session.getId(), session);
		else
			log.w("Tried to register session when sessions are disabled.");
	}
	
	public Session get(HttpExchange exchange){
		String id = CookieUtil.getCookies(exchange.getRequestHeaders()).get(ID_COOKIE_NAME);
		if(config.readBool("server.sessions.ipcheck"))
			id = exchange.getRemoteAddress().getAddress().getHostAddress() + ":" + id;
		return sessions.get(id);
	}
	
	public boolean hasSession(HttpExchange exchange){
		return get(exchange) != null;
	}
}
