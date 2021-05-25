package ru.boomearo.whitelister.database.sections;

public class SectionWhiteList {
    public final int id;
    public final String name;
    public final boolean isProtected;
    public final Long timeAdded;
    public final String whoAdd;

    public SectionWhiteList(int id, String name, boolean isProtected, Long timeAdded, String whoAdd) {
        this.id = id;
        this.name = name;
        this.isProtected = isProtected;
        this.timeAdded = timeAdded;
        this.whoAdd = whoAdd;
    }
}
