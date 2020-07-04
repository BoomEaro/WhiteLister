package ru.boomearo.whitelister.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sqlite.JDBC;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.database.sections.SectionWhiteList;

public class Sql {
	private static Sql instance = null; 
	private static final String CON_STR = "jdbc:sqlite:[path]database.db";

	public static synchronized Sql getInstance() throws SQLException { 
		if(instance == null)
			instance = new Sql();
		return instance;
	}

	private Connection connection; 

	private Sql() throws SQLException { 
		DriverManager.registerDriver(new JDBC()); 
		this.connection = DriverManager.getConnection(CON_STR.replace("[path]", WhiteLister.getContext().getDataFolder() + File.separator)); 
	}

	public synchronized List<SectionWhiteList> getAllDataWhiteList() { 
		try (Statement statement = this.connection.createStatement()) {
			List<SectionWhiteList> collections = new ArrayList<SectionWhiteList>();
			ResultSet resSet = statement.executeQuery("SELECT id, name, isProtected, timeAdded, whoAdd FROM list");
			while(resSet.next()) {
				collections.add(new SectionWhiteList(resSet.getInt("id"), resSet.getString("name"), resSet.getBoolean("isProtected"), resSet.getLong("timeAdded"), resSet.getString("whoAdd")));
			}
			return collections;
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	public synchronized SectionWhiteList getDataWhiteList(String name) {
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resSet = statement.executeQuery("SELECT id, isProtected, timeAdded, whoAdd FROM list WHERE name = '" + name + "' LIMIT 1");

			if (resSet.next()) {
				return new SectionWhiteList(resSet.getInt("id"), name, resSet.getBoolean("isProtected"), resSet.getLong("timeAdded"), resSet.getString("whoAdd"));
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	public synchronized void putWhiteList(String name, boolean isProtected, Long timeAdded, String whoAdd) { 
		try (PreparedStatement statement = this.connection.prepareStatement(
				"INSERT INTO list(`name`, `isProtected`, `timeAdded`, `whoAdd`) " +
				"VALUES(?, ?, ?, ?)")) {
			statement.setString(1, name);
			statement.setBoolean(2, isProtected);
			statement.setLong(3, timeAdded);
			statement.setString(4, whoAdd);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean removeWhiteList(String name) { 
		try (Statement statement = this.connection.createStatement()) {
			return statement.execute("DELETE FROM list WHERE name = '" + name + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized void updateWhiteList(String name, boolean isProtected, Long timeAdded, String whoAdd) {
		String sql = "UPDATE list SET isProtected = ? , "
				+ "timeAdded = ? , "
				+ "whoAdd = ? "
				+ "WHERE name = ?";

		try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {

			pstmt.setBoolean(1, isProtected);
			pstmt.setLong(2, timeAdded);
			pstmt.setString(3, whoAdd);
			pstmt.setString(4, name);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void createNewDatabaseWhiteList() {
		String sql = "CREATE TABLE IF NOT EXISTS list (\n"
				+ "	id integer PRIMARY KEY,\n"
				+ "	name text NOT NULL,\n"
				+ "	isProtected boolean NOT NULL,\n"
				+ "	timeAdded long NOT NULL,\n"
				+ "	whoAdd text NOT NULL\n"
				+ ");";

		try (Statement stmt = this.connection.createStatement()) {
			stmt.execute(sql);
			WhiteLister.getContext().getLogger().info("Таблица белого списка успешно загружена.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public synchronized void Disconnect() throws SQLException {
		this.connection.close();
	}
}
