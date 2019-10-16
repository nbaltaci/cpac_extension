package com.nuray.cpacexecution.test;//package com.nuray.cpacexecution;

import com.nuray.cpacexecution.ExecutionOfCPAC;
import com.nuray.cpacexecution.cpacmodel.*;
import com.nuray.cpacexecution.enforcementfunctions.*;
import com.nuray.cpacexecution.storage.*;
import com.nuray.gagm.pathfinder.Edge;
import com.nuray.gagm.pathfinder.Graph;
import com.nuray.gagm.pathfinder.VARGeneration;
import com.nuray.gagm.pathfinder.Vertex;

import java.util.*;

public class Test {


    private static ResourceBase resourceBase;
    private static ActionBase actionBase;
    private static AgentBase agentBase;
    private static PolicyBase policyBase;
    private static SODBase sodBase;


    public static void main(String [] args) throws Exception {

//        testVAR(false,"active"); //==> this is no sod violation case (i.e. lines 14-18 of algorithm executeCPAC is not run)
//        I need to find a violation case
//        // ==> since this is no SoD violation case, set the parameter to false

//        testVAR(true,"active");// ==> sod violation case for operational mode="active"
//        // note that lines 15 and 16 of the algorithm will not be executed since it is not emergency. So,
//        // sod violation will not be allowed.

        testVAR(true,"emergency");// ==> sod violation case for operational mode="emergency"
        // note that lines 15 and 16 of the algorithm will be executed since it is emergency.
        // So, sod violation will be allowed based on risk values.==> for our test case, risk(sod)=500,
        // risk(v_cur)=risk(v_next)=0, so violation will not be allowed.








    }

    public static void testIAR(boolean isSoDViolation,String operationalMode) throws Exception
    {
        generatePoliciesAndPolicyElements(isSoDViolation,operationalMode);

        // 4. call executeCPAC algorithm
        ExecutionOfCPAC executionOfCPAC=new ExecutionOfCPAC(resourceBase,actionBase,agentBase,policyBase,sodBase);

        Resource resource = resourceBase.getResourceList().get(0);// ==> since there is single resource in my test case
        Agent agent = agentBase.getAgentList().get(0);// ==> since there is single agent in my test case
        List<Action> actions = actionBase.getActionList();

        Vertex v_cur=new Vertex(1);

        List<Agent> agentList = executionOfCPAC.executeCPAC(null,null, operationalMode, v_cur);
    }

    public static void testVAR(boolean isSoDViolation,String operationalMode) throws Exception {

        generatePoliciesAndPolicyElements(isSoDViolation,operationalMode);

        // 4. call executeCPAC algorithm
        ExecutionOfCPAC executionOfCPAC=new ExecutionOfCPAC(resourceBase,actionBase,agentBase,policyBase,sodBase);

        Resource resource = resourceBase.getResourceList().get(0);// ==> since there is single resource in my test case
        Agent agent = agentBase.getAgentList().get(0);// ==> since there is single agent in my test case
        List<Action> actions = actionBase.getActionList();


        // 4.a. generate varList_v_cur (based on generated resource,actions, and agent (and operational mode))
        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v = generateVarListForTest(agent, resource,
                                                                                                    actions, operationalMode);
        Vertex v_cur=new Vertex(1);
        //        int[] finalStates={3,4};
//        Queue<Map<Vertex,Queue<Map<Edge, VirtualAccessRequest>>>> varList_v=generateVarListForExperiment(1,1,
//                                                                                            3,finalStates);

        List<Agent> agentList = executionOfCPAC.executeCPAC(null, varList_v, operationalMode, v_cur);
    }

    private static void generatePoliciesAndPolicyElements(boolean isSoDViolation,String operationalMode) throws Exception {
        // 1. First, generate resource, action, agent, authorization policy, and SoD policy bases.
        resourceBase=new ResourceBase();
        actionBase=new ActionBase();
        agentBase=new AgentBase();

        policyBase=new PolicyBase();
        sodBase=new SODBase();

        // 2. Generate policy elements (resource(s), action(s), agent(s)) and add them to repositories
        // 2a. Generate a resource
        Resource resource=new Resource("patient123","human");
        // 2.a.1. add resource attribute "role"
        List<String> possibleRoles=new ArrayList<>(Arrays.asList(new String[]{"patient","physician","nurse","admin"}));
        AttributeRange roleRange=new AttributeRange(possibleRoles);
        Attribute resourceRole=new Attribute("role",roleRange,"categorical");
        resourceRole.setAttributeValueCategorical("patient");
        resource.addResourceAttribute(resourceRole);

        // 2.a.2. add resource attribute "ACT_ref"
        Attribute ACT_ref=new Attribute("ACT_ref",new AttributeRange(150,200),"numeric");
        resource.addResourceAttribute(ACT_ref);

        // 2.a.3. add resource attribute "ACT_cur"
        Attribute ACT_cur_lower=new Attribute("ACT_cur_lower",new AttributeRange(0,Double.MAX_VALUE),"numeric");
        ACT_cur_lower.setAttributeValueNumeric(ACT_ref.getAttributeRange().getLowerLimit());

        Attribute ACT_cur_upper=new Attribute("ACT_cur_upper",new AttributeRange(0,Double.MAX_VALUE),"numeric");
        ACT_cur_upper.setAttributeValueNumeric(ACT_ref.getAttributeRange().getUpperLimit());

        resource.addResourceAttribute(ACT_cur_lower);
        resource.addResourceAttribute(ACT_cur_upper);

        resourceBase.addResource(resource);

        // 2b. Generate actions
        Action action1=new Action("deliverHeparin","cyber-physical");
        Action action2=new Action("infuseDrug1","cyber-physical");
        Action action3=new Action("infuseDrug2","cyber-physical");
        List<Action> actions=Arrays.asList(new Action[]{action1,action2,action3});

        actionBase.addAction(action1);
        actionBase.addAction(action2);
        actionBase.addAction(action3);

        // 2b. Generate an agent
        Agent agent=new Agent("heparinDevice1","physical");
        agentBase.addAgent(agent);

        // 2c. Generate a policy
        Policy policy=generatePolicy(isSoDViolation);

        // 2d. Generate an sod policy
        SODPolicy sodPolicy=generateSoDPolicy();

        // 3. Add policies to repositories
        policyBase.addPolicy(policy);
        sodBase.addSODPolicy(sodPolicy);
    }



    private static Policy generatePolicy(boolean isSodViolation) throws Exception {
        // 2c. Generate a policy rule and add it to a policy

        // 2c.1. generate resource attributes with their matching functions to be used in the policy rule
        // (Map attributes to their attribute matching functions for XACML evaluation)
        List<Map<Attribute, String>> resourceAttributes = generateResourceAttributes();
        Map<String, List<Map<Attribute, String>>> resourceAttMatchFuncListMap=new HashMap<>();
        resourceAttMatchFuncListMap.put("resource group1",resourceAttributes); // this is a <Resource></Resource> group in XACML

        // 2c.2. generate agent attributes with their matching functions to be used in the policy rule
        // (Map attributes to their attribute matching functions for XACML evaluation)
        List<Map<Attribute,String>> agentAttributes=generateAgentAttributes();
        Map<String, List<Map<Attribute, String>>> agentAttMatchFuncListMap=new HashMap<>();
        agentAttMatchFuncListMap.put("agent group 1",agentAttributes);

        // 2c.3. generate environment attributes to be used in the policy rule
            // will be null for our sample CPAC policy

        // 2c.4. generate action attributes with their matching functions to be used in the policy rule
        // (Map attributes to their attribute matching functions for XACML evaluation)
        List<Map<Attribute,String>> actionAttributes=generateActionAttributes(isSodViolation);
        Map<String, List<Map<Attribute, String>>> actionAttMatchFuncListMap=new HashMap<>();
        actionAttMatchFuncListMap.put("action group 1",actionAttributes);

        PolicyRule policyRule=new PolicyRule(resourceAttMatchFuncListMap,agentAttMatchFuncListMap,
                actionAttMatchFuncListMap,null,
                "active","sample CPAC policy rule","permit");

        Policy policy=new Policy("sample CPAC policy",
                "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides",
                new ArrayList<>(Arrays.asList(new PolicyRule[]{policyRule})));


        return policy;

    }



    /**
     *  matching functions are for XACML request evaluation process
     * @param attribute
     * @param matchingFunction
     */
    private static Map<Attribute, String> mapAttributesToMatchingFunctions(Attribute attribute, String matchingFunction)
    {
        Map<Attribute,String> attributeToMatchingFunctionMap=new HashMap<>();
        attributeToMatchingFunctionMap.put(attribute,matchingFunction);
        return attributeToMatchingFunctionMap;
    }

    private static List<Map<Attribute, String>>  generateResourceAttributes() throws Exception {

        Attribute resourceID=new Attribute("resourceID",null,"categorical");
        resourceID.setAttributeValueCategorical("patient123");

        List<String> possibleRoles=new ArrayList<>(Arrays.asList(new String[]{"patient","physician","nurse","admin"}));
        AttributeRange roleRange=new AttributeRange(possibleRoles);
        Attribute role=new Attribute("role",roleRange,"categorical");
        role.setAttributeValueCategorical("patient");

        List<String> possibleClinicalConditions=new ArrayList<>(Arrays.asList(new String[]{"standard","risky"}));
        AttributeRange ccRange=new AttributeRange(possibleClinicalConditions);
        Attribute clinicalCondition=new Attribute("clinicalCondition",ccRange,"categorical");
        clinicalCondition.setAttributeValueCategorical("standard");

        Attribute ACT_ref=new Attribute("ACT_ref",new AttributeRange(150,200),"numeric");

        Attribute ACT_cur_lower=new Attribute("ACT_cur_lower",new AttributeRange(0,Double.MAX_VALUE),"numeric");
        ACT_cur_lower.setAttributeValueNumeric(ACT_ref.getAttributeRange().getLowerLimit());

        Attribute ACT_cur_upper=new Attribute("ACT_cur_upper",new AttributeRange(0,Double.MAX_VALUE),"numeric");
        ACT_cur_upper.setAttributeValueNumeric(ACT_ref.getAttributeRange().getUpperLimit());


        // 2c.5. Map attributes to their attribute matching functions (for XACML evaluation)

            // for resource attributes

        Map<Attribute,String> resourceIDToMatchingFunctionMap=
                mapAttributesToMatchingFunctions(resourceID,
                        "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case");

        Map<Attribute,String> roleToMatchingFunctionMap=mapAttributesToMatchingFunctions(role,
                "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case");

//        Map<Attribute,String> clinicalConToMatchingFunctionMap=mapAttributesToMatchingFunctions(clinicalCondition,
//                "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case");
//
//        Map<Attribute,String> ACT_refToMatchingFunctionMap=mapAttributesToMatchingFunctions(ACT_ref,
//                "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal");

        Map<Attribute,String> ACT_curToMatchingFunctionMap1=mapAttributesToMatchingFunctions(ACT_cur_lower,
                "urn:oasis:names:tc:xacml:1.0:function:double-greater-than");
        Map<Attribute,String> ACT_curToMatchingFunctionMap2=mapAttributesToMatchingFunctions(ACT_cur_upper,
                "urn:oasis:names:tc:xacml:1.0:function:double-less-than");

        List<Map<Attribute, String>> resourceAttToMatchingFunctions=new LinkedList<>();

        resourceAttToMatchingFunctions.add(resourceIDToMatchingFunctionMap);
        resourceAttToMatchingFunctions.add(roleToMatchingFunctionMap);
//        resourceAttToMatchingFunctions.add(clinicalConToMatchingFunctionMap);
//        resourceAttToMatchingFunctions.add(ACT_refToMatchingFunctionMap);
        resourceAttToMatchingFunctions.add(ACT_curToMatchingFunctionMap1);
        resourceAttToMatchingFunctions.add(ACT_curToMatchingFunctionMap2);

        return resourceAttToMatchingFunctions;

    }

    private static List<Map<Attribute, String>> generateAgentAttributes() throws Exception {
        Attribute agentID=new Attribute("agentID",null,"categorical");
        agentID.setAttributeValueCategorical("heparinDevice1");

        // 2c.5. Map attributes to their attribute matching functions (for XACML evaluation)
            // for agent attributes

        Map<Attribute,String> agentIDToMatchingFunctionMap=mapAttributesToMatchingFunctions(agentID,
                "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case");

        List<Map<Attribute, String>> agentAttToMatchingFunctions=new LinkedList<>();
        agentAttToMatchingFunctions.add(agentIDToMatchingFunctionMap);

        return agentAttToMatchingFunctions;
    }

    /**
     *
     * @param isSoDViolation: if this is true, actions in the policy (action attributes in an XACML policy) are generated
     *                      accordingly.
     * @return
     * @throws Exception
     */
    private static List<Map<Attribute, String>> generateActionAttributes(boolean isSoDViolation) throws Exception {
        List<Map<Attribute, String>> actionAttToMatchingFunctions=new LinkedList<>();
        Attribute actionID=new Attribute("actionID",null,"categorical");

        if(isSoDViolation)
        {
            actionID.setAttributeValueCategorical("infuseDrug2");
        }
        else
        {
            actionID.setAttributeValueCategorical("deliverHeparin");
        }

        // Map attributes to their attribute matching functions (for XACML evaluation) for action attributes
        Map<Attribute,String> actionIDToMatchingFunctionMap=mapAttributesToMatchingFunctions(actionID,
                "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case");
        actionAttToMatchingFunctions.add(actionIDToMatchingFunctionMap);

        return actionAttToMatchingFunctions;
    }

    private static SODPolicy generateSoDPolicy()
    {
        // 2c. Generate an SoD rule and add it to an SoD policy
        /*
                sample sod from the paper:
                sod1=<{perm1,perm2},2>
                perm1=<  infuseDrug1,patientX >
                perm2=< infuseDrug2,patientX >
         */

        SODPolicy sodPolicy=new SODPolicy();

            // find human resources (patients) to be infused a drug

        List<Resource> resourceList = resourceBase.getResourceList();
        List<Resource> humanResList=new LinkedList<>();
        for (Resource resource:resourceList)
        {
            if(resource.getType().equalsIgnoreCase("human"))
            {
                if(resource.getResourceID().startsWith("patient"))
                humanResList.add(resource);
            }
        }

        // create sod1 for each patient as a resource

        Action action1=actionBase.getActionWithActionID("infuseDrug1");
        List<Action> actionList1=new LinkedList<>();
        actionList1.add(action1);

        Action action2=actionBase.getActionWithActionID("infuseDrug2");
        List<Action> actionList2=new LinkedList<>();
        actionList2.add(action2);

        for (Resource resource:humanResList)
        {
            Permission permission1=new Permission(resource,actionList1);
            Permission permission2=new Permission(resource,actionList2);

            List<Permission> permissionList=new LinkedList<>();
            permissionList.add(permission1);
            permissionList.add(permission2);

            SODPolicyRule sodPolicyRule1=new SODPolicyRule(permissionList,2,500);
            sodPolicy.addSoDPolicyRule(sodPolicyRule1);
        }

        return sodPolicy;
    }


    private static Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> generateVarListForTest(Agent agent,
                                                                                                     Resource resource,
                                                                                                     List<Action> actionList,
                                                                                                     String operationalMode) throws Exception {
        // assuming that I am generating a graph for test case 1 where there are 5 vertices, starting vertex is 1 and
        // target vertex is 3 (shortest path for this graph: 1-2-3 (so, there are 2 edges on the shortest path).
        // I assume both edges are action edges. For edge 1, I generate a var that will correspond to an eligible agent
        // that I generated in the main method. For edge 2, I generate a var that will correspond to no eligible agent.

        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_start=new LinkedList<>();
        Vertex startVertex=new Vertex(1);
        Vertex intermediateVertex=new Vertex(2);
        Vertex targetVertex=new Vertex(3);

        List<Attribute> agentAttributes = agent.getAgentAttributes();
        List<Attribute> resourceAttributes = resource.getResourceAttributes();
        // I will add first two actions in the main method to var 1 (these actions are: deliverHeparin, infuseDrug1)
        Iterator<Action> actionIterator = actionList.iterator();
        Action action1 = actionIterator.next();
        List<Attribute> action1Attributes = action1.getActionAttributes();
        Action action2=actionIterator.next();
        List<Attribute> action2Attributes = action2.getActionAttributes();
        Map<Action, List<Attribute>> actionToActionAttsMapVar1=new HashMap<>();
        actionToActionAttsMapVar1.put(action1,action1Attributes);
        actionToActionAttsMapVar1.put(action2,action2Attributes);

        VirtualAccessRequest var1=new VirtualAccessRequest(agentAttributes,resourceAttributes,actionToActionAttsMapVar1,operationalMode);
        Edge edge1=new Edge(startVertex,intermediateVertex,0.5,"action",var1);
        Map<Edge,VirtualAccessRequest> map1=new HashMap<>();
        map1.put(edge1,var1);

        // I will add last action in the main method to var 2 (this action is: infuseDrug2)
        Action action3 = actionIterator.next();
        List<Attribute> action3Attributes = action3.getActionAttributes();
        Map<Action, List<Attribute>> actionToActionAttsMapVar2=new HashMap<>();
        actionToActionAttsMapVar2.put(action3,action3Attributes);

        VirtualAccessRequest var2=new VirtualAccessRequest(agentAttributes,resourceAttributes,actionToActionAttsMapVar2,operationalMode);
        Edge edge2=new Edge(intermediateVertex,targetVertex,0.5,"action",var2);
        Map<Edge,VirtualAccessRequest> map2=new HashMap<>();
        map2.put(edge2,var2);

        Queue<Map<Edge, VirtualAccessRequest>> varList_f=new LinkedList<>();
        varList_f.add(map1);
        varList_f.add(map2);
        Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>> map3=new HashMap<>();
        map3.put(targetVertex,varList_f);
        varList_v_start.add(map3);

        return varList_v_start;
    }

    private static Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> generateVarListForExperiment(int testCaseNo,
                                                                                                           int startVertexNo,
                                                                                                           int targetVertexNo,
                                                                                                           int[] finalStates) throws Exception {
        // following method generates an emergency sub-graph for test purposes
        Graph graph = com.nuray.gagm.test.Test.initializeGraph("sparse", testCaseNo, finalStates);

        VARGeneration varGeneration =new VARGeneration(graph,startVertexNo,targetVertexNo,finalStates);

        Set<Graph> subGraphSet=new HashSet<Graph>();
        subGraphSet.add(graph);

        Map<String, Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>>> varSet =
                varGeneration.generateVAR(subGraphSet);

        Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>> varSet_m = varSet.get("emergency");

        Vertex startVertex=new Vertex(1);
        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_start=new LinkedList<>();

        while(!varSet_m.isEmpty())
        {
            Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>> varList_v = varSet_m.poll();
            Iterator<Vertex> iterator = varList_v.keySet().iterator();

            while (iterator.hasNext())
            {
                Vertex sourceVertex = iterator.next();
                if(sourceVertex.equals(startVertex))
                {
                    varList_v_start = varList_v.get(sourceVertex);
                }
            }
        }

        return varList_v_start;
    }





}
