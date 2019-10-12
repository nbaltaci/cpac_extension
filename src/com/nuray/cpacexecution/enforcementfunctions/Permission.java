package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Resource;

import java.util.List;

public class Permission {

    private int permissionId;
    private Resource resource;
//    private String resourceID; //'resource_id' is a string that is used to describe the the resource itself (machine1, file2 etc.)
    private List<Action> actionList; //'action_id' is a string that is used to describe the action itself (read, write etc.)

    public Permission(Resource resource, List<Action> actionList)
    {
        this.resource=resource;
        this.actionList=actionList;
        permissionId++;
    }

    @Override
    public boolean equals(Object obj)
    {
        Permission other = (Permission) obj;
        return other.permissionId==this.permissionId;
    }

    public int getPermissionId() {
        return permissionId;
    }

    public Resource getResource() {
        return resource;
    }

    public List<Action> getActionList() {
        return actionList;
    }
}
