package me.korbsti.soaromach.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import me.korbsti.soaromach.PremiumChatChannels;

public class MySQLAPI {

	private Connection connection;
	private String database;
	private String username;
	private String password;

	public API api;
	
	PremiumChatChannels plugin;

	public MySQLAPI(String database, String username, String password, PremiumChatChannels plugin) {
		this.database = database;
		this.username = username;
		this.password = password;
		connectMySQL(database, username, password);
		api = new API(this, plugin);
		this.plugin = plugin;
	}


	public void connectMySQL(String database, String username, String password) {

		try {
			connection = DriverManager.getConnection(database, username, password);

			if (connection != null) {
				System.out.println("Connected to database");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
