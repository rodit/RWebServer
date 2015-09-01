package net.site40.rodit.webserver.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Log {

	private static ArrayList<Log> logs = new ArrayList<Log>();

	private String tag;
	private OutputStream out;
	private OutputStream err;
	private boolean tags;

	public Log(){
		this("Log");
	}

	public Log(String tag){
		this(tag, System.out, System.err);
	}

	public Log(String tag, OutputStream out, OutputStream err){
		this.tag = tag;
		this.out = out;
		this.err = err;
		this.tags = true;
		logs.add(this);
	}

	public OutputStream getOut(){
		return out;
	}

	public void setOut(OutputStream out){
		this.out = out;
	}

	public void setOut(PrintWriter writer){
		this.out = new WriterOutputStream(writer);
	}

	public OutputStream getErr(){
		return err;
	}

	public void setErr(OutputStream err){
		this.err = err;
	}

	public void setErr(PrintWriter writer){
		this.err = new WriterOutputStream(writer);
	}
	
	public boolean tags(){
		return tags;
	}
	
	public void tags(boolean tags){
		this.tags = tags;
	}

	public void i(String msg){
		log(out, "INFO", msg);
	}

	public void w(String msg){
		log(out, "WARN", msg);
	}

	public void d(String msg){
		log(out, "DBUG", msg);
	}

	public void e(String msg){
		log(err, "ERRR", msg);
	}

	private void log(OutputStream stream, String type, String msg){
		try{
			stream.write(((tags ? "[" + type + "][" + tag + "]" : "") + msg + "\n").getBytes());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
