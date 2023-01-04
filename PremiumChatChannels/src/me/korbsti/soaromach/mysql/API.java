package me.korbsti.soaromach.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.korbsti.soaromach.PremiumChatChannels;

public class API {
	
	MySQLAPI main;
	PremiumChatChannels plugin;
	
	public API(MySQLAPI main, PremiumChatChannels plugin) {
		this.main = main;
		this.plugin = plugin;
	}
	
	public void createTable(String table, List<String> varNames) {
		try {
			String line = "CREATE TABLE IF NOT EXISTS " + table + " (id int NOT NULL AUTO_INCREMENT,";
			for (String str : varNames) {
				line += " " + str + " varchar(255),";
			}
			
			line += " PRIMARY KEY(id))";
			System.out.println(line);
			main.getConnection().prepareStatement(line).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addNewTableInformation(String table, List<String> varNames, List<String> varEquals) {
		
		String line = "INSERT INTO " + table + " (";
		int x = 0;
		for (String str : varNames) {
			if (varEquals.size() - 1 == x) {
				line += str + ")";
				break;
			}
			line += str + ", ";
			x++;
		}
		
		line += " VALUES ('";
		x = 0;
		
		for (String str : varEquals) {
			if (varEquals.size() - 1 == x) {
				line += str + "')";
				break;
			}
			line += str + "', '";
			x++;
		}
		
		try {
			Bukkit.getLogger().info(line);
			main.getConnection().prepareStatement(line).execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateTableInformation(String table, String id, String varName, String varEquals, Player p) {
		String uuid = p.getUniqueId().toString();
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				try {
					plugin.cacheSQL.put(uuid, true);
					main.getConnection().prepareStatement("UPDATE " + table + " SET " + varName + " = '" + varEquals
					        + "' WHERE " + table + ".id = " + id).executeUpdate();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					plugin.cacheSQL.put(uuid, true);
					
				}
			}
			
		});
		
	}
	
	public ArrayList<String> getTableInformation(String table, List<String> varNames) {
		try {
			String line = "SELECT id";
			for (String str : varNames) {
				line += "," + str;
			}
			line += " from " + table;
			
			PreparedStatement stmt = main.getConnection().prepareStatement(line);
			
			ResultSet set = stmt.executeQuery();
			ArrayList<String> returning = new ArrayList<String>();
			while (set.next()) {
				String returningLine = "";
				int x = 1;
				for (int i = -1; i != varNames.size(); i++) {
					returningLine += set.getString(x) + "=~=~";
					x++;
				}
				
				returning.add(returningLine.substring(0, returningLine.length() - 4));
			}
			return returning;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCertainTableInfoID(String table, List<String> varNames, int id) {
		try {
			String line = "SELECT id";
			for (String str : varNames) {
				line += "," + str;
			}
			line += " from " + table;
			
			PreparedStatement stmt = main.getConnection().prepareStatement(line);
			
			ResultSet set = stmt.executeQuery();
			while (set.next()) {
				String returningLine = "";
				int x = 1;
				for (int i = -1; i != varNames.size(); i++) {
					returningLine += set.getString(x) + "=~=~";
					x++;
				}
				returningLine = returningLine.substring(0, returningLine.length() - 4);
				
				if (returningLine.split("=~=~")[0].equals(id + "")) {
					return returningLine;
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
