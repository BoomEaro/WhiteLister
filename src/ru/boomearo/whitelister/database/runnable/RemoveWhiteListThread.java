package ru.boomearo.whitelister.database.runnable;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.database.Sql;

public class RemoveWhiteListThread extends BukkitRunnable {
	
	private final String playerName;
	
	public RemoveWhiteListThread(String playerName) {
		this.playerName = playerName;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(WhiteLister.getInstance());
	}

	@Override
	public void run() {
		try {
			Sql.getInstance().removeWhiteList(this.playerName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
