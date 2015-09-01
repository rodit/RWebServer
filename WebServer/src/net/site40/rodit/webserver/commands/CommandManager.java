package net.site40.rodit.webserver.commands;

import java.util.ArrayList;
import java.util.HashMap;

import net.site40.rodit.webserver.http.Server;

public class CommandManager {

	private ArrayList<Command> commands;
	private Server server;

	public CommandManager(Server server){
		this.commands = new ArrayList<Command>();
		this.server = server;

		add(new CommandHelp());
		add(new CommandGC());
		add(new CommandStart());
		add(new CommandStop());
		add(new CommandRestart());
		add(new CommandConfig());
	}

	public void add(Command command){
		commands.add(command);
		command.setManager(this);
	}

	public void remove(Command command){
		commands.remove(command);
		command.setManager(null);
	}

	public Command get(String name){
		for(Command c : commands)
			if(c.getName().equals(name))
				return c;
		return null;
	}

	public Server getServer(){
		return server;
	}

	public void run(String command){
		String[] words = command.split("\\s+");
		Command c = get(words[0]);
		if(c == null){
			System.out.println("Command '" + words[0] + "' not found.");
			return;
		}
		HashMap<String, String> args = new HashMap<String, String>();
		String opt = "";
		String val = "";
		int optIndex = 0;
		for(int i = 0; i < command.length(); i++){
			char ch = command.charAt(i);
			if(!opt.isEmpty() && i > optIndex + 2 && !((command.length() > i + 1 && command.charAt(i + 1) == '-') || (command.charAt(i) == '-')))
				val += ch;
			if((ch == '-' && !opt.isEmpty() && !val.isEmpty()) || (i == command.length() - 1 && !opt.isEmpty() && !val.isEmpty())){
				args.put(opt, val);
				opt = "";
				val = "";
			}
			if(ch == '-' && command.length() > i + 1){
				opt = command.charAt(i + 1) + "";
				optIndex = i;
				continue;
			}
		}
		c.run(args);
	}

	static class CommandHelp extends Command{

		public CommandHelp(){
			super("help", "help [-c command]", "Shows the help screen or the usage of the given command.");
		}

		@Override
		public boolean run(HashMap<String, String> args){
			if(args.get("c") != null){
				Command c = manager.get(args.get("c"));
				if(c != null)
					write("Usage:\n    " + c.getUsage());
				else{
					error("Command " + args.get("c") + " not found.");
					return false;
				}
			}
			write("Available Commands:");
			for(Command c : manager.commands)
				write("    " + c.getName() + " - " + c.getDesc());
			return true;
		}
	}

	static class CommandGC extends Command{

		public CommandGC(){
			super("gc", "gc", "Runs the garbage collector.");
		}

		@Override
		public boolean run(HashMap<String, String> args){
			System.gc();
			return true;
		}
	}

	static class CommandStop extends Command{

		public CommandStop(){
			super("stop", "stop", "Stops the server.");
		}

		@Override
		public boolean run(HashMap<String, String> args){
			if(manager.server.stop())
				return true;
			error("Failed to stop server. Is the server running?");
			return false;
		}
	}

	static class CommandStart extends Command{

		public CommandStart(){
			super("start", "start", "Starts the server.");
		}

		@Override
		public boolean run(HashMap<String, String> args){
			if(manager.server.start())
				return true;
			error("Failed to start server. Is the server already running?");
			return false;
		}
	}

	static class CommandRestart extends Command{

		public CommandRestart(){
			super("restart", "restart", "Restarts the server.");
		}

		@Override
		public boolean run(HashMap<String, String> args){
			manager.server.restart();
			return true;
		}
	}

	static class CommandConfig extends Command{

		public CommandConfig(){
			super("config", "config -k key\n       -v value", "Modifies the server configuration");
		}

		@Override
		public boolean run(HashMap<String, String> args){
			String key = args.get("k");
			String val = args.get("v");
			if(key == null || key.isEmpty()){
				error("Invalid key.");
				return false;
			}
			if(val == null || val.isEmpty()){
				error("Invalid value.");
				return false;
			}
			manager.server.getConfig().write(key, val);
			manager.server.getConfig().save("server.cfg");
			return true;
		}
	}
}
