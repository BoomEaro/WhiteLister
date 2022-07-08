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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.sqlite.JDBC;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.database.sections.SectionWhiteList;

public class Sql {

    private final ExecutorService executor;
    private final Connection connection;

    private static Sql instance = null;
    private static final String CON_STR = "jdbc:sqlite:[path]database.db";

    public static Sql getInstance() {
        return instance;
    }

    public static void initSql() throws SQLException {
        if (instance != null) {
            return;
        }

        instance = new Sql();
    }

    private Sql() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.executor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
                .setPriority(3)
                .setNameFormat("WhiteLister-SQL-%d")
                .build());

        this.connection = DriverManager.getConnection(CON_STR.replace("[path]", WhiteLister.getInstance().getDataFolder() + File.separator));

        createNewDatabaseWhiteList();
    }

    public Future<List<SectionWhiteList>> getAllDataWhiteList() {
        return this.executor.submit(() -> {
            try (Statement statement = this.connection.createStatement()) {
                List<SectionWhiteList> collections = new ArrayList<SectionWhiteList>();
                ResultSet resSet = statement.executeQuery("SELECT id, name, isProtected, timeAdded, whoAdd FROM list");
                while (resSet.next()) {
                    collections.add(new SectionWhiteList(resSet.getInt("id"), resSet.getString("name"), resSet.getBoolean("isProtected"), resSet.getLong("timeAdded"), resSet.getString("whoAdd")));
                }
                return collections;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        });
    }

    public void putWhiteList(String name, boolean isProtected, Long timeAdded, String whoAdd) {
        this.executor.execute(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO list(`name`, `isProtected`, `timeAdded`, `whoAdd`) " +
                            "VALUES(?, ?, ?, ?)")) {
                statement.setString(1, name);
                statement.setBoolean(2, isProtected);
                statement.setLong(3, timeAdded);
                statement.setString(4, whoAdd);
                statement.execute();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeWhiteList(String name) {
        this.executor.execute(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM list WHERE name = ?")) {
                statement.setString(1, name);

                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateWhiteList(String name, boolean isProtected, Long timeAdded, String whoAdd) {
        this.executor.execute(() -> {
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
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void createNewDatabaseWhiteList() {
        String sql = "CREATE TABLE IF NOT EXISTS list (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	isProtected boolean NOT NULL,\n"
                + "	timeAdded long NOT NULL,\n"
                + "	whoAdd text NOT NULL\n"
                + ");";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(sql);
            WhiteLister.getInstance().getLogger().info("Таблица белого списка успешно загружена.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws SQLException, InterruptedException {
        this.executor.shutdown();
        this.executor.awaitTermination(15, TimeUnit.SECONDS);
        this.connection.close();
    }
}
