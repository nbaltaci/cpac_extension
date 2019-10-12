package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.cpacmodel.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
