package ru.boomearo.whitelister.object;

public class WhiteListedPlayer implements Comparable<WhiteListedPlayer> {

    private final String name;
    private boolean protect;
    private final long timeAdded;
    private final String whoAdd;

    public WhiteListedPlayer(String name, boolean isProtected, long timeAdded, String whoAdd) {
        this.name = name;
        this.protect = isProtected;
        this.timeAdded = timeAdded;
        this.whoAdd = whoAdd;
    }

    public String getName() {
        return this.name;
    }

    public boolean isProtect() {
        return this.protect;
    }

    public long getTimeAdded() {
        return this.timeAdded;
    }

    public String getWhoAdd() {
        return whoAdd;
    }

    public void setProtect(boolean protect) {
        this.protect = protect;
    }

    @Override
    public int compareTo(WhiteListedPlayer arg0) {
        int r = Boolean.compare(arg0.isProtect(), this.protect);
        if (r == 0) {
            r = Long.compare(arg0.getTimeAdded(), this.timeAdded);
            if (r == 0) {
                r = this.name.compareTo(arg0.getName());
            }
        }
        return r;
    }
}
