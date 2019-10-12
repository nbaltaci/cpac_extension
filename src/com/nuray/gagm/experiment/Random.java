package com.nuray.gagm.experiment;


import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.AttributeRanges;
import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;

import java.util.*;

/**
 * Created by TOSHIBA on 1/2/2017.
 */
public class Random {
    private int upperLimit, lowerLimit;
//    private double fraction;  // for the fraction of critical edges in a graph
    private int agentAttValueCounter;  // for generating random attribute values
    private int resourceAttValueCounter;  // for generating random attribute values
    private int actionAttValueCounter;  // for generating random attribute values

    public Random()
    {

    }

    public Random(int lowerLimit, int upperLimit)
    {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        agentAttValueCounter=0;
        resourceAttValueCounter=0;
        actionAttValueCounter=0;
    }

    public int generateRandomWeight()
    {
        return (int)(Math.random()*((upperLimit-lowerLimit)+1)) + lowerLimit;
    }

    public int generateRandomIndex()
    {
        return (int)(Math.random()*((upperLimit-lowerLimit)+1));
    }


    public String generateRandomEdgeType(double fraction)
    {
        // if randomly generated boolean value is true, assign the edge "critical" type, otherwise assign "action" type
        return getRandomBooleanWithFraction(fraction)==true? "critical" : "action";
    }

    /**
     *
     * @param fraction: for the fraction of critical edges in a graph
     * @return
     */
    private boolean getRandomBooleanWithFraction(double fraction) {

        // fraction==> this is the % time that a "true" value is generated. e.g. if it is 0.5, the
        // values generated by Math.random() method will be less than 0.5 50% of the time.

        return Math.random() < fraction;
    }

    private boolean getRandomBoolean() {

        int randNum= (int) Math.round(Math.random());
        if(randNum==1)
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    public VirtualAccessRequest generateRandomVar(ActionBase actionBase, String operationalMode) throws Exception {

        // generate agent attributes
        List<Attribute> agentAttList=new LinkedList<Attribute>();

        Attribute userName=new Attribute("agentID",null,"categorical");
        Attribute email=new Attribute("email",null,"categorical");
        Attribute role=new Attribute("role",null,"categorical");
        Attribute age=new Attribute("age",new AttributeRanges(15.0,85.00),"numeric");

        agentAttList.addAll(Arrays.asList(new Attribute[]{userName,email,role,age}));
//        userName.setAttributeValueCategorical("agent");

        boolean attExistenceFlag=false;

        for(int i=0; i<agentAttList.size();i++)
        {
            attExistenceFlag=getRandomBoolean();
            if (attExistenceFlag==true)
            {
                Attribute attribute = agentAttList.get(i);
                agentAttValueCounter++;
                if(attribute.getAttributeType().equalsIgnoreCase("categorical"))
                {
                    attribute.setAttributeValueCategorical("value"+agentAttValueCounter);
                }
                if(attribute.getAttributeType().equalsIgnoreCase("numeric"))
                {
                    Random random = new Random(0, 100);
                    attribute.setAttributeValueNumeric(Math.random()*((upperLimit-lowerLimit)+1));
                }
            }
        }

        // generate resource attributes
        List<Attribute> resourceAttList=new LinkedList<Attribute>();

        Attribute resourceName=new Attribute("resourceID",null,"categorical");

        resourceAttList.addAll(Arrays.asList(new Attribute[]{resourceName}));

        resourceAttValueCounter++;

        resourceAttList.get(0).setAttributeValueCategorical("value"+resourceAttValueCounter);

        // generate action & action attributes (here I assume a single action)
        List<Attribute> actionAttList=new LinkedList<Attribute>();

        Attribute actionName=new Attribute("actionID",null,"categorical");

        actionAttList.addAll(Arrays.asList(new Attribute[]{actionName}));

        actionAttValueCounter++;

        Attribute actionAtt=actionAttList.get(0);

        actionAtt.setAttributeValueCategorical("value"+actionAttValueCounter);

        Map<Action,List<Attribute>> actionToAttMap=new HashMap<>();

        //generate a random action type (from these options: human, cyber, physical)
        String actionType=generateActionType();

        Action action=new Action(actionAtt.getAttributeValueCategorical(),actionType);
        actionBase.addAction(action);
        actionToAttMap.put(action,actionAttList);

        // generate var
        VirtualAccessRequest virtualAccessRequest=new VirtualAccessRequest(agentAttList,resourceAttList,actionToAttMap,operationalMode);

        return virtualAccessRequest;
    }

    /**
     *
     * @param nAgentAtt : number of agent attributes to generate synthetically, note that these are not real attributes defined
     *                  in "Agent" class in "CPACExecution" module.
     * @param nResAtt : number of resource attributes to generate synthetically, note that these are not real attributes defined
     *      *                  in "Agent" class in "CPACExecution" module.
     * @param nActionAtt : number of action attributes to generate synthetically, note that these are not real attributes defined
     *      *                  in "Agent" class in "CPACExecution" module.
     * @param actionBase
     * @param operationalMode
     * @return
     */
    public VirtualAccessRequest generateRandomVar(int nAgentAtt, int nResAtt, int nActionAtt, int nActions,
                                                  ActionBase actionBase, String operationalMode) throws Exception {
        // generate agent attributes
        List<Attribute> agentAttList=generateAttsForVar(nAgentAtt);

        // generate resource attributes
        List<Attribute> resourceAttList=generateAttsForVar(nResAtt);

        //generate action attributes
        Map<Action,List<Attribute>> actionToAttMap=generateActionsAndActionAttributes(nActionAtt,nActions,actionBase);

        VirtualAccessRequest virtualAccessRequest=new VirtualAccessRequest(agentAttList,resourceAttList,actionToAttMap,operationalMode);

        return virtualAccessRequest;
    }


    private Map<Action,List<Attribute>> generateActionsAndActionAttributes(int nAttributes, int nActions,
                                                                           ActionBase actionBase) throws Exception {

        Map<Action,List<Attribute>> actionToAttMap=new HashMap<>();

        for(int i=0;i<nActions;i++)
        {
            // generate action attributes
            List<Attribute> attList=generateAttsForVar(nAttributes);

            //generate a random action type (from these options: object-oriented or cyber-physical)
            String actionType=generateActionType();

            Action action=new Action("action"+(i+1),actionType);
            actionBase.addAction(action);
            actionToAttMap.put(action,attList);
        }
        return actionToAttMap;
    }

    private List<Attribute> generateAttsForVar(int nAttributes)
    {
        // generate resource attributes
        List<Attribute> attList=new LinkedList<Attribute>();

        for(int i=0; i<nAttributes; i++)
        {
            Attribute attribute=new Attribute("attribute"+(i+1),null,"categorical");
            attribute.setAttributeValueCategorical("value"+(i+1));
            attList.add(attribute);
        }

        return attList;
    }


    private String generateActionType()
    {
        //generate a random action type (from these options: "object-oriented" or"cyber-physical)
//        boolean randomBoolean = getRandomBoolean();
//        String actionType="";
//
//        if(randomBoolean==true)
//        {
//            actionType="object-oriented";
//        }
//        else
//        {
//            actionType="cyber-physical";
//        }

        return getRandomBoolean() ? "object-oriented":"cyber-physical";
//        return actionType;
    }
}