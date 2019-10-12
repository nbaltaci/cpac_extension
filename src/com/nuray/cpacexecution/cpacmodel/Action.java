package com.nuray.cpacexecution.cpacmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action {

    private List<Attribute> actionAttributes;
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
    public Action(String actionID, String type)
    {
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
        actionAttributes=new ArrayList<>(Arrays.asList(new Attribute[]{actionName}));
    }

    @Override
    public boolean equals(Object obj)
    {
        Action other = (Action) obj;
        return other.actionId==this.actionId;
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
}
