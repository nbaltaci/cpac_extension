package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.cpacmodel.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceBase {
    private List<Resource> resourceList;
    Map<String, Resource> resourceMap;

    public ResourceBase()
    {
        resourceList=new ArrayList<>();
        resourceMap=new HashMap<>();
    }

    public void addResource(Resource resource)
    {
        resourceList.add(resource);
        resourceMap.put(resource.getResourceID(),resource);
    }

    public void deleteResource(Resource resource) throws Exception {
        if (resourceList.contains(resource))
        {
            resourceList.remove(resource);
            resourceMap.remove(resource.getResourceID());
        }
        else
        {
            throw new Exception("Resource cannot be removed since it is not in the list!");
        }
    }


    public Resource getResource(Resource resource)
    {
        if (resourceList.contains(resource))
        {
            return resource;
        }
        else
        {
            return null;
        }
    }

    public Resource getResourceWithResourceID(String resourceID)
    {

        return resourceMap.get(resourceID);
    }

    public List<Resource> getResourceList()
    {
        return resourceList;
    }
}
