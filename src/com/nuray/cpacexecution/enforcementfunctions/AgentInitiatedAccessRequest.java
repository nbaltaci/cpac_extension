package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.Resource;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.cpacexecution.storage.AgentBase;
import com.nuray.cpacexecution.storage.ResourceBase;

import java.util.List;

public class AgentInitiatedAccessRequest extends Request {
    private Agent agent;
    private InitialAccessRequest iar;

    public AgentInitiatedAccessRequest(InitialAccessRequest iar, String operationalMode, ResourceBase resourceBase,
                                       ActionBase actionBase, AgentBase agentBase) throws Exception {
        super(operationalMode);
        this.iar=iar;
        this.permission=iar.getPermission();
        this.agent=iar.getAgent();

        enhanceIAR(resourceBase,actionBase,agentBase);

        requestContentXACML=constructXACMLRequest();

    }

    public InitialAccessRequest getIar()
    {
        return iar;
    }

    /**
     * This function enhances an IAR (initial access request submitted by agent to the AC system)
     * to an AAR (agent-initiated requested, which is a well-defined access request, enriched with
     * attributes of the agent, resource, action_ID etc.)
     * @param resourceBase
     * @param actionBase
     * @param agentBase
     * @throws Exception
     */
    public void enhanceIAR(ResourceBase resourceBase, ActionBase actionBase, AgentBase agentBase) throws Exception
    {
        Resource resource = permission.getResource();
        Resource resourceMatched = resourceBase.getResource(resource);
        if(resourceMatched!=null)
        {
            resourceAttributes = resourceMatched.getResourceAttributes();
        }

        List<Action> actionList=permission.getActionList();
        for (Action action:actionList)
        {
            Action actionMatched= actionBase.getAction(action);
            if(actionMatched==null)
            {
                throw new Exception("There is no such action defined, so it cannot be requested: "+action);
            }
            List<Attribute> actionAttList=actionMatched.getActionAttributes();

            actionAttributes.put(actionMatched,actionAttList);
        }

        Agent agentMatched = agentBase.getAgent(this.agent);
        if(agentMatched!=null)
        {
            agentAttributes = agentMatched.getAgentAttributes();
        }
    }

}
