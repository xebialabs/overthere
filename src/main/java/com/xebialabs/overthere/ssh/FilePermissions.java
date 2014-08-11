package com.xebialabs.overthere.ssh;

import java.util.List;

public class FilePermissions {
    private final String owner;
    private String group;
    private int mask;

    public FilePermissions(String owner, String group, String fromLs) {
        this.owner = owner;
        this.group = group;
        addMask(fromLs, 1, 'r', 0x100);
        addMask(fromLs, 2, 'w', 0x80);
        addMask(fromLs, 3, 'x', 0x40);
        addMask(fromLs, 3, 's', 0x840);
        addMask(fromLs, 4, 'r', 0x20);
        addMask(fromLs, 5, 'w', 0x10);
        addMask(fromLs, 6, 'x', 0x8);
        addMask(fromLs, 6, 's', 0x408);
        addMask(fromLs, 7, 'r', 0x4);
        addMask(fromLs, 8, 'w', 0x2);
        addMask(fromLs, 9, 'x', 0x1);
        addMask(fromLs, 9, 't', 0x201);
    }

    public boolean canRead(String user, List<String> groups) {
        int permMask = 0x4;
        if (this.owner.equals(user)) {
            permMask |= 0x100;
        }
        if (groups.contains(group)) {
            permMask |= 0x20;
        }
        return (mask & permMask) > 0;
    }

    public boolean canWrite(String user, List<String> groups) {
        int permMask = 0x2;
        if (this.owner.equals(user)) {
            permMask |= 0x80;
        }
        if (groups.contains(group)) {
            permMask |= 0x10;
        }
        return (mask & permMask) > 0;
    }

    public boolean canExecute(String user, List<String> groups) {
        int permMask = 0x1;
        if (this.owner.equals(user)) {
            permMask |= 0x40;
        }
        if (groups.contains(group)) {
            permMask |= 0x8;
        }
        return (mask & permMask) > 0;
    }

    private void addMask(String ls, int i, char expected, int add) {
        if (ls.charAt(i) == expected) {
            mask |= add;
        }
    }
}
