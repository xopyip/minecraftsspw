package tech.mateuszbaluch.minecraftsspw.launcher;

public enum OSType {
    OSX, WINDOWS, LINUX;

    public String getText() {
        return this.name().toLowerCase();
    }
}
