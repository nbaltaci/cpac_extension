package com.nuray.cpacexecution.enforcementfunctions;

import java.util.List;

public class SODPolicyRule {
    private List<Permission> Perm;
    private int k; // this is # agents needed for permissions in Perm to fulfill a task. (Remember: sod=< Perm,k >)
    private double riskOfSoDPolicy;

    /**
     *
     * @param Perm
     * @param k: this is # agents needed for permissions in Perm to fulfill a task. (Remember: sod=< Perm,k >)
     */
    public SODPolicyRule(List<Permission> Perm, int k, double riskOfSoDPolicy)
    {
        this.Perm=Perm;
        this.k=k;
        this.riskOfSoDPolicy=riskOfSoDPolicy;
    }

//    @Override
//    public boolean equals(Object obj)
//    {
//        SODPolicyRule another=(SODPolicyRule) obj;
//        return another.getSodPolicyId()==((SODPolicyRule) obj).getSodPolicyId();
//    }

    public List<Permission> getPerm()
    {
        return Perm;
    }

    public int getK()
    {
        return k;
    }

    public double getRiskOfSoDPolicy()
    {
        return riskOfSoDPolicy;
    }
}
