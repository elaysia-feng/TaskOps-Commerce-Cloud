package com.elias.task.membership;

public enum MembershipLevel {
    FREE(20),
    VIP(50),
    SVIP(100);

    private final int maxTasks;

    MembershipLevel(int maxTasks) {
        this.maxTasks = maxTasks;
    }

    public int maxTasks() {
        return maxTasks;
    }
}
