package net.site40.rodit.webserver.rscript;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.site40.rodit.webserver.Configuration;
import net.site40.rodit.webserver.http.Server;
import net.site40.rodit.webserver.util.Log;

import com.sun.net.httpserver.HttpExchange;

public class ScriptManager {

	private static final String SCRIPT_OPEN = "<?rsc";
	private static final String SCRIPT_CLOSE = "?>";

	private Configuration config;
	private ScriptEngineManager manager;
	private ScriptEngine engine;

	public ScriptManager(Configuration config){
		this.config = config;
		this.manager = new ScriptEngineManager();
	}

	public String parsePage(String page, HttpExchange cex, Server server, File requested){
		Log log = new Log("RScript");
		long time = System.currentTimeMillis();
		boolean next = true;
		String replaced = page;
		while(next){
			int indexo = replaced.indexOf(SCRIPT_OPEN);
			int indexc = replaced.indexOf(SCRIPT_CLOSE);
			if(indexo > -1 && indexc > -1){
				String script = replaced.substring(indexo + 5, indexc - 3);
				String replace = replaced.substring(replaced.indexOf(SCRIPT_OPEN), replaced.indexOf(SCRIPT_CLOSE) + 2);
				String scrReturn = evaluate(script, cex, server, requested, log);
				replaced = replaced.replace(replace, scrReturn);
			}
			next = replaced.indexOf(SCRIPT_OPEN) > -1 && replaced.indexOf(SCRIPT_CLOSE) > -1;
		}
		log.i("Script execution took " + (System.currentTimeMillis() - time) + "ms");
		return replaced;
	}

	public String evaluate(String script, HttpExchange cex, Server server, File requested, Log log){
		StringWriter sw = null;
		PrintWriter writer = null;
		if(!config.readBool("rscript.enabled"))
			return script;
		try{
			engine = manager.getEngineByName("JavaScript");
			PostData post = new PostData(cex);
			QueryString query = new QueryString(cex.getRequestURI());
			engine.put("log", log);
			engine.put("postdata", post);
			engine.put("querystring", query);
			engine.put("stdin", cex.getRequestBody());
			engine.put("stdout", cex.getResponseBody());
			engine.put("server", server);
			engine.put("config", server.getConfig());
			engine.put("permissions", server.getPermissions());
			engine.put("script", this);
			engine.put("sessions", server.getSessions());
			engine.put("request", cex);
			engine.put("session", server.getSessions().get(cex));
			engine.put("requested", requested);
			engine.put("SERVER_NAME", "RWebServer");
			engine.put("SERVER_VERSION", server.getVersion());
			engine.put("SERVER_RSCRIPT", config.read("rscript.enabled"));
			engine.put("SERVER_CONFIG", "server.cfg");
			engine.put("SERVER_DOCUMENTS", config.read("server.dir"));
			engine.put("OS_NAME", System.getProperty("os.name"));
			sw = new StringWriter();
			writer = new PrintWriter(sw);
			log.setOut(writer);
			log.setErr(writer);
			log.tags(false);
			engine.getContext().setWriter(writer);
			engine.eval(addFunctions(script));
		}catch(Exception e){
			String msg = e.getMessage();
			if(msg.contains("line number ")){
				int numIndex = msg.indexOf("line number ") + "line number ".length();
				int index = 0;
				String total = "";
				while(true){
					try{
						total += Integer.valueOf(msg.charAt(numIndex + index) + "");
						index++;
					}catch(NumberFormatException nfe){
						break;
					}
				}
				int num = Integer.valueOf(total) - functions_lines;
				msg = msg.replace("line number " + total, "line number " + num);
			}
			if(writer != null)
				writer.println("<br>Script error - " + msg);
			else
				return "<br>Script error - " + msg;
		}
		return new String(sw.getBuffer());
	}

	private static final String functions = ""
			+ "var files = Java.type('net.site40.rodit.webserver.util.FileUtil');\n"
			+ "var cookies = Java.type('net.site40.rodit.webserver.util.CookieUtil');\n"
			+ "var system = Java.type('java.lang.System');\n"
			+ "var File = Java.type('java.io.File');\n"
			+ "var jString = Java.type('java.lang.String');\n"
			+ "var mysql = Java.type('net.site40.rodit.webserver.mysql.MySql');\n"
			+ "var mysql_obj = null;\n"
			+ "function abs(file){\n"
			+ "    return new File(SERVER_DOCUMENTS + server.getDir(requested), file);\n"
			+ "}\n"
			+ "function echo(text){\n"
			+ "    print(text);\n"
			+ "}\n"
			+ "function session_start(){\n"
			+ "    if(!sessions.hasSession(request))\n"
			+ "        session = sessions.create(request);\n"
			+ "}\n"
			+ "function session_destroy(){\n"
			+ "    sessions.destroy(sessions.get(request));\n"
			+ "    session = null;\n"
			+ "}\n"
			+ "function session_reset(){\n"
			+ "    session_destroy();\n"
			+ "    session_start();\n"
			+ "}\n"
			+ "function read(istream){\n"
			+ "    return files.read(istream);\n"
			+ "}\n"
			+ "function read_file_raw(file){\n"
			+ "    return files.read(abs(file));\n"
			+ "}\n"
			+ "function read_file(file){\n"
			+ "    return files.readString(abs(file));\n"
			+ "}\n"
			+ "function get_cookie(name){\n"
			+ "    return cookies.getCookies(request.getRequestHeaders()).get(name);\n"
			+ "}\n"
			+ "function set_cookie(name, value){"
			+ "    request.getResponseHeaders().add('Set-Cookie', name + '=' + value + ';');\n"
			+ "}\n"
			+ "function get_cookies(){\n"
			+ "    return cookies.getCookies(request.getRequestHeaders());\n"
			+ "}\n"
			+ "function header(name, value){\n"
			+ "    request.getResponseHeaders().set(name, value);\n"
			+ "    if(name == 'Location' || name == 'location')\n"
			+ "        request.sendResponseHeaders(301, 0);\n"
			+ "}\n"
			+ "function get_header(name){\n"
			+ "    return request.getRequestHeaders().getFirst(name);\n"
			+ "}\n"
			+ "function get_files(file){\n"
			+ "    return files.list(abs(file));\n"
			+ "}\n"
			+ "function file_size(file){\n"
			+ "    return files.size(abs(file));\n"
			+ "}\n"
			+ "function file_exists(file){\n"
			+ "    return files.exists(abs(file));\n"
			+ "}\n"
			+ "function is_file(file){\n"
			+ "    return files.isFile(abs(file));\n"
			+ "}\n"
			+ "function is_dir(file){\n"
			+ "    return files.isDirectory(abs(file));\n"
			+ "}\n"
			+ "function include(path){\n"
			+ "    var pathhelper = new File(server.getDir(requested)).getParent();\n"
			+ "    if(pathhelper == null)pathhelper = '';\n"
			+ "    print(script.parsePage(files.readString(new File(SERVER_DOCUMENTS + server.getDir(requested), path)), request, server, new File(SERVER_DOCUMENTS + pathhelper, path)));\n"
			+ "}\n"
			+ "function post(key){\n"
			+ "    return postdata.get(key);\n"
			+ "}\n"
			+ "function query(key){\n"
			+ "    return querystring.get(key);\n"
			+ "}\n"
			+ "function isset(object){\n"
			+ "    return object != null && object != '';\n"
			+ "}\n"
			+ "function write(stream, data){\n"
			+ "    stream.write(data);\n"
			+ "    stream.flush();\n"
			+ "}\n"
			+ "function write_file(file, contents){\n"
			+ "    write_file(file, contents, false);\n"
			+ "}\n"
			+ "function write_file(file, contents, append){\n"
			+ "    files.write(abs(file), contents, append);\n"
			+ "}\n"
			+ "function delete_file(file){\n"
			+ "    abs(file).delete();\n"
			+ "}\n"
			+ "function create_file(file){\n"
			+ "    touch(file);\n"
			+ "}\n"
			+ "function touch(file){\n"
			+ "    files.touch(abs(file));\n"
			+ "}\n"
			+ "function mkdir(file){\n"
			+ "    abs(file).mkdir();\n"
			+ "}\n"
			+ "function mkdirs(file){\n"
			+ "    abs(file).mkdirs();\n"
			+ "}\n"
			+ "function FatalError(){ Error.apply(this, arguments); this.name = 'FatalError'; }\n"
			+ "FatalError.prototype = Object.create(Error.prototype);\n"
			+ "function exit(){\n"
			+ "    print('Exit not implemented yet.');//throw new FatalError('');\n"
			+ "}\n"
			+ "function exec(file){\n"
			+ "    return files.runString(abs(file));\n"
			+ "}\n"
			+ "function exec_raw(file){\n"
			+ "    return files.run(file);\n"
			+ "}\n"
			+ "function exec_async(file){\n"
			+ "    files.runAsync(file);\n"
			+ "}\n"
			+ "function mysqli_connect(host, database, username, password){\n"
			+ "    var con = new mysql(host, database, username, password);\n"
			+ "    con.setLog(log);\n"
			+ "    return con;\n"
			+ "}\n"
			+ "function mysqli_disconnect(con){\n"
			+ "    con.close();\n"
			+ "}\n"
			+ "function mysqli_query(con, sql, args){\n"
			+ "    return con.query(sql, args);\n"
			+ "}\n"
			+ "function mysqli_query_update(con, sql, args){\n"
			+ "    return con.queryUpdate(sql, args);\n"
			+ "}\n"
			+ "function mysql_connect(host, database, username, password){\n"
			+ "    if(mysql_obj == null)\n"
			+ "        mysql_obj = mysqli_connect(host, database, username, password);\n"
			+ "    else"
			+ "        log.e('');"
			+ "}\n"
			+ "function mysql_disconnect(){\n"
			+ "    if(mysql_obj != null){\n"
			+ "        mysqli_disconnect(mysql_obj);\n"
			+ "        mysql_obj = null;\n"
			+ "    }\n"
			+ "}\n"
			+ "function mysql_query(sql, args){\n"
			+ "    if(mysql_obj != null){\n"
			+ "        if(args != null)\n"
			+ "            return mysqli_query(mysql_obj, sql, args);\n"
			+ "        else\n"
			+ "            return mysqli_query(mysql_obj, sql, null);"
			+ "    }\n"
			+ "}\n"
			+ "function mysql_query_update(sql, args){\n"
			+ "    if(mysql_obj != null){\n"
			+ "        if(args != null)\n"
			+ "            return mysqli_query_update(mysql_obj, sql, args);\n"
			+ "        else\n"
			+ "            return mysqli_query_update(mysql_obj, sql, null);"
			+ "    }\n"
			+ "}\n"
			+ "function mysql_num_rows(rs)\n{"
			+ "    var row = rs.getRow();\n"
			+ "    rs.absolute(0);"
			+ "    var count = 0;\n"
			+ "    while(rs.next())\n"
			+ "        count++;\n"
			+ "    rs.absolute(row);\n"
			+ "    return count;\n"
			+ "}\n";
	private static final int functions_lines = functions.split("\n").length;

	public String addFunctions(String script){
		return functions + script;
	}
}
