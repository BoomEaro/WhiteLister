package ru.boomearo.whitelister.object;

public class WhiteListedPlayer implements Comparable<WhiteListedPlayer> {
	
	private String name;
	private boolean isProtected;
	private Long timeAdded;
	private String whoAdd;
	
	public WhiteListedPlayer(String name, boolean isProtected, Long timeAdded, String whoAdd) {
		this.name = name;
		this.isProtected = isProtected;
		this.timeAdded = timeAdded;
		this.whoAdd = whoAdd;
	}
	
	public String getName() {
		return name;
	}
	public boolean isProtected() {
		return isProtected;
	}
	public Long getTimeAdded() {
		return timeAdded;
	}
	public String whoAdd() {
		return whoAdd;
	}
	
	public void setProtected(boolean b) {
		this.isProtected = b;
	}
	
	
	@Override
	public int compareTo(WhiteListedPlayer arg0) {
	     int result;
	     long time = arg0.getTimeAdded() - this.timeAdded;
	     if (time > 0) {
	    	 result = 1;
	     }
	     else {
	    	 result = -1;
	     }
	     return result;

	}
}
