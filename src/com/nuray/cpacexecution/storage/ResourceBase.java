package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.Resource;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<Resource> getResourcesWithAttributeValue(Attribute attribute) throws Exception {

        List<Resource> result=new LinkedList<>();
        Iterator<Resource> resourceIterator = resourceList.iterator();

        if(attribute.isValueSet())
        {
            return resourceMap.values()
                    .stream()
                    .filter(resource -> resource.hasAttribute(attribute))
                    .collect(Collectors.toList());
        }
//        while (resourceIterator.hasNext())
//        {
//            Resource resource = resourceIterator.next();
//
//            List<Attribute> resourceAttributes=resource.getResourceAttributes();
//
//            for (Attribute resourceAtt:resourceAttributes)
//            {
//                if(resourceAtt.getAttributeName().equalsIgnoreCase(attribute.getAttributeName()))
//                {
//                    if(resourceAtt.isValueSet())
//                    {
//                        if(resourceAtt.getAttributeType().equalsIgnoreCase("categorical"))
//                        {
//                            if(resourceAtt.getAttributeValueCategorical().equalsIgnoreCase(attribute.getAttributeValueCategorical()))
//                            {
//                                result.add(resource);
//                            }
//                        }
//                        if(resourceAtt.getAttributeType().equalsIgnoreCase("numeric"))
//                        {
//                            if(resourceAtt.getAttributeValueNumeric()==attribute.getAttributeValueNumeric())
//                            {
//                                result.add(resource);
//                            }
//                        }
//                    }
//                }
//            }
//        }
        return result;
    }
}
