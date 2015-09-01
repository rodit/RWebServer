package net.site40.rodit.webserver.plugins;

import net.site40.rodit.webserver.http.Server;

import com.sun.net.httpserver.HttpExchange;

public interface IPlugin {

	public void init(Server server);
	public void handle(Server server, HttpExchange exchange);
	public void preWrite(Server server, HttpExchange exchange, int status, String mime, byte[] data);
	public void postWrite(Server server, HttpExchange exchange, int status, String mime, byte[] data);
	public String getName();
	public String getVersion();
}
