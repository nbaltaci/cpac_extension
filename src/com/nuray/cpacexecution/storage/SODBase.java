package com.nuray.cpacexecution.storage;

import com.nuray.cpacexecution.enforcementfunctions.SODPolicy;

import java.util.ArrayList;
import java.util.List;

public class SODBase {

    private List<SODPolicy> sodPolicies;

    public SODBase()
    {
        sodPolicies=new ArrayList<>();
    }

    public void addSODPolicy(SODPolicy sodPolicy)
    {
        sodPolicies.add(sodPolicy);
    }

    public void deleteSODPolicy(SODPolicy sodPolicy) throws Exception
    {
        if(sodPolicies.contains(sodPolicy))
        {
            sodPolicies.remove(sodPolicy);
        }
        else
        {
            throw new Exception("SoD policy cannot be removed since it is not in the list!");
        }
    }

    public SODPolicy getSODPolicy(SODPolicy sodPolicy)
    {
        if (sodPolicies.contains(sodPolicy))
        {
            return sodPolicy;
        }
        return null;
    }



    public List<SODPolicy> getSODPolicyList()
    {
        return sodPolicies;
    }
}
