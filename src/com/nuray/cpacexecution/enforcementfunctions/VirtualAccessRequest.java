package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.Resource;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.cpacexecution.storage.ResourceBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VirtualAccessRequest extends Request {
    private final static String RESOURCE_LINE="<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">";
    private final static String ACTION_LINE="<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">";


    public VirtualAccessRequest(List<Attribute> agentAtts, List<Attribute> resourceAtts,
                                Map<Action,List<Attribute>> actionAtts, String operationalMode) throws Exception {
        super(operationalMode);

        this.agentAttributes=agentAtts;
        this.resourceAttributes=resourceAtts;
        this.actionAttributes=actionAtts;
    }



    public Permission getPermission(ResourceBase resourceBase, ActionBase actionBase) {
        this.permission=extractPermissionFromRequestXACML(getRequestContentXACML(),resourceBase,actionBase);
        return permission;
    }

    public Permission extractPermissionFromRequestXACML(String requestContentXACML,
                                                        ResourceBase resourceBase,
                                                        ActionBase actionBase)
    {
        String resourceID=null;
        List<Action> actionList=new ArrayList<>();
        Scanner sc=new Scanner(requestContentXACML);

        while (sc.hasNext())
        {
            String line=sc.next();

            String attributeIDString="AttributeId";
            String resourceIDString="resource-id";
            String actionIDString="action-id";

            if(line.equalsIgnoreCase(RESOURCE_LINE))
            {
                line=sc.next();

                //check if the next line is a resource-id attribute
                if ((line.indexOf(attributeIDString)!=-1) && (line.indexOf(resourceIDString)!=-1))
                {
                    line=sc.next();
                    int endIndexOfResourceID=line.indexOf("</AttributeValue>");
                    int startIndexOfResourceID=line.indexOf(">")+1;
                    resourceID=line.substring(startIndexOfResourceID,endIndexOfResourceID);

                }
            }
            if(line.equalsIgnoreCase(ACTION_LINE))
            {
                line=sc.next();

                //check if the next line is an action-id attribute
                if( (line.indexOf(attributeIDString)!=-1) && (line.indexOf(actionIDString)!=-1) )
                {
                    int endIndexOfActionID=line.indexOf("</AttributeValue>");
                    int startIndexOfActionID=line.indexOf(">")+1;
                    String actionID=line.substring(startIndexOfActionID,endIndexOfActionID);
                    Action actionMatched = actionBase.getActionWithActionID(actionID);
                    actionList.add(actionMatched);
                }
            }
        }

        //retrieve the permission from the PermissionBase
        Resource resourceMatched = resourceBase.getResourceWithResourceID(resourceID);

        return new Permission(resourceMatched,actionList);
    }
}
