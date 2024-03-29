package com.nuray.cpacexecution.cpacmodel;

import java.util.*;

public class Resource {
    private List<Attribute> resourceAttributes;
    private Set<Attribute> resourceAttributeSet;

    /*
    note that 'resourceId' is not the same as 'resource_id' attribute.
    'resourceId' is an int that is simply used to identify the count of attribute whereas
    'resource_id' is a string that is used to describe the resource itself (machine1, file2 etc.)
     */
    private int resourceId;
    private String resourceID;
    private String type;

    private Attribute resourceName;

    /**
     *
     * @param resourceID: This basically represents the name of the resource. An example for a cyber resource
     *                  is a URL such as: "http://127.0.0.1/service/very_secure/"
     * @param type: This corresponds to the type of resource. Valid options are: "human", "physical", and "cyber".
     *      *           (see CPACSpecifications.java)
     */
    public Resource(String resourceID, String type) throws Exception {
        if(!CPACSpecifications.elementTypes.contains(type))
        {
            throw new IllegalArgumentException("Type should be one of the values specified in" +
                    " \"CPACSpecifications.java\" file, i.e. human, physical, or cyber"+ type);
        }

        this.resourceID=resourceID;
        this.type=type;
        resourceId++;

        resourceName=new Attribute("resourceID",null,"categorical");
        resourceName.setAttributeValueCategorical(resourceID);

        resourceAttributeSet=new HashSet<>();

//        resourceAttributes=new ArrayList<>(Arrays.asList(new Attribute[]{resourceName}));

        resourceAttributes=new ArrayList<>();
        addResourceAttribute(resourceName);
    }

    @Override
    public boolean equals(Object obj)
    {
        Resource other = (Resource) obj;
        return other.resourceID.equalsIgnoreCase(this.resourceID);
    }

    public String getResourceID()
    {
        return resourceID;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getType()
    {
        return type;
    }

    public Attribute getResourceName()
    {
        return resourceName;
    }

    public List<Attribute> getResourceAttributes() {
        return resourceAttributes;
    }

    public void setResourceName(Attribute resourceName)
    {
        this.resourceName = resourceName;
    }

    public void addResourceAttribute(Attribute attribute)
    {
        resourceAttributes.add(attribute);
        resourceAttributeSet.add(attribute);
    }

    public boolean hasAttribute(Attribute attribute)
    {
        return resourceAttributeSet.contains(attribute);
    }
}
