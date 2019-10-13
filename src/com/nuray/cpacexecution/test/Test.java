package com.nuray.cpacexecution.test;//package com.nuray.cpacexecution;

import com.nuray.cpacexecution.ExecutionOfCPAC;
import com.nuray.cpacexecution.cpacmodel.*;
import com.nuray.cpacexecution.enforcementfunctions.*;
import com.nuray.cpacexecution.storage.*;

import java.util.*;

public class Test {


    private static ResourceBase resourceBase;
    private static ActionBase actionBase;
    private static AgentBase agentBase;
    private static PolicyBase policyBase;
    private static SODBase sodBase;


    public static void main(String [] args) throws Exception {

        // 1. First, generate resource, action, agent, authorization policy, and SoD policy bases.
        resourceBase=new ResourceBase();
        actionBase=new ActionBase();
        agentBase=new AgentBase();

        policyBase=new PolicyBase();
        sodBase=new SODBase();

        // 2. Generate resource(s), action(s), agent(s), authorization policy rule(s), and SoD policy rule(s)
            // 2a. Generate a resource
        Resource resource=new Resource("patient123","human");

        // this is for being able to define a role for the human resource above:
        Agent fakeAgent=new Agent("patient123","human");
        fakeAgent.getRole().setAttributeValueCategorical("human");
        resource.addResourceAttribute(fakeAgent.getRole());

            // 2b. Generate actions
        Action action1=new Action("deliverHeparin","cyber-physical");
        Action action2=new Action("infuseDrug1","cyber-physical");
        Action action3=new Action("infuseDrug2","cyber-physical");


        // 2b. Generate an agent
        Agent agent=new Agent("heparinDevice1","physical");


            // 2c. Generate a policy
        Policy policy=generatePolicy();

            // 2d. Generate an sod policy
        SODPolicy sodPolicy=generateSoDPolicy();

        // 3. Add policy elements to repositories
        resourceBase.addResource(resource);
        agentBase.addAgent(agent);
        actionBase.addAction(action1);
        actionBase.addAction(action2);
        actionBase.addAction(action3);
        policyBase.addPolicy(policy);
        sodBase.addSODPolicy(sodPolicy);

        // 4. call executeCPAC algorithm
        ExecutionOfCPAC executionOfCPAC=new ExecutionOfCPAC(resourceBase,actionBase,agentBase,policyBase,sodBase);

        executionOfCPAC.executeCPAC()




    }


    public static Policy generatePolicy() throws Exception {
        // 2c. Generate a policy rule and add it to a policy

        // 2c.1. generate resource attributes with their matching functions to be used in the policy rule

        List<Map<Attribute, String>> resourceAttributes = generateResourceAttributes();
        Map<String, List<Map<Attribute, String>>> resourceAttMatchFuncListMap=new HashMap<>();
        resourceAttMatchFuncListMap.put("resource group1",resourceAttributes); // this is a <Resource></Resource> group in XACML

        // 2c.2. generate agent attributes to be used in the policy rule
        List<Map<Attribute,String>> agentAttributes=generateAgentAttributes();
        Map<String, List<Map<Attribute, String>>> agentAttMatchFuncListMap=new HashMap<>();
        agentAttMatchFuncListMap.put("agent group 1",agentAttributes);

        // 2c.3. generate environment attributes to be used in the policy rule
            // will be null for our sample CPAC policy

        // 2c.4. generate action attributes to be used in the policy rule
        List<Map<Attribute,String>> actionAttributes=generateActionAttributes();
        Map<String, List<Map<Attribute, String>>> actionAttMatchFuncListMap=new HashMap<>();
        actionAttMatchFuncListMap.put("action group 1",actionAttributes);


        // 2c.5. Map attributes to their attribute matching functions (for XACML evaluation)


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

        Attribute ACT_ref=new Attribute("ACT_ref",new AttributeRange(200,250),"numeric");

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

    private static List<Map<Attribute, String>> generateActionAttributes() throws Exception {
        Attribute actionID=new Attribute("actionID",null,"categorical");
        actionID.setAttributeValueCategorical("deliverHeparin");

        // 2c.5. Map attributes to their attribute matching functions (for XACML evaluation)
            // for action attributes

        Map<Attribute,String> actionIDToMatchingFunctionMap=mapAttributesToMatchingFunctions(actionID,
                "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case");

        List<Map<Attribute, String>> actionAttToMatchingFunctions=new LinkedList<>();
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





}
