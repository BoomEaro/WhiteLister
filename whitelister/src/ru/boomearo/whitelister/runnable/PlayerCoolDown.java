package ru.boomearo.whitelister.runnable;

import org.bukkit.scheduler.BukkitRunnable;
import ru.boomearo.whitelister.WhiteLister;

public class PlayerCoolDown extends BukkitRunnable {
	
	private String name;
	
	public PlayerCoolDown(String name) {
		this.name = name;
		runnable();
	}
	
	public void runnable() {
		this.runTaskLaterAsynchronously(WhiteLister.getInstance(), 30*20);
	}
	
	@Override
	public void run() {
		WhiteLister.getInstance().getWhiteListManager().removePlayerCd(this.name);
		this.cancel();
	}
	
	public String getName() {
		return this.name;
	}
	
}