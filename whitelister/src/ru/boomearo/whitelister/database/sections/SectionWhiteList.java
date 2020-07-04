package ru.boomearo.whitelister.database.sections;

public class SectionWhiteList {
	public int id;
	public String name;
	public boolean isProtected;
	public Long timeAdded;
	public String whoAdd;
	public SectionWhiteList(int id, String name, boolean isProtected, Long timeAdded, String whoAdd) {
		this.id = id;
		this.name = name;
		this.isProtected = isProtected;
		this.timeAdded = timeAdded;
		this.whoAdd = whoAdd;
	}
}
