package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.storage.AgentBase;
import com.nuray.cpacexecution.storage.PolicyBase;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class SODPolicyRule {
    private List<Permission> Perm;
    private int k; // this is # agents needed for permissions in Perm to fulfill a task. (Remember: sod=< Perm,k >)
    private double riskOfSoDPolicy;

    private Set<MERConstraint> MERSet; // this is for improving the time performance of checking sod violations
    /*
            Please refer to following papers for detailed explanations of relevant concepts and algorithms:

                        [1] S.  Jha,  S.  Sural,  V.  Atluri,  and  J.  Vaidya,  “Enforcing  separation  of  duty  in
                          attribute  based access  control  systems,”in International Conference on Information Systems
                           Security.Springer, 2015, pp. 61–78
                        [2] Specification  and  verification  of  separation  of  dutyconstraints in attribute-based access
                         control,”IEEE Transactions  on  Information  Forensics  and  Security,  vol.  13,  no.  4,pp.
                         897–911, 2017.
     */


    /**
     *
     * @param Perm
     * @param k: this is # agents needed for permissions in Perm to fulfill a task. (Remember: sod=< Perm,k >)
     */
    public SODPolicyRule(List<Permission> Perm, int k, double riskOfSoDPolicy, PolicyBase policyBase,AgentBase agentBase)
    {
        this.Perm=Perm;
        this.k=k;
        this.riskOfSoDPolicy=riskOfSoDPolicy;
        MERSet=new HashSet<>();
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

    public Set<MERConstraint> getMERSet()
    {
        return MERSet;
    }

    public void setMERSet(Set<MERConstraint> merSet)
    {
        this.MERSet=merSet;
    }

    /*
        This method is used to transform an sod constraint to a mutually exclusive rule (MER) with the purpose of making
        sod verification an intractable process. Please refer to the following paper:
                    [1] S.  Jha,  S.  Sural,  V.  Atluri,  and  J.  Vaidya,  “Enforcing  separation  of  duty  in  attribute
                    based access  control  systems,”inInternational Conference on Information Systems Security.Springer, 2015,
                    pp. 61–78
         */
    public SoAPPolicyRule createSoAP(PolicyBase policyBase, AgentBase agentBase) throws Exception {
        /*
            1: for each t ∈ SoD do
                2: find relevant[t]
            3: end for
         */
        Set<PolicyRule> relevantPolicyRules=new HashSet<>();

        List<PolicyRule> policyRulesWithPermit = policyBase.getPolicyList()
                .stream()
                .flatMap(pol -> pol.getPolicyRules().stream())
                .filter(policyRule -> policyRule.getRuleEffect().equalsIgnoreCase("permit"))
                .collect(Collectors.toList());

        for (Permission p:Perm)
        {
            List<Agent> agents=agentBase.getAgentList();

            for (Agent agent:agents)
            {
                List<PolicyRule> relevantRulesforAgent = policyRulesWithPermit
                        .stream()
                        .filter(policyRule ->
                                policyRule.getAgentAttributes().values()
                                        .stream()
                                        .flatMap(agentAttributeGroup ->
                                                agentAttributeGroup.stream()
                                                        .flatMap(agentAttribute -> agentAttribute.keySet().stream()))
                                        .allMatch(policyRuleAttribute -> agent.hasAttribute(policyRuleAttribute)))
                        .filter(policyRule -> policyRule.getPermissionsFromRule().contains(p))
                        .collect(Collectors.toList());

                relevantPolicyRules.addAll(relevantRulesforAgent);
            }
        }

        /*
        5: SoAP ← CREATE MINIMAL SoAP(SoD, P, FU)
         */

        SoAPPolicyRule minimalSoAP = createMinimalSoAP(this, relevantPolicyRules, agentBase);
//        if(minimalSoAP!=null)
//        {
//            return minimalSoAP;
//        }
//        else
//        {
//            throw new Exception("SoD rule cannot be enforced!");
//        }
        return minimalSoAP;
    }

    private SoAPPolicyRule createMinimalSoAP(SODPolicyRule sodPolicyRule,Set<PolicyRule> reducedPolicyRuleSet,AgentBase agentBase)
    {
        /*
        1: for q → 0 to (2|P|-1) do
            2: policy set[q] ← null
            3: sod tset[q] ← null
            4: pset size[q] ← null
        5: end for
         */
        Set<Set<PolicyRule>> powerSet = powerSet(reducedPolicyRuleSet);
        powerSet.removeAll(Collections.singleton(Collections.emptySet()));

        List<Set<PolicyRule>> policy_set=new ArrayList();
        List<Set<Permission>> sod_tset=new ArrayList<>();
        List<Integer> pset_size=new ArrayList<>();

        /*
        6: q ← 0
        7: for each pset ∈ 2 |P| do
            8: policy set[q] ← pset
            9: pset size[q] ← |pset|
            10: sod tset[q] ← ∪p∈pset auth policy[p]
            11: q ← q + 1
        12: end for
         */

        for (Set<PolicyRule> policyRuleSet: powerSet) // 7: for each pset ∈ 2 |P| do
        {
            policy_set.add(policyRuleSet); // 8: policy set[q] ← pset
            pset_size.add(policyRuleSet.size()); //  9: pset size[q] ← |pset|

            Set<Permission> permissionsFromPolicyRuleSet=new HashSet<>();
            for (PolicyRule rule:policyRuleSet)
            {
                List<Permission> permissionsFromRule = rule.getPermissionsFromRule();
                permissionsFromPolicyRuleSet.addAll(new HashSet<>(permissionsFromRule));
            }
            sod_tset.add(permissionsFromPolicyRuleSet); // 10: sod tset[q] ← ∪p∈pset auth policy[p]
        }

        /*
        13: min ←∞
        14: for i → 0 to q do
            15: if (sod tset[q] {t1, t2,......, tn}) &&(pset size[q] < min) then
                16: min ← pset size[q]
                17: location ← q
            18: end if
        19: end for
         */

        int min=Integer.MAX_VALUE;
        int locationOfPolicySetToReturn=-1;
        for(int i=0; i<powerSet.size(); i++)
        {
            List<Permission> sod_tset_permissions=new ArrayList<>(sod_tset.get(i));
            if (sod_tset_permissions.containsAll(sodPolicyRule.getPerm()) && pset_size.get(i)<min)
            {
                min=pset_size.get(i);
                locationOfPolicySetToReturn=i;
            }
        }

        /*
        20: if (min >= k) then
            21: return <policy set[location], k>
        22: end if
         */

        if(min!=Integer.MAX_VALUE && min>=sodPolicyRule.getK())
        {
            SoAPPolicyRule minSoAP=new SoAPPolicyRule(policy_set.get(locationOfPolicySetToReturn),sodPolicyRule.getK());

            return minSoAP;
        }
        return null;
    }


    private <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
//            sets.add(set);
        }
        return sets;
    }

    public Set<MERConstraint> generateMER(SoAPPolicyRule soAPPolicyRule)
    {
        if(soAPPolicyRule==null)
        {
            return null;
        }
        Set<MERConstraint> resultToReturn=new HashSet<>();

        int k=soAPPolicyRule.k;
        Set<PolicyRule> policyRules=soAPPolicyRule.policyRuleSet;
        int n=policyRules.size();

        if(k==2)
        {
            resultToReturn.add(new MERConstraint(policyRules,n));
        }
        else if(k==n)
        {
            resultToReturn.add(new MERConstraint(policyRules,2));
        }
        else
        {
            List<PolicyRule> policyRuleList=new ArrayList<>();
            policyRuleList.addAll(policyRules);

            int m=n/k+1;
            m=k-1;
            List<Set<PolicyRule>> subsetsOfPolicyRules = getSubsets(policyRuleList, m);

            for (Set<PolicyRule> subset:subsetsOfPolicyRules)
            {
                resultToReturn.add(new MERConstraint(subset,m));
            }
        }

        return resultToReturn;
    }

    /*
        The following method is for finding the subsets of a set with a given length "l".
        Please refer to the following page for the code:
        https://stackoverflow.com/questions/12548312/find-all-subsets-of-length-k-in-an-array
     */
    private List<Set<PolicyRule>> getSubsets(List<PolicyRule> superSet, int l) {
        List<Set<PolicyRule>> result = new ArrayList<>();
        getSubsetsHelper(superSet, l, 0, new HashSet<>(), result);
        return result;
    }

    private void getSubsetsHelper(List<PolicyRule> superSet, int l, int idx,
                            Set<PolicyRule> current,List<Set<PolicyRule>> solution) {
        //successful stop clause
        if (current.size() == l)
        {
            solution.add(new HashSet<>(current));
            return;
        }
        //unsuccessful stop clause
        if (idx == superSet.size())
        {
            return;
        }
        PolicyRule policyRule = superSet.get(idx);
        current.add(policyRule);
        //"guess" x is in the subset
        getSubsetsHelper(superSet, l, idx+1, current, solution);
        current.remove(policyRule);
        //"guess" x is not in the subset
        getSubsetsHelper(superSet, l, idx+1, current, solution);
    }




    /*
    This class is for separation of authorization policy rules (we need this to overcome the intractability problem of sod verification).
    Instead of verifying sod constraints directly on permissions (which are represented as sod=< Perm,k >), we verify them
    based on policy rules as suggested in the following papers:
            [1] S.  Jha,  S.  Sural,  V.  Atluri,  and  J.  Vaidya,  “Enforcing  separation  of  duty  in  attribute  based
              access  control  systems,”inInternational Conference on Information Systems Security.Springer, 2015, pp. 61–78
            [2] Specification  and  verification  of  separation  of  dutyconstraints in attribute-based access control,”IEEE
             Transactions  on  Information  Forensics  and  Security,  vol.  13,  no.  4,pp. 897–911, 2017.
     */

    public class SoAPPolicyRule
    {
        private Set<PolicyRule> policyRuleSet;
        private int k;

        public SoAPPolicyRule(Set<PolicyRule> policyRuleSet, int k)
        {
            this.policyRuleSet=policyRuleSet;
            this.k=k;
        }

        public Set<PolicyRule> getPolicyRuleSet()
        {
            return policyRuleSet;
        }
    }


    /*
    This class is for mutually exclusive rules (we need this to overcome the intractability problem of sod verification).
    Instead of verifying sod constraints directly on permissions (which are represented as sod=< Perm,k >), we verify them
    based on policy rules as suggested in the following papers:
            [1] S.  Jha,  S.  Sural,  V.  Atluri,  and  J.  Vaidya,  “Enforcing  separation  of  duty  in  attribute  based
              access  control  systems,”inInternational Conference on Information Systems Security.Springer, 2015, pp. 61–78
            [2] Specification  and  verification  of  separation  of  dutyconstraints in attribute-based access control,”IEEE
             Transactions  on  Information  Forensics  and  Security,  vol.  13,  no.  4,pp. 897–911, 2017.
     */
    public class MERConstraint {
        private Set<PolicyRule> policyRules;
        private int m; // this is # agents needed for performing actions
        // provided by policy rules in "Policy" to fulfill a task. (Remember: MER=< Perm,k >)

        public MERConstraint(Set<PolicyRule> policyRules, int m)
        {
            this.policyRules=policyRules;
            this.m=m;
        }

        public Set<PolicyRule> getPolicyRules() {
            return policyRules;
        }

        public int getM()
        {
            return m;
        }
    }
}
