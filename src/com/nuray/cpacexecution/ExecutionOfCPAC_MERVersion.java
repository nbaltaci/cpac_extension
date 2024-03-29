package com.nuray.cpacexecution;



import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.enforcementfunctions.*;
import com.nuray.cpacexecution.storage.*;
import com.nuray.gagm.pathfinder.Edge;
import com.nuray.gagm.pathfinder.Vertex;
import com.nuray.wso2.Test;
import org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ExecutionOfCPAC_MERVersion {

    private ResourceBase resourceBase;
    private ActionBase actionBase;
    private AgentBase agentBase;
    private PolicyBase policyBase;
    private SODBase sodBase;

    AuthorizationDecision authorizationDecision;

    // WSO2 IS related variables
    EntitlementPolicyAdminServiceStub EPASadminStub;
//    RemoteUserStoreManagerServiceStub RUSMSadminStub;


    public ExecutionOfCPAC_MERVersion(ResourceBase resourceBase,
                                      ActionBase actionBase,
                                      AgentBase agentBase,
                                      PolicyBase policyBase,
                                      SODBase sodBase) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException, EntitlementServiceException {

        this.resourceBase=resourceBase;
        this.actionBase=actionBase;
        this.agentBase=agentBase;
        this.policyBase=policyBase;
        this.sodBase=sodBase;

        //  publish AC policies to PAP
        List<Policy> policyList=policyBase.getPolicyList();
        authorizationDecision=new AuthorizationDecision();
        EPASadminStub = authorizationDecision.getEPASadminStub();
//        authorizationDecision.publishPolicies(policyList);
//        publishPolicies(policyList);

        //publish SOD policies to PAP here...
        List<SODPolicy> sodPolicyList = sodBase.getSODPolicyList();
        publishSODPolicies(sodPolicyList);

    }

    /**
     *
     * @param iar
     * @param varList_v_cur
     * @param m_cur
     * @param v_cur
     */
    public List<Agent> executeCPAC(InitialAccessRequest iar,
                                   Queue<Map<Vertex,Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur,
                                   String m_cur, Vertex v_cur, Vertex v_next,
                                   VirtualAccessRequest[] vars) throws Exception {

        //  line 1: initialization
        List<Agent> agentList_v_cur=new ArrayList<>();
        List<Agent> agentList_v_next=new ArrayList<>();
//        Vertex v_next = null;

        // line 2: if this is a var generated by GAGM
        if(varList_v_cur!=null)
        {
            // line 3: Extract var for the current and next state from varList_v_cur
//            VirtualAccessRequest[] vars = extractVars(varList_v_cur,v_cur);
            VirtualAccessRequest var_v_cur = vars[0];
            VirtualAccessRequest var_v_next = vars[1];

            // line 4: query eligible agents for var_v_cur and var_v_next from PIP & add
            List<Attribute> var_v_curAgentAttributes = var_v_cur.getAgentAttributes();
            agentList_v_cur= queryEligibleAgents(var_v_curAgentAttributes);

            if(var_v_next!=null)
            {
                List<Attribute> var_v_nextAgentAttributes = var_v_next.getAgentAttributes();
                agentList_v_next= queryEligibleAgents(var_v_nextAgentAttributes);

                // line 5

                List<Agent> agentList_intersection = intersectAgentLists(agentList_v_cur, agentList_v_next);

                if(!agentList_intersection.isEmpty())
                {
                    //  line 6

                    agentList_v_cur=agentList_intersection;
                }
            }

            //  line 7: Extract perm_var from var_v_cur
            Permission perm_var=var_v_cur.extractPermission(resourceBase);

            //  line 8: check applicable sod policies to perm_var
            List<SODPolicyRule> applicableSoDs=checkApplicableSoDPolicies(perm_var);

            List<Agent> tempListForEligibleAgents=new LinkedList<>(agentList_v_cur); //=> this is to prevent concurrent
            // modification exception to the agentList_v_cur (below in the inner for loop)

            if(!applicableSoDs.isEmpty()) // line 9
            {
                for (SODPolicyRule sod:applicableSoDs)  //  line 10
                {
                    Set<SODPolicyRule.MERConstraint> MERSet = sod.getMERSet();
                    if (MERSet!=null)
                    {
                        for (SODPolicyRule.MERConstraint merConstraint:MERSet)
                        {
                            for (Agent agent:agentList_v_cur)
                            {
                                List<PolicyRule> validPolicyRules=computeValidSetForAgent(merConstraint,agent);

                                if (validPolicyRules.size()>merConstraint.getM())
                                {
                                    if(m_cur.equalsIgnoreCase("emergency"))     //  line 13
                                    {
                                        if(v_next==null)
                                        {
                                            System.out.println("v_next is null!");
                                        }
                                        else if(sod.getRiskOfSoDPolicy()>(Math.abs(v_cur.getRisk()-v_next.getRisk())))   //  line 14
                                        {
                                            tempListForEligibleAgents.remove(agent);      //  line 15
                                        }
                                    }
                                    else
                                    {
                                        tempListForEligibleAgents.remove(agent);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            agentList_v_cur=tempListForEligibleAgents;

            for (Agent agent:agentList_v_cur)   //  line 18
            {
                //  line 19:    Send perm_var to PEP of agn_i  (this can be realized by creating a PEP object for each client in the system
                // and calling a method  implemented for token management such as: manageToken(agni,permvar,timestamp).
                // Please note that this is not in the scope of the paper.
            }
        }

        // line 20: if this an access request submitted by an agent
        if(iar!=null)
        {
            //  line 21: extract the requested permission and requesting agent from iar
            Agent agent = iar.getAgent();
            Permission perm_iar = iar.getPermission();
            Agent authorizedAgent=null;

            //  line 22: check applicable sod policies to perm_iar
            List<SODPolicyRule> applicableSoDs=checkApplicableSoDPolicies(perm_iar);

            if(!applicableSoDs.isEmpty()) // line 23
            {
                for (SODPolicyRule sod:applicableSoDs)  //  line 24
                {
                    Set<SODPolicyRule.MERConstraint> MERSet = sod.getMERSet();

                    for (SODPolicyRule.MERConstraint merConstraint:MERSet)
                    {
                        List<PolicyRule> validPolicyRules=computeValidSetForAgent(merConstraint,agent);

                        if (validPolicyRules.size()>merConstraint.getM())
                        {
                            if(m_cur.equalsIgnoreCase("emergency"))     //  line 13
                            {
                                if(sod.getRiskOfSoDPolicy()<=(Math.abs(v_cur.getRisk()-v_next.getRisk())))   //  line 14
                                {
                                    authorizedAgent = evaluate(iar, m_cur);//  line 28
                                }
                            }
                        }
                        else
                        {
                            authorizedAgent = evaluate(iar, m_cur);//  line 28
                        }
                    }
                }
            }
            else    //  line 31
            {
                authorizedAgent=evaluate(iar,m_cur);      //  line 32
            }

            if(authorizedAgent!=null)
            {
                agentList_v_cur.add(authorizedAgent); // line 33
            }
        }
        return agentList_v_cur;     //  line 34

    }

    private boolean checkMEPSet(Set<SODPolicyRule.MERConstraint> MERSet, List<Agent> agentList_v_cur)
    {
        boolean flag=true;
        for (SODPolicyRule.MERConstraint merConstraint:MERSet)
        {
            flag=check_MEP(merConstraint,agentList_v_cur);
            if (flag==false)
            {
                return false;
            }
        }
        return true;
    }

    private boolean check_MEP(SODPolicyRule.MERConstraint merConstraint,List<Agent> agentList_v_cur)
    {
        for (Agent agent:agentList_v_cur)
        {
            List<PolicyRule> validPolicyRules= find_valid_set(merConstraint,agent);

            if (validPolicyRules.size()>merConstraint.getM())
            {
                return false;
            }
        }
        return true;
    }

    private List<PolicyRule> find_valid_set(SODPolicyRule.MERConstraint merConstraint, Agent agent)
    {
        Set<PolicyRule> merConstraintPolicyRules = merConstraint.getPolicyRules();

        List<PolicyRule> validPolicyRules = merConstraintPolicyRules
                .stream()
                .filter(policyRule -> policyRule.getRuleEffect().equalsIgnoreCase("permit"))
                .filter(policyRule ->
                        policyRule.getAgentAttributes().values()
                                .stream()
                                .flatMap(agentAttributeGroup ->
                                        agentAttributeGroup.stream()
                                                .flatMap(agentAttribute -> agentAttribute.keySet().stream()))
                                .allMatch(policyRuleAttribute -> agent.hasAttribute(policyRuleAttribute))).collect(Collectors.toList());

        return validPolicyRules;
    }

    public VirtualAccessRequest[] extractVars(Queue<Map<Vertex,Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur, Vertex startVertex) throws Exception
    {
        // line 3: Extract var for the current and next state from varList_v_cur
        VirtualAccessRequest var_v_cur = null;
        VirtualAccessRequest var_v_next = null;

        if(!varList_v_cur.isEmpty()&&varList_v_cur!=null)
        {
            Iterator<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_iterator = varList_v_cur.iterator();

            while(varList_v_iterator.hasNext())
            {
                Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>> vertexQueueMap = varList_v_iterator.next();
                Vertex finalState = vertexQueueMap.keySet().iterator().next();

                Queue<Map<Edge, VirtualAccessRequest>> varList_f = vertexQueueMap.get(finalState);

                Iterator<Map<Edge, VirtualAccessRequest>> edgeMapIterator = varList_f.iterator();

                while (edgeMapIterator.hasNext())
                {
                    Map<Edge, VirtualAccessRequest> edgeVarMap = edgeMapIterator.next();

                    //check if there is an edge from v_cur. If so, it is safe to find v_next and call executeCPAC().
                    // otherwise, there is no need to call executeCPAC() since there will be no var_v_cur
                    Edge nextEdge = edgeVarMap.keySet().iterator().next();// => there is only one mapping btw an edge and a var

                    if(nextEdge.getSourceVertex().equals(startVertex))
                    {
                        var_v_cur=nextEdge.getVar();
                        Vertex v_next = findVarNextOnShortestPath(startVertex, varList_v_cur);

                        if(edgeMapIterator.hasNext())
                        {
                            edgeVarMap=edgeMapIterator.next();
                            nextEdge = edgeVarMap.keySet().iterator().next();

                            if (v_next!=null&&nextEdge.getSourceVertex().equals(v_next))
                            {
                                var_v_next=nextEdge.getVar();
                            }
                        }
                    }
                }
            }
        }


        VirtualAccessRequest[] vars=new VirtualAccessRequest[2];
        vars[0]=var_v_cur;
        vars[1]=var_v_next;

        return vars;
    }

    public Vertex findVarNextOnShortestPath(Vertex v_cur,Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur) throws Exception
    {
        Vertex v_next=null;
        if(varList_v_cur!=null&&varList_v_cur.size()!=0)
        {
            Iterator<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> iterator = varList_v_cur.iterator();
            while (iterator.hasNext())
            {
                Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>> vertexQueueMap = iterator.next();
                Iterator<Vertex> finalStateIterator = vertexQueueMap.keySet().iterator();

                while (finalStateIterator.hasNext())
                {
                    Vertex finalState = finalStateIterator.next();
                    Queue<Map<Edge, VirtualAccessRequest>> varList_f = vertexQueueMap.get(finalState);

                    Iterator<Map<Edge, VirtualAccessRequest>> edgeMapIterator = varList_f.iterator();

                    while (edgeMapIterator.hasNext())
                    {
                        Map<Edge, VirtualAccessRequest> edgeVarMap = edgeMapIterator.next();

                        //check if there is an edge from v_cur. If so, it is safe to find v_next and call executeCPAC().
                        // otherwise, there is no need to call executeCPAC() since there will be no var_v_cur
                        Edge nextEdge = edgeVarMap.keySet().iterator().next();// => there is only one mapping btw an edge and a var

                        if(nextEdge.getSourceVertex().equals(v_cur))
                        {
                            List<Edge> edges = v_cur.getEdges();
                            for (Edge e:edges)
                            {
                                if(e.getTargetVertex().equals(nextEdge.getTargetVertex()))
                                {
                                    if (edgeMapIterator.hasNext())
                                    {
                                        edgeVarMap=edgeMapIterator.next();
                                        nextEdge=edgeVarMap.keySet().iterator().next();
                                        if(nextEdge.getSourceVertex().equals(e.getTargetVertex()))
                                        {
                                            v_next=nextEdge.getSourceVertex();
                                        }
                                        else
                                        {
                                            v_next=e.getTargetVertex();
                                        }
                                    }
                                    else
                                    {
                                        v_next=e.getTargetVertex();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return v_next;
    }

    private List<Agent> queryEligibleAgents(List<Attribute> attributes) throws Exception {

        Date date=new Date();




        List<Agent> existingEligibleAgents=new ArrayList<>();
        for (Attribute attribute:attributes)
        {
            List<Agent> agentsWithAttributeValue = agentBase.getAgentsWithAttributeValue(attribute);
            if(existingEligibleAgents.size()==0)
            {
                existingEligibleAgents.addAll(agentsWithAttributeValue);
            }
            else
            {
                existingEligibleAgents =intersectAgentLists(existingEligibleAgents,agentsWithAttributeValue);
            }
        }
        Date date2=new Date();
//        System.out.println("Time for queryEligibleAgents() is: "+(date2.getTime()-date.getTime()));
        return existingEligibleAgents;
    }

    private List<Agent> intersectAgentLists(List<Agent> agentList1, List<Agent> agentList2) {
        Date date=new Date();


        List<Agent> intersection = new LinkedList<>();

        for(Agent agent : agentList1) {
            if(agentList2.contains(agent))
            {
                if (!intersection.contains(agent))
                {
                    intersection.add(agent);
                }
            }
        }

        Date date2=new Date();
//        System.out.println("Time for intersectAgentLists() is: "+(date2.getTime()-date.getTime()));

        return intersection;
    }

    /**
     * To compute valid set (of permissions) for an agent, first find the policy rules that are applicable to the given agent and have
     * "permit" type of rule effect.
     * @param agent
     * @throws Exception
     */
    private List<PolicyRule> computeValidSetForAgent(SODPolicyRule.MERConstraint merConstraint, Agent agent) throws Exception
    {
        Set<PolicyRule> merConstraintPolicyRules = merConstraint.getPolicyRules();

//        List<PolicyRule> policyRulesWithPermit = merConstraintPolicyRules
//                .stream()
//                .filter(policyRule -> policyRule.getRuleEffect().equalsIgnoreCase("permit"))
//                .collect(Collectors.toList());
//
//        List<PolicyRule> validPolicyRules = policyRulesWithPermit
//                .stream()
//                .filter(policyRule ->
//                        policyRule.getAgentAttributes().values()
//                                .stream()
//                                .flatMap(agentAttributeGroup ->
//                                        agentAttributeGroup.stream()
//                                                .flatMap(agentAttribute -> agentAttribute.keySet().stream()))
//                                .allMatch(policyRuleAttribute -> agent.hasAttribute(policyRuleAttribute))).collect(Collectors.toList());

        List<PolicyRule> validPolicyRules = merConstraintPolicyRules
                .stream()
                .filter(policyRule -> policyRule.getRuleEffect().equalsIgnoreCase("permit"))
                .filter(policyRule ->
                        policyRule.getAgentAttributes().values()
                                .stream()
                                .flatMap(agentAttributeGroup ->
                                        agentAttributeGroup.stream()
                                                .flatMap(agentAttribute -> agentAttribute.keySet().stream()))
                                .allMatch(policyRuleAttribute -> agent.hasAttribute(policyRuleAttribute))).collect(Collectors.toList());

        return validPolicyRules;
    }

    private List<SODPolicyRule> checkApplicableSoDPolicies(Permission permission)
    {
        Date date=new Date();

        List<SODPolicyRule> applicableSODPolicyRules=new ArrayList<>();
        List<SODPolicy> sodPolicyList = sodBase.getSODPolicyList();

        for (SODPolicy sodPolicy:sodPolicyList)
        {
            List<SODPolicyRule> sodPolicyRules = sodPolicy.getSodPolicyRules();
            for (SODPolicyRule rule:sodPolicyRules)
            {
                List<Permission> Perm=rule.getPerm();
                for (Permission p:Perm)
                {
                    if(p.getResource().equals(permission.getResource()))
                    {
                        List<Action> actionListInRequestedPerm = permission.getActionList();
                        for (Action action:actionListInRequestedPerm)
                        {
                            if(p.getActionList().contains(action))
                            {
                                if(!applicableSODPolicyRules.contains(rule))
                                {
                                    applicableSODPolicyRules.add(rule);
                                }
                            }
                        }
                    }
                }
//                if (Perm.contains(permission))
//                {
//                    applicableSODPolicyRules.add(rule);
//                }
            }
        }

        Date date2=new Date();

//        System.out.println("Time for checkApplicableSoDPolicies() is: "+(date2.getTime()-date.getTime()));

        return applicableSODPolicyRules;
    }

    private List<Permission> intersectPermissionLists(List<Permission> permissionList1, List<Permission> permissionList2)
    {
        Date date=new Date();

        List<Permission> intersection = new LinkedList<>();

        for(Permission perm1 : permissionList1)
        {
            for (Permission perm2:permissionList2)
            {
                if(perm1.getResource().equals(perm2.getResource()))
                {
                    List<Action> perm1ActionList = perm1.getActionList();
                    boolean allActionsIncluded=true;
                    for (Action action:perm1ActionList)
                    {
                        if(!perm2.getActionList().contains(action))
                        {
                            allActionsIncluded=false;
//                            intersection.add(perm1);
                        }
                    }
                    if(allActionsIncluded)
                    {
                        if(!intersection.contains(perm1))
                        {
                            intersection.add(perm1);
                        }
                    }
                }
            }
        }

        Date date2=new Date();

//        System.out.println("Time for intersectPermissionLists() is: "+(date2.getTime()-date.getTime()));

        return intersection;
    }

    private Agent evaluate(InitialAccessRequest iar, String m_cur) throws Exception
    {
        //  line 36: Extend iar with registered attributes in PIP
        AgentInitiatedAccessRequest aar=new AgentInitiatedAccessRequest(iar,m_cur,resourceBase,actionBase,agentBase);
        //  line 37: Get policy set from PAP ==> this is done by WSO2 IS entitlement stub (ESadminStub) (inside auth method of my
        // AuthorizationDecision.java class

        //  line 38
        String decisionXML = authorizationDecision.auth(aar);
        String decision=authorizationDecision.parseDecision(decisionXML);


        if(decision.equalsIgnoreCase("Permit"))     //  line 39
        {
            //  line 38:    Send perm_iar to PEP of agn  (this can be realized by creating a PEP object for each client in the system
            // and calling a method  implemented for token management such as: manageToken(agn,perm_iar,timestamp).
            // Please note that this is not in the scope of the paper.
            return iar.getAgent(); //  line 41
        }
        return null;  // line 42
    }


//    private void publishPolicies(List<Policy> policyList) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {
//        //publish these policies to the PAP
//        for (Policy policy:policyList)
//        {
//            PolicyDTO policyToPublish = new PolicyDTO();
//            policyToPublish.setPolicyId(policy.getPolicyID());
//            policyToPublish.setPolicy(policy.getPolicyContentXACML());
//            policyToPublish.setActive(true);
//            policyToPublish.setPromote(true);
//
//            authorizationDecision.getEPASadminStub().addPolicy(policyToPublish);
//
////            EPASadminStub.addPolicy(policyToPublish);
//        }
//    }

    private void publishSODPolicies(List<SODPolicy> sodPolicyList)
    {
        // TODO: to be implemented later if needed (for publishing SOD policies to WSO2 identity server, for now it is not needed)

    }

    public AuthorizationDecision getAuthorizationDecision()
    {
        return authorizationDecision;
    }

}
