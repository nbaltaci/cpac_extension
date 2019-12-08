package com.nuray.cpacexecution.cpacmodel;

import java.util.*;

public class Action {

    private List<Attribute> actionAttributes;
    private Set<Attribute> actionAttributeSet;
    /*
    note that 'actionId' is not the same as 'action_id' attribute.
    'actionId' is an int that is simply used to identify the count of attribute whereas
    'action_id' is a string that is used to describe the action itself (read, write etc.)
     */
    private int actionId;
    private String actionID;
    private String type;

    private Attribute actionName;


    /**
     *
     * @param actionID: This basically represents the name of the action. An example for a obj-oriented action: "read"
     * @param type: This corresponds to the type of action. Valid options are: "object-oriented", "cyber-physical".
     *           (see CPACSpecifications.java)
     */
    public Action(String actionID, String type) throws Exception {
        if(!CPACSpecifications.actionTypes.contains(type))
        {
            throw new IllegalArgumentException("Type should be one of the values specified in" +
                    " \"CPACSpecifications.java\" file, i.e. object-oriented or cyber-physical: "+ type);
        }

        this.actionID=actionID;
        this.type=type;
        actionId++;

        actionName=new Attribute("actionID",null,"categorical");
        actionName.setAttributeValueCategorical(actionID);

        actionAttributeSet=new HashSet<>();

//        actionAttributes=new ArrayList<>(Arrays.asList(new Attribute[]{actionName}));

        actionAttributes=new ArrayList<>();
        addActionAttribute(actionName);
    }

    @Override
    public boolean equals(Object obj)
    {
        Action other = (Action) obj;
        return other.actionID.equalsIgnoreCase(this.actionID);
    }

    public String getActionID()
    {
        return actionID;
    }
    public int getActionId() {
        return actionId;
    }
    public String getType()
    {
        return type;
    }

    public List<Attribute> getActionAttributes()
    {
        return actionAttributes;
    }

    public void addActionAttribute(Attribute attribute)
    {
        actionAttributes.add(attribute);
        actionAttributeSet.add(attribute);
    }

    public boolean hasAttribute(Attribute attribute)
    {
        return actionAttributeSet.contains(attribute);
    }

}
