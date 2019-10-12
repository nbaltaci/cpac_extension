package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Agent;

public class InitialAccessRequest {

    private Permission permission;
    private Agent agent;

    public InitialAccessRequest(Permission permission, Agent agent)
    {
        this.permission=permission;
        this.agent=agent;
    }

    public Permission getPermission() {
        return permission;
    }
    public Agent getAgent() {
        return agent;
    }
}
