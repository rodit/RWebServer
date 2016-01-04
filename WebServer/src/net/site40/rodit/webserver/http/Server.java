package net.site40.rodit.webserver.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import net.site40.rodit.webserver.Configuration;
import net.site40.rodit.webserver.commands.CommandManager;
import net.site40.rodit.webserver.permissions.AuthenticationManager;
import net.site40.rodit.webserver.permissions.PermissionManager;
import net.site40.rodit.webserver.plugins.PluginManager;
import net.site40.rodit.webserver.rscript.ScriptManager;
import net.site40.rodit.webserver.session.SessionManager;
import net.site40.rodit.webserver.util.FileUtil;
import net.site40.rodit.webserver.util.Log;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

public class Server implements HttpHandler{

	private Log log = new Log("Server");
	private Configuration config;
	private HttpServer server;
	private PermissionManager permissions;
	private SessionManager sessions;
	private ScriptManager scripts;
	private AuthenticationManager auth;
	private CommandManager commands;
	private PluginManager plugins;
	private boolean running;
	
	private String version = "1.0.1";

	public Server(Configuration config){
		log.i("Initializing server...");
		this.config = config;
		try{
			if(config.readBool("server.https")){
				log.i("Initializing https...");
				server = HttpsServer.create(new InetSocketAddress(config.readInt("server.port")), 0);
				
				log.e("HTTPS is currently not supported.");
				return;
			}else
				server = HttpServer.create(new InetSocketAddress(config.readInt("server.port")), 0);
		}catch(IOException e){
			log.e("Error while creating server. Aborting setup (" + e.getMessage() + ")");
			return;
		}/*catch(NoSuchAlgorithmException e){
			Log.e("Server", "Failed to setup https. Aborting setup (" + e.getMessage() + ")");
			return;
		}catch(KeyStoreException e){
			Log.e("Server", "Error while setting up keystore - " + e.getMessage());
			return;
		}catch(CertificateException e){
			Log.e("Server", "Error while reading keystore - " + e.getMessage());
			return;
		}catch(UnrecoverableKeyException e){
			Log.e("Server", "Error while setting up key manager - " + e.getMessage());
			return;
		}catch(KeyManagementException e){
			Log.e("Server", "Error while setting up ssl context - " + e.getMessage());
			return;
		}*/
		log.i("Loaded configuration.");
		File dir = new File(config.read("server.dir"));
		if(!dir.exists() || !dir.isDirectory())
			dir.mkdirs();
		server.createContext("/", this);
		log.i("Created server context.");
		server.setExecutor(null);
		permissions = new PermissionManager(config);
		log.i("Loaded permission manager.");
		sessions = new SessionManager(config);
		log.i("Loaded session manager.");
		scripts = new ScriptManager(config);
		log.i("Loaded script manager.");
		auth = new AuthenticationManager(config);
		log.i("Loaded authentication manager.");
		commands = new CommandManager(this);
		log.i("Loaded command manager.");
		plugins = new PluginManager(config);
		log.i("Loaded plugin manager.");
		plugins.init(this);
		log.i("Server initialized.");
		if(!config.readBool("rscript.enabled"))
			log.w("You have disabled RScript. This means any scripts written in documents will be served in plain text and visible to the client.");
	}
	
	public void command(String command){
		commands.run(command);
	}

	public boolean start(){
		if(server == null){
			log.e("Failed to start server - server = null.");
			return false;
		}
		if(running){
			log.w("Tried to start server when it was already running.");
			return false;
		}
		server.start();
		running = true;
		log.i("Server started.");
		return true;
	}

	public boolean stop(){
		if(server == null){
			log.e("Failed to stop server - server = null.");
			return false;
		}
		if(!running){
			log.w("Tried to stop server when it wasn't running.");
			return false;
		}
		server.stop(0);
		log.i("Server stopped.");
		return true;
	}

	public void restart(){
		stop();
		start();
	}

	public Configuration getConfig(){
		return config;
	}

	public PermissionManager getPermissions(){
		return permissions;
	}

	public SessionManager getSessions(){
		return sessions;
	}

	public ScriptManager getScripts(){
		return scripts;
	}

	public AuthenticationManager getAuth(){
		return auth;
	}
	
	public PluginManager getPlugins(){
		return plugins;
	}

	public String getVersion(){
		return version;
	}

	public String getDir(String requestFile){
		return getDir(new File(requestFile));
	}

	public String getDir(File requestFile){
		if(requestFile.isFile())
			requestFile = requestFile.getParentFile();
		requestFile = new File(requestFile.getPath().substring(config.read("server.dir").length()));
		return requestFile.getPath();
	}

	@Override
	public void handle(HttpExchange exchange)throws IOException{
		plugins.handle(this, exchange);
		String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
		String url = exchange.getRequestURI().toString().split(Pattern.quote("?"))[0];
		log.i("HTTP request from " + ip + " for resource " + url + ".");
		File request = new File(config.read("server.dir"), url);
		log.i("Requesting file " + request.getPath());
		if(!request.isFile() && request.isDirectory()){
			request = new File(request.getPath().endsWith("/") ? new File(request.getPath().substring(0, request.getPath().length() - 1)) : request, config.read("server.index"));
			log.i("Indexed request to " + request.getPath() + ".");
			File parent = request.getParentFile();
			if(!request.isFile() && parent.isDirectory() && config.readBool("server.list")){
				log.i("No index found. Attempting to list files in directory.");
				String list = "<html><head><title>Index of " + (request.isDirectory() ? request.getName() : request.getParent()) + "</title></head><body><div align=\"center\"><h1>Index of " + (request.isDirectory() ? request.getName() : request.getParent()) + "</h1><br><a href=\"../\">../</a><br>";
				for(String file : FileUtil.list(request.isDirectory() ? request : request.getParentFile()))
					if(!file.equals(".access") && !file.equals(".auth") && !file.equals(".pass"))
						list += "<a href=\"" + file + "\">" + file + "</a><br>";
				list += "</div></body></html>";
				sendPage(exchange, 200, list, request);
				return;
			}
		}
		if(!request.exists()){
			log.i("Resource @ " + request.getPath() + " not found. Sending 404 response instead.");
			sendPage(exchange, 404, FileUtil.readString(config.read("server.notfound")), request, config.read("server.notfound").endsWith(".rsc"));
			return;
		}
		if(!auth.authenticate(exchange, request)){
			log.i("Permissions denied for unauthed client @ " + ip + ".");
			sendPage(exchange, 401, FileUtil.readString(config.read("server.notauthed")), request, config.read("server.notauthed").endsWith(".rsc"));
			return;
		}
		if(!permissions.canAccess(exchange, request)){
			log.i("Permission denied for client @ " + ip + ".");
			sendPage(exchange, 403, FileUtil.readString(config.read("server.forbidden")), request, config.read("server.forbidden").endsWith(".rsc"));
			return;
		}
		if(request.getName().endsWith(".rsc"))
			sendPage(exchange, 200, FileUtil.readString(request), request, true);
		else
			sendPage(exchange, 200, FileUtil.readString(request), request);
	}
	
	public void sendPage(HttpExchange exchange, int status, String data, File requested){
		sendPage(exchange, status, data, requested, false);
	}
	
	public void sendPage(HttpExchange exchange, int status, String data, File requested, boolean script){
		writeFinal(exchange, status, script ? scripts.parsePage(data, exchange, this, requested) : data);
	}
	
	public void writeFinal(HttpExchange exchange, int status, String data){
		writeFinal(exchange, status, config.getMime("html"), data.getBytes());
	}
	
	public void writeFinal(HttpExchange exchange, int status, String mime, byte[] data){
		try{
			plugins.preWrite(this, exchange, status, mime, data);
			data = plugins.modifyBeforeSend(this, exchange, status, mime, data);
			exchange.getResponseHeaders().set("Content-Length", data.length + "");
			exchange.getResponseHeaders().set("Content-Type", mime.startsWith("text/") ? mime + "; charset=utf-8" : mime);
			if(exchange.getResponseCode() != 301)
				exchange.sendResponseHeaders(status, data.length);
			OutputStream out = exchange.getResponseBody();
			out.write(data);
			out.close();
			plugins.postWrite(this, exchange, status, mime, data);
			log.i("Sent response length=" + data.length + ".");
		}catch(IOException e){
			log.i("Error while writing response to client - " + e.getMessage());
		}
	}
}
