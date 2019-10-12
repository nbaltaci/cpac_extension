package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.cpacmodel.Attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AgentBase {

    private List<Agent> agentList;

    public AgentBase()
    {
        agentList=new ArrayList<>();
    }

    public void addAgent(Agent agent)
    {
        agentList.add(agent);
    }

    public void deleteAgent(Agent agent) throws Exception {
        if (agentList.contains(agent))
        {
            agentList.remove(agent);
        }
        else
        {
            throw new Exception("Resource cannot be removed since it is not in the list!");
        }
    }

    public Agent getAgent(Agent agent)
    {
        if (agentList.contains(agent))
        {
            return agent;
        }
        else
        {
            return null;
        }
    }

    public List<Agent> getAgentList()
    {
        return agentList;
    }

    public List<Agent> getAgentsWithAttributeValue(Attribute attribute) throws Exception {

        List<Agent> agentList=new ArrayList<>();

        Iterator<Agent> agentIterator = agentList.iterator();

        while (agentIterator.hasNext())
        {
            Agent agent = agentIterator.next();

            List<Attribute> agentAttributes = agent.getAgentAttributes();

            if(agentAttributes.contains(attribute))
            {
                int index = agentAttributes.indexOf(attribute);
                Attribute agentAttribute = agentAttributes.get(index);
                if(agentAttribute.getAttributeType().equalsIgnoreCase("categorical"))
                {
                    if(agentAttribute.getAttributeValueCategorical().equalsIgnoreCase(attribute.getAttributeValueCategorical()))
                    {
                        agentList.add(agent);
                    }
                }
                if(agentAttribute.getAttributeType().equalsIgnoreCase("numerical"))
                {
                    if(agentAttribute.getAttributeValueNumeric()==attribute.getAttributeValueNumeric())
                    {
                        agentList.add(agent);
                    }
                }

            }
        }
        return agentList;
    }


}
