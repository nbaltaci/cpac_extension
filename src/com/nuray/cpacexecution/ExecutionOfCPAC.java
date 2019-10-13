package com.nuray.cpacexecution;



import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.enforcementfunctions.*;
import com.nuray.cpacexecution.storage.*;
import com.nuray.gagm.pathfinder.Edge;
import com.nuray.gagm.pathfinder.Vertex;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;

import java.rmi.RemoteException;
import java.util.*;


public class ExecutionOfCPAC {

    private ResourceBase resourceBase;
    private ActionBase actionBase;
    private AgentBase agentBase;
    private PolicyBase policyBase;
    private SODBase sodBase;

    AuthorizationDecision authorizationDecision;

    // WSO2 IS related variables
    EntitlementPolicyAdminServiceStub EPASadminStub;
//    RemoteUserStoreManagerServiceStub RUSMSadminStub;


    public ExecutionOfCPAC(ResourceBase resourceBase,
                           ActionBase actionBase,
                           AgentBase agentBase,
                           PolicyBase policyBase,
                           SODBase sodBase) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        this.resourceBase=resourceBase;
        this.actionBase=actionBase;
        this.agentBase=agentBase;
        this.policyBase=policyBase;
        this.sodBase=sodBase;

        //  publish AC policies to PAP
        List<Policy> policyList=policyBase.getPolicyList();
        authorizationDecision=new AuthorizationDecision();
        EPASadminStub = authorizationDecision.getEPASadminStub();
        publishPolicies(policyList);

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
                                   Map<Edge, VirtualAccessRequest> varList_v_cur,
                                   String m_cur, Vertex v_cur) throws Exception {

        //  line 1: initialization
        List<Agent> agentList_v_cur=new ArrayList<>();
        List<Agent> agentList_v_next=new ArrayList<>();
        Vertex v_next = null;

        //  line 2: SOD←Get SoD policies from PAP ==> This is done above in the constructor by adding SoD policies to sodBase


        // line 3: if this is a var generated by GAGM
        if(!varList_v_cur.isEmpty())
        {
            // line 4: Extract var for the current and next state from varList_v_cur

            VirtualAccessRequest var_v_cur = null;
            VirtualAccessRequest var_v_next = null;

            Iterator<Edge> iterator = varList_v_cur.keySet().iterator();

            Edge edge1 = iterator.next();
            Vertex sourceVertex = edge1.getSourceVertex();

            if(sourceVertex==v_cur)
            {
                v_next=edge1.getTargetVertex();
                var_v_cur = varList_v_cur.get(edge1);

                Edge edge2 = iterator.next();
                sourceVertex=edge2.getSourceVertex();

                if(sourceVertex==v_next)
                {
                    var_v_next = varList_v_cur.get(edge2);
                }
                else
                {
                    throw new Exception("Shortest path (or varList) is computed wrong!");
                }
            }
            else
            {
                throw new Exception("Shortest path (or varList) is computed wrong!");
            }

            // line 5: query eligible agents for var_v_cur and var_v_next from PIP & add

            List<Attribute> var_v_curAgentAttributes = var_v_cur.getAgentAttributes();
            List<Attribute> var_v_nextAgentAttributes = var_v_next.getAgentAttributes();

            agentList_v_cur= queryEligibleAgents(var_v_curAgentAttributes, agentList_v_cur);
            agentList_v_next=queryEligibleAgents(var_v_nextAgentAttributes,agentList_v_next);

            // line 6

            List<Agent> agentList_intersection = intersectAgentLists(agentList_v_cur, agentList_v_next);

            if(!agentList_intersection.isEmpty())
            {
                //  line 7

                agentList_v_cur=agentList_intersection;
            }

            //  line 8: Extract perm_var from var_v_cur
            Permission perm_var=var_v_cur.extractPermission(resourceBase);

            //  line 9: check applicable sod policies to perm_var
            List<SODPolicyRule> applicableSoDs=checkApplicableSoDPolicies(perm_var);

            if(!applicableSoDs.isEmpty()) // line 10
            {
                for (SODPolicyRule sod:applicableSoDs)  //  line 11
                {
                    for (Agent agent:agentList_v_cur)   //  line 12
                    {
                        List<Permission> validPerms=computeValidSetForAgent(agent);
                        validPerms.add(perm_var);
                        List<Permission> unsafePermissions=intersectPermissionLists(validPerms,sod.getPerm());

                        if(unsafePermissions.size()>=sod.getK())    //  line 13
                        {
                            if(m_cur.equalsIgnoreCase("emergency"))     //  line 14
                            {
                                if(sod.getRiskOfSoDPolicy()>(Math.abs(v_cur.getRisk()-v_next.getRisk())))   //  line 15
                                {
                                    agentList_v_cur.remove(agent);      //  line 16
                                }
                            }
                            else       //   line 17
                            {
                                agentList_v_cur.remove(agent);      //  line 18
                            }

                        }
                    }

                }
            }

            for (Agent agent:agentList_v_cur)   //  line 19
            {
                //  line 20:    Send perm_var to PEP of agn_i  (this can be realized by creating a PEP object for each client in the system
                // and calling a method  implemented for token management such as: manageToken(agni,permvar,timestamp).
                // Please note that this is not in the scope of the paper.
            }
        }

        // line 21: if this an access request submitted by an agent
        if(iar!=null)
        {
            //  line 22: extract the requested permission and requesting agent from iar
            Agent agent = iar.getAgent();
            Permission perm_iar = iar.getPermission();
            agentList_v_cur.add(agent);

            //  line 23: check applicable sod policies to perm_iar
            List<SODPolicyRule> applicableSoDs=checkApplicableSoDPolicies(perm_iar);

            if(!applicableSoDs.isEmpty()) // line 24
            {
                for (SODPolicyRule sod:applicableSoDs)  //  line 25
                {
                    List<Permission> validPerms=computeValidSetForAgent(agent);
                    validPerms.add(perm_iar);
                    List<Permission> unsafePermissions=intersectPermissionLists(validPerms,sod.getPerm());

                    if(unsafePermissions.size()>=sod.getK())    //  line 26
                    {
                        if(m_cur.equalsIgnoreCase("emergency"))     //  line 27
                        {
                            if(sod.getRiskOfSoDPolicy()<=(Math.abs(v_cur.getRisk()-v_next.getRisk())))   //  line 28
                            {
                                evaluate(iar,m_cur);      //  line 29

                            }
                            else        //  line 30
                            {
                                agentList_v_cur.remove(agent);      //  line 29: actually this line is for dropping the iar as written in the
                                // CPACExecution pseudocode. It can be implemented by doing nothing since iar will not be evaluated
                                // and agentList_v_cur will stay null as the agent will not be authorized.
                            }
                        }
                        else    //  line 32
                        {
                            agentList_v_cur.remove(agent);      //  line 33: actually this line is for dropping the iar as written in the
                            // CPACExecution pseudocode. It can be implemented by doing nothing since iar will not be evaluated
                            // and agentList_v_cur will stay null as the agent will not be authorized.
                        }
                    }
                }
            }
            else    //  line 30
            {
                evaluate(iar,m_cur);      //  line 31
            }
        }

        return agentList_v_cur;     //  line 32

    }



    private List<Agent> queryEligibleAgents(List<Attribute> attributes, List<Agent> existingEligibleAgents) throws Exception {
        for (Attribute attribute:attributes)
        {
            List<Agent> agentsWithAttributeValue = agentBase.getAgentsWithAttributeValue(attribute);
            for (Agent a:agentsWithAttributeValue)
            {
                if(!existingEligibleAgents.contains(a))
                {
                    existingEligibleAgents.add(a);
                }
            }
        }
        return existingEligibleAgents;
    }

    private List<Agent> intersectAgentLists(List<Agent> agentList1, List<Agent> agentList2) {
        List<Agent> intersection = new LinkedList<>();

        for(Agent agent : agentList1) {
            if(agentList2.contains(agent)) {
                intersection.add(agent);
            }
        }
        return intersection;
    }

    /**
     * To compute valid set (of permissions) for an agent, first find the policy rules that are applicable to the given agent and have
     * "permit" type of rule effect.
     * @param agent
     * @throws Exception
     */
    private List<Permission> computeValidSetForAgent(Agent agent) throws Exception
    {
        List<Policy> policyList = policyBase.getPolicyList();

        if(policyList.isEmpty())
        {
            throw new Exception("You have not recorded any policy, so you cannot perform this operation.");
        }

        List<Permission> permissionsApplicableToAgent=new ArrayList<>();

        for (Policy policy:policyList)
        {
            List<PolicyRule> policyRules = policy.getPolicyRules();

            List<Permission> permissionsFromPolicy=new ArrayList<>();

            for (PolicyRule policyRule:policyRules)
            {
                List<Permission> permissionsFromRule=new ArrayList<>();

                //check only policy rules that have "permit" effects (positive authorizations)
                if (policyRule.getRuleEffect().equalsIgnoreCase("permit"))
                {
                    Map<String, List<Map<Attribute, String>>> agentAttributeMap = policyRule.getAgentAttributes();
                    Iterator<String> iterator = agentAttributeMap.keySet().iterator(); //here we iterate through agent groups,
                    // i.e. <Subject></Subject> groups in XACML file

                    while (iterator.hasNext())
                    {
                        String agentGroupInPolicyRule = iterator.next();
                        List<Map<Attribute, String>> agentAttMatchFunctionMaps = agentAttributeMap.get(agentGroupInPolicyRule);

                        for (Map<Attribute,String> agentAttMFMap:agentAttMatchFunctionMaps)
                        {
                            Attribute attribute = agentAttMFMap.keySet().iterator().next();// the map here is a single-element map
                            // (a single agent att and an attribute matching function, so we only get a single element using "next" method

                            String attributeName = attribute.getAttributeName();

                            if(attributeName.equalsIgnoreCase("agentID"))
                            {
                                String agentIDValInPolicyRule = attribute.getAttributeValueCategorical();
                                String realAgentID=agent.getUserName().getAttributeValueCategorical();

                                if(agentIDValInPolicyRule.equalsIgnoreCase(realAgentID))
                                {
                                    permissionsFromRule = policyRule.getPermissionsFromRule(resourceBase, actionBase);
                                    permissionsFromPolicy.addAll(permissionsFromRule);
                                }
                            }
                        }
                    }
                }
            }

            permissionsApplicableToAgent.addAll(permissionsFromPolicy);
        }

        return permissionsApplicableToAgent;
    }

    private List<SODPolicyRule> checkApplicableSoDPolicies(Permission permission)
    {
        List<SODPolicyRule> applicableSODPolicyRules=new ArrayList<>();
        List<SODPolicy> sodPolicyList = sodBase.getSODPolicyList();

        for (SODPolicy sodPolicy:sodPolicyList)
        {
            List<SODPolicyRule> sodPolicyRules = sodPolicy.getSodPolicyRules();
            for (SODPolicyRule rule:sodPolicyRules)
            {
                List<Permission> Perm=rule.getPerm();
                if (Perm.contains(permission))
                {
                    applicableSODPolicyRules.add(rule);
                }
            }
        }

        return applicableSODPolicyRules;
    }

    private List<Permission> intersectPermissionLists(List<Permission> permissionList1, List<Permission> permissionList2)
    {
        List<Permission> intersection = new LinkedList<>();

        for(Permission perm : permissionList1) {
            if(permissionList2.contains(perm)) {
                intersection.add(perm);
            }
        }
        return intersection;
    }

    private void evaluate(InitialAccessRequest iar, String m_cur) throws Exception
    {
        //  line 34: Extend iar with registered attributes in PIP
        AgentInitiatedAccessRequest aar=new AgentInitiatedAccessRequest(iar,m_cur,resourceBase,actionBase,agentBase);
        //  line 35: Get policy set from PAP ==> this is done by WSO2 IS entitlement stub (ESadminStub) (inside auth method of my
        // AuthorizationDecision.java class

        //  line 36
        String decision = authorizationDecision.auth(aar);

        if(decision.equalsIgnoreCase("Permit"))     //  line 37
        {
            //  line 38:    Send perm_iar to PEP of agn  (this can be realized by creating a PEP object for each client in the system
            // and calling a method  implemented for token management such as: manageToken(agn,perm_iar,timestamp).
            // Please note that this is not in the scope of the paper.
        }
    }


    private void publishPolicies(List<Policy> policyList) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        //publish these policies to the PAP
        for (Policy policy:policyList)
        {
            PolicyDTO policyToPublish = new PolicyDTO();
            policyToPublish.setPolicyId(policy.getPolicyID());
            policyToPublish.setPolicy(policy.getPolicyContentXACML());
            policyToPublish.setActive(true);
            policyToPublish.setPromote(true);

            EPASadminStub.addPolicy(policyToPublish);
        }
    }

    private void publishSODPolicies(List<SODPolicy> sodPolicyList)
    {
        // TODO: to be implemented later if needed (for publishing SOD policies to WSO2 identity server, for now it is not needed)

    }

}
