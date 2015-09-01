package net.site40.rodit.webserver;

import java.util.Scanner;

import net.site40.rodit.webserver.http.Server;

public class WebServer {

	private static Server server;
	private static Configuration config;

	public static void main(String[] args){
		config = new Configuration();
		config.load("server.cfg");
		//Log.loadGobal(config);
		server = new Server(config);
		server.start();
		if(config.readBool("server.shell")){
			Scanner scanner = new Scanner(System.in);
			String line = null;
			System.out.print("RWebServer ~>");
			while((line = scanner.nextLine()) != null){
				server.command(line);
				System.out.print("RWebServer ~>");
			}
			scanner.close();
		}
	}
}
