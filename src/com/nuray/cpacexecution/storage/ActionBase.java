package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Attribute;

import java.util.*;
import java.util.stream.Collectors;

public class ActionBase {

    private List<Action> actionList;
    Map<String, Action> actionMap;

    public ActionBase()
    {
        actionList=new ArrayList<>();
        actionMap=new HashMap<>();
    }

    public void addAction(Action action)
    {
        actionList.add(action);
        actionMap.put(action.getActionID(),action);
    }

    public void deleteAction(Action action) throws Exception {
        if(actionList.contains(action))
        {
            actionList.remove(action);
            actionMap.remove(action.getActionID());
        }
        else
        {
            throw new Exception("Action cannot be removed since it is not in the list!");
        }

    }

    public Action getAction(Action action)
    {
        if (actionList.contains(action))
        {
            return action;
        }
        return null;
    }

    public Action getActionWithActionID(String actionID)
    {

        return actionMap.get(actionID);
    }

    public List<Action> getActionList()
    {
        return actionList;
    }

    public List<Action> getActionsWithAttributeValue(Attribute attribute) throws Exception {

        List<Action> result=new LinkedList<>();
        Iterator<Action> actionIterator = actionList.iterator();

        if(attribute.isValueSet())
        {
            return actionMap.values()
                    .stream()
                    .filter(action -> action.hasAttribute(attribute))
                    .collect(Collectors.toList());
        }

//        while (actionIterator.hasNext())
//        {
//            Action action = actionIterator.next();
//
//            List<Attribute> actionAttributes=action.getActionAttributes();
//
//            for (Attribute actionAtt:actionAttributes)
//            {
//                if(actionAtt.getAttributeName().equalsIgnoreCase(attribute.getAttributeName()))
//                {
//                    if(actionAtt.isValueSet())
//                    {
//                        if(actionAtt.getAttributeType().equalsIgnoreCase("categorical"))
//                        {
//                            if(actionAtt.getAttributeValueCategorical().equalsIgnoreCase(attribute.getAttributeValueCategorical()))
//                            {
//                                result.add(action);
//                            }
//                        }
//                        if(actionAtt.getAttributeType().equalsIgnoreCase("numeric"))
//                        {
//                            if(actionAtt.getAttributeValueNumeric()==attribute.getAttributeValueNumeric())
//                            {
//                                result.add(action);
//                            }
//                        }
//                    }
//                }
//            }
//        }
        return result;
    }

}
