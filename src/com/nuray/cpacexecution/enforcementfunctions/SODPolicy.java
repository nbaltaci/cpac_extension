package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Resource;

import java.util.LinkedList;
import java.util.List;

public class SODPolicy {
    private int sodPolicyId;
    private List<SODPolicyRule> sodPolicyRules;

    public SODPolicy()
    {
        sodPolicyRules=new LinkedList<>();
        sodPolicyId++;
    }

    @Override
    public boolean equals(Object obj)
    {
        SODPolicy other = (SODPolicy) obj;
        return other.sodPolicyId==this.sodPolicyId;
    }

    public void addSoDPolicyRule(SODPolicyRule sodPolicyRule)
    {
        sodPolicyRules.add(sodPolicyRule);
    }

    public List<SODPolicyRule> getSodPolicyRules()
    {
        return sodPolicyRules;
    }

    public void setSodPolicyRules(List<SODPolicyRule> sodPolicyRules)
    {
        this.sodPolicyRules = sodPolicyRules;
    }
}
