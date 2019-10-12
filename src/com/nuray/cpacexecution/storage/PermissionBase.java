package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.enforcementfunctions.Permission;

import java.util.ArrayList;
import java.util.List;

public class PermissionBase {

    private List<Permission> permissionList;

    public PermissionBase()
    {
        permissionList=new ArrayList<>();
    }

    public void addPermission(Permission permission)
    {
        permissionList.add(permission);
    }

    public void deletePermission(Permission permission) throws Exception {
        if(permissionList.contains(permission))
        {
            permissionList.remove(permission);
        }
        else
        {
            throw new Exception("Permission cannot be removed since it is not in the list!");
        }

    }

    public Permission getPermission(Permission permission)
    {
        if (permissionList.contains(permission))
        {
            return permission;
        }
        return null;
    }

    public List<Permission> getPermissionList()
    {
        return permissionList;
    }

}
