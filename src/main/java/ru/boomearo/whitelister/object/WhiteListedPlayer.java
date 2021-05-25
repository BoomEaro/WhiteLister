package ru.boomearo.whitelister.object;

public class WhiteListedPlayer implements Comparable<WhiteListedPlayer> {

    private final String name;
    private boolean isProtected;
    private final long timeAdded;
    private final String whoAdd;

    public WhiteListedPlayer(String name, boolean isProtected, long timeAdded, String whoAdd) {
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

    public long getTimeAdded() {
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
        int r = Boolean.compare(arg0.isProtected(), this.isProtected);
        if (r == 0) {
            r = Long.compare(arg0.getTimeAdded(), this.timeAdded);
            if (r == 0) {
                r = this.name.compareTo(arg0.getName());
            }
        }
        return r;
    }
}
