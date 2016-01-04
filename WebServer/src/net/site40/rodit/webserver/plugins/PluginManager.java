package net.site40.rodit.webserver.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import net.site40.rodit.webserver.Configuration;
import net.site40.rodit.webserver.http.Server;
import net.site40.rodit.webserver.util.Log;

import com.sun.net.httpserver.HttpExchange;

public class PluginManager {

	private Log log = new Log("PluginManager");
	private ArrayList<IPlugin> plugins;

	public PluginManager(Configuration config){
		this.plugins = new ArrayList<IPlugin>();
		if(config.readBool("plugins.enabled")){
			for(String plugin : new File(config.read("plugins.dir")).list()){
				File file = new File(config.read("plugins.dir"), plugin);
				if(!file.exists()){
					log.i("Plugin load failed. Plugin file '" + file.getPath() + "' could not be found.");
					continue;
				}
				log.i("Loading plugin " + file.getName() + ".");
				try{
					JarFile jarFile = new JarFile(file);
					Enumeration<JarEntry> e = jarFile.entries();

					URL[] urls = { new URL("jar:file:" + file.getPath() + "!/") };
					URLClassLoader cl = URLClassLoader.newInstance(urls);
					
					String pluginClass = "";

					ArrayList<String> postLoad = new ArrayList<String>();
					ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
					while (e.hasMoreElements()) {
						JarEntry je = (JarEntry) e.nextElement();
						if(je.isDirectory() || !je.getName().endsWith(".class")){
							if(je.getName().equals("plugin.info")){
								InputStream is = jarFile.getInputStream(je);
								byte[] data = new byte[is.available()];
								is.read(data);
								is.close();
								String dat = new String(data);
								for(String line : dat.split("\n")){
									String[] parts = line.split(Pattern.quote("="));
									if(parts.length < 2)
										continue;
									if(parts[0].equals("main"))
										pluginClass = parts[1];
								}
							}
							continue;
						}
						String className = je.getName().substring(0, je.getName().length() - 6);
						postLoad.add(className);
					}
					for(String className : postLoad){
						log.d("Loading plugin class " + className);
						className = className.replace('/', '.');
						Class<?> c = cl.loadClass(className);
						classes.add(c);
					}
					jarFile.close();
					for(Class<?> cls : classes){
						if(cls.getCanonicalName().equals(pluginClass)){
							Object plug = cls.newInstance();
							if(plug instanceof IPlugin){
								IPlugin iplug = (IPlugin)plug;
								plugins.add(iplug);
								log.i("Loaded plugin " + iplug.getName() + " version " + iplug.getVersion() + ".");
							}
						}
					}
				}catch(IOException e){
					log.e("IOException while loading plugin - " + e.getMessage());
				}catch(ClassNotFoundException e){
					log.e("ClassNotFoundException while loading plugin - " + e.getMessage());
				}catch(InstantiationException e){
					log.e("InstantiationException while loading plugin - " + e.getMessage());
				}catch(IllegalAccessException e){
					log.e("IllegalAccessException while loading plugin - " + e.getMessage());
				}
			}
		}
	}

	public void init(Server server){
		for(IPlugin p : plugins)
			p.init(server);
	}

	public void handle(Server server, HttpExchange exchange){
		for(IPlugin p : plugins)
			p.handle(server, exchange);
	}

	public void preWrite(Server server, HttpExchange exchange, int status, String mime, byte[] data){
		for(IPlugin p : plugins)
			p.preWrite(server, exchange, status, mime, data);
	}

	public byte[] modifyBeforeSend(Server server, HttpExchange exchange, int status, String mime, byte[] data){
		for(IPlugin p : plugins)
			data = p.modifyBeforeSend(server, exchange, status, mime, data);
		return data;
	}

	public void postWrite(Server server, HttpExchange exchange, int status,String mime, byte[] data){
		for(IPlugin p : plugins)
			p.postWrite(server, exchange, status, mime, data);
	}
}
