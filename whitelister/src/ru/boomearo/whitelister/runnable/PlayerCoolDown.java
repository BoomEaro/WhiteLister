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
		this.runTaskLaterAsynchronously(WhiteLister.getContext(), 30*20);
	}
	
	@Override
	public void run() {
		WhiteLister.getContext().getWhiteListManager().removePlayerCd(this.name);
		this.cancel();
	}
	
	public String getName() {
		return this.name;
	}
	
}