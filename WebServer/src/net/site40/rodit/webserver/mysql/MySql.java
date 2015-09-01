package net.site40.rodit.webserver.mysql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import net.site40.rodit.webserver.util.Log;

public class MySql {

	private Log log = new Log("MySql");
	private Connection connection;

	public MySql(String address, String database, String username, String password){
		if(!address.contains(":"))
			address += ":3306";
		try{
			try{
				Class.forName("com.mysql.jdbc.Driver");
			}catch(ClassNotFoundException e){
				log.e("Error while loading mysql driver.");
				return;
			} 
			String url = "jdbc:mysql://" + address + "/" + database;
			Properties props = new Properties();
			props.setProperty("user", username);
			props.setProperty("password", password);
			connection = DriverManager.getConnection(url, props);
		}catch(SQLException e){
			log.e("Error while creating mysql connection - " + e.getMessage());
		}
	}

	public Log getLog(){
		return log;
	}

	public void setLog(Log log){
		this.log = log;
	}

	public void close(){
		try{
			if(!connection.isClosed())
				connection.close();
		}catch(SQLException e){
			log.e("Error while closing mysql connection - " + e.getMessage());
		}
	}
	
	public PreparedStatement makeStatement(String sql, Object... values){
		try{
			PreparedStatement statement = connection.prepareStatement(sql);
			for(int i = 0; i < values.length; i++){
				if(values[i] == null)
					continue;
				if(values[i] instanceof Serializable)
					statement.setObject(i + 1, values[i]);
				else
					log.w("Non-Serializable object passed to prepared statement.");
			}
			return statement;
		}catch(SQLException e){
			log.e("Error while creating query - " + e.getMessage());
		}
		return null;
	}

	public ResultSet query(String sql, Object... values){
		return query(makeStatement(sql, values));
	}

	public ResultSet query(PreparedStatement statement){
		try{
			return statement.executeQuery();
		}catch(SQLException e){
			log.e("Error while querying mysql - " + e.getMessage());
		}
		return null;
	}
	
	public int queryUpdate(String sql, Object... values){
		return queryUpdate(makeStatement(sql, values));
	}

	public int queryUpdate(PreparedStatement statement){
		try{
			return statement.executeUpdate();
		}catch(SQLException e){
			log.e("Error while querying mysql - " + e.getMessage());
		}
		return -1;
	}
}
