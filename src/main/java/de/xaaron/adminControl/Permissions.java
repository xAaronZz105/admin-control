package de.xaaron.adminControl;

public enum Permissions {
    VANISH_BASE ("vanish.base"),
    VANISH_SELF ("vanish.self"),
    VANISH_OTHER ("vanish.other"),

    ADMINCONTROL_BASE ("admincontrol.base"),
    ADMINCONTROL_CONFIG ("admincontrol.config");

    private final String permissionPath;
    Permissions(String permissionPath) {
        this.permissionPath = permissionPath;
    }

    public String getPermission() {
        return Main.instance.getConfig().getString("permissions." + permissionPath);
    }
}
