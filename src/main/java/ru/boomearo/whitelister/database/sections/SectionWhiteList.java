package ru.boomearo.whitelister.database.sections;

public class SectionWhiteList {
    private final int id;
    private final String name;
    private final boolean isProtected;
    private final Long timeAdded;
    private final String whoAdd;

    public SectionWhiteList(int id, String name, boolean isProtected, Long timeAdded, String whoAdd) {
        this.id = id;
        this.name = name;
        this.isProtected = isProtected;
        this.timeAdded = timeAdded;
        this.whoAdd = whoAdd;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isProtected() {
        return this.isProtected;
    }

    public Long getTimeAdded() {
        return this.timeAdded;
    }

    public String getWhoAdd() {
        return this.whoAdd;
    }
}
