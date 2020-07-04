package ru.boomearo.whitelister.database.runnable;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.database.Sql;

public class PutWhiteListThread extends BukkitRunnable {
	
	private String name;
	private boolean isProtected;
	private long timeAdded;
	private String whoAdd;
	
	public PutWhiteListThread(String name, boolean isProtected, long timeAdded, String whoAdd) {
		this.name = name;
		this.isProtected = isProtected;
		this.timeAdded = timeAdded;
		this.whoAdd = whoAdd;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(WhiteLister.getInstance());
	}
	

	@Override
	public void run() {
		try {
			Sql.getInstance().putWhiteList(this.name, this.isProtected, this.timeAdded, this.whoAdd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
