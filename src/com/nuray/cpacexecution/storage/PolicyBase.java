package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.enforcementfunctions.Policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyBase {

    private List<Policy> policyList;
    Map<String, Policy> policyMap;

    public PolicyBase()
    {
        policyList=new ArrayList<>();
        policyMap=new HashMap<>();
    }

    public void addPolicy(Policy policy)
    {
        policyList.add(policy);
        policyMap.put(policy.getPolicyID(),policy);

    }

    public void deletePolicy(Policy policy) throws Exception {
        if(policyList.contains(policy))
        {
            policyList.remove(policy);
            policyMap.remove(policy.getPolicyID());
        }
        else
        {
            throw new Exception("Action cannot be removed since it is not in the list!");
        }

    }

    public Policy getPolicy(Policy policy)
    {
        if (policyList.contains(policy))
        {
            return policy;
        }
        return null;
    }

    public List<Policy> getPolicyList()
    {
        return policyList;
    }
}
