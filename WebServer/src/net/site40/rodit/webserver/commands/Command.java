package net.site40.rodit.webserver.commands;

import java.util.HashMap;

public abstract class Command {

	protected String name;
	protected String usage;
	protected String desc;
	protected String error;
	protected CommandManager manager;
	
	public Command(String name, String usage, String desc){
		this.name = name;
		this.usage = usage;
		this.desc = desc;
	}
	
	public void setManager(CommandManager manager){
		this.manager = manager;
	}
	
	public String getName(){
		return name;
	}
	
	public String getUsage(){
		return usage;
	}
	
	public String getDesc(){
		return desc;
	}
	
	protected void error(String error){
		this.error = error;
	}
	
	protected void write(String msg){
		System.out.println(msg);
	}
	
	public abstract boolean run(HashMap<String, String> args);
}
