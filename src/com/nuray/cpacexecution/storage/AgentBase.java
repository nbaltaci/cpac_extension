package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.cpacmodel.Attribute;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgentBase {

    private List<Agent> agentList;
    Map<String, Agent> agentMap;

    public AgentBase()
    {
        agentList=new ArrayList<>();
        agentMap=new HashMap<>();
    }

    public void addAgent(Agent agent)
    {
        agentList.add(agent);
        agentMap.put(agent.getAgentID(),agent);
    }

    public void deleteAgent(Agent agent) throws Exception {
        if (agentList.contains(agent))
        {
            agentList.remove(agent);
            agentMap.remove(agent.getAgentID());
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

        List<Agent> result=new LinkedList<>();
        Iterator<Agent> agentIterator = agentList.iterator();



        if(attribute.isValueSet())
        {
            return agentMap.values()
                    .stream()
                    .filter(agn -> agn.hasAttribute(attribute))
//                    .filter(agn -> agn.getAgentAttributes()
//                            .stream()
//                            .filter(att -> att.isValueSet())
//                            .filter(att -> att.getAttributeType().equalsIgnoreCase(attribute.getAttributeType()))
//                            .filter(att ->
//                            {
//                                boolean filterResult = false;
//                                try {
//                                    if (attribute.getAttributeType().equalsIgnoreCase("categorical")) {
//                                        filterResult = att.getAttributeValueCategorical().equalsIgnoreCase(attribute.getAttributeValueCategorical());
//                                    } else if (attribute.getAttributeType().equalsIgnoreCase("numeric")) {
//                                        filterResult = att.getAttributeValueNumeric() == attribute.getAttributeValueNumeric();
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                return filterResult;
//                            }).count() != 0
//
//                    )
                    .collect(Collectors.toList());
        }





//        while (agentIterator.hasNext())
//        {
//            Agent agent = agentIterator.next();
//
////            List<Attribute> agentAttributes = agent.getAgentAttributes();
//            Map<String, Attribute> agentAttributes = agent.getAgentAttributes();
//
//            for (Attribute agentAtt:agentAttributes)
//            {
//                if(agentAtt.getAttributeName().equalsIgnoreCase(attribute.getAttributeName()))
//                {
//                    if(agentAtt.isValueSet())
//                    {
//                        if(agentAtt.getAttributeType().equalsIgnoreCase("categorical"))
//                        {
//                            if(agentAtt.getAttributeValueCategorical().equalsIgnoreCase(attribute.getAttributeValueCategorical()))
//                            {
//                                result.add(agent);
//                            }
//                        }
//                        if(agentAtt.getAttributeType().equalsIgnoreCase("numeric"))
//                        {
//                            if(agentAtt.getAttributeValueNumeric()==attribute.getAttributeValueNumeric())
//                            {
//                                result.add(agent);
//                            }
//                        }
//                    }
//                }
//            }
        return result;
    }


}
