package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.CPACSpecifications;
import com.nuray.cpacexecution.cpacmodel.Resource;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.cpacexecution.storage.ResourceBase;

import java.util.*;

public class PolicyRule {
//
//    private Map<Attribute, String> resourceAttributes; // This a map between a resource attribute and its matching function in XACML rule,
//                                                        // such as:  urn:oasis:names:tc:xacml:1.0:function:string-equal
//    private Map<Attribute, String> agentAttributes; // This a map between an agent attribute and its matching function in XACML rule
//    private Map<Attribute, String> actionAttributes; // This a map between an action attribute and its matching function in XACML rule
//    private Map<Attribute, String> environmentAttributes; // This a map between an env attribute and its matching function in XACML rule

    private Map<String, List<Map<Attribute,String>>> agentAttributeMap; // This a map between a group number and a set of
    // <attribute, matching function> pairs. More precisely, this is used to represent different <Subject></Subject> groups in
    // an XACML file. In a <Subject></Subject> group, there may be multiple values of a single attribute, specified between
    // separate <SubjectMatch and </SubjectMatch> tags (refer to IIA006Policy.xml and IIA001Policy.xml files to understand better)

    // Example to a matching function in XACML rule:  urn:oasis:names:tc:xacml:1.0:function:string-equal
    // (look at XACMLSpecifications.java file)

    // Earlier, I defined this as Map<Attribute, String>, but in that case multiple subjects (or multiple occurence of the same
    // subject attribute) are not allowed (a policy may say that allow access to subjects with subjectID=... and also to subjects
    // with subjectID= .... (that points to multiple subjects).
    private Map<String, List<Map<Attribute,String>>> resourceAttributeMap; // A policy tule may also include multiple resources based on
    // the XACML specification. (refer to IIB044Policy.xml file to understand better)
    private Map<String, List<Map<Attribute,String>>> actionAttributeMap;
    private Map<String, List<Map<Attribute,String>>> environmentAttributeMap;



    private String ruleID;
    private String ruleEffect;

    public PolicyRule(Map<String, List<Map<Attribute,String>>> resourceAttributeMap,
                      Map<String, List<Map<Attribute,String>>> agentAttributeMap,
                      Map<String, List<Map<Attribute,String>>>actionAttributeMap,
                      Map<String, List<Map<Attribute,String>>> environmentAttributeMap,
                      String operationalMode,
                      String ruleID, String ruleEffect)
    {
        if(!CPACSpecifications.MO.contains(operationalMode))
        {
            throw new IllegalArgumentException("Operational mode should be one of the values specified in" +
                    " \"CPACSpecifications.java\" file, i.e. active, passive, autonomous, or emergency"+ operationalMode);
        }
        if(!EnforcementSpecifications.policyRuleEffects.contains(ruleEffect))
        {
            throw new IllegalArgumentException("Rule effect should be one of the values specified in" +
                    " \"EnforcementSpecifications.java\" file, i.e. permit or deny"+ ruleEffect);
        }
        validateAttributeMap(resourceAttributeMap);
        validateAttributeMap(agentAttributeMap);
        validateAttributeMap(actionAttributeMap);
        validateAttributeMap(environmentAttributeMap);

        this.resourceAttributeMap=resourceAttributeMap;
        this.agentAttributeMap=agentAttributeMap;
        this.actionAttributeMap=actionAttributeMap;
        this.environmentAttributeMap=environmentAttributeMap;

        this.ruleID=ruleID;
        this.ruleEffect=ruleEffect;
    }

    /**
     * This method is used to check if a given attribute to be used in a policy rule has proper function
     * that is for comparing the attribute value in the policy to the attribute value in a request
     * @param attributeToMatchFunctionMap
     */
    private void validateAttributeMap(Map<String, List<Map<Attribute,String>>> attributeToMatchFunctionMap)
    {
        if(attributeToMatchFunctionMap!=null)
        {
            Set<String> subjectGroups = attributeToMatchFunctionMap.keySet(); //<Subject></Subject> groups in an XACML file

            Iterator<String> iterator = subjectGroups.iterator();

            while (iterator.hasNext())
            {
                String nextSubjectGroup = iterator.next();
                List<Map<Attribute, String>> attributeMatchingFunctionMap = attributeToMatchFunctionMap.get(nextSubjectGroup);

                for (Map<Attribute,String> map:attributeMatchingFunctionMap)
                {
//                Map.Entry<Attribute, String> attributeMatchingFunctionEntry = map.entrySet().iterator().next();
//                Attribute attribute = attributeMatchingFunctionEntry.getKey();
//                String attributeType = attribute.getAttributeType();

                    Attribute attribute = map.keySet().iterator().next();
                    String attributeType =attribute.getAttributeType();
                    String attributeMatchingFunction = map.get(attribute);

//                String attributeMatchingFunction = attributeMatchingFunctionEntry.getValue();

                    if(attributeType.equalsIgnoreCase("categorical"))
                    {
                        if(!XACMLSpecifications.STRING_MATCH_FUNCTIONS.contains(attributeMatchingFunction))
                        {
                            final StringBuilder stringBuilder=new StringBuilder();
                            XACMLSpecifications.STRING_MATCH_FUNCTIONS.forEach(l -> stringBuilder.append(l+"\n "));

                            String possibleStringMatchFunctions=stringBuilder.toString();

                            throw new IllegalArgumentException("String matching function should be one of the values specified in" +
                                    " \"XACMLSpecifications.java\" file, i.e. one of the followings: \n"+ possibleStringMatchFunctions);
                        }
                    }
                    else if (attributeType.equalsIgnoreCase("numerical"))
                    {
                        if(!XACMLSpecifications.DOUBLE_MATCH_FUNCTIONS.contains(attributeMatchingFunction))
                        {
                            final StringBuilder stringBuilder=new StringBuilder();
                            XACMLSpecifications.DOUBLE_MATCH_FUNCTIONS.forEach(l -> stringBuilder.append(l+"\n "));

                            String possibleDoubleMatchFunctions=stringBuilder.toString();

                            throw new IllegalArgumentException("Double matching function should be one of the values specified in" +
                                    " \"XACMLSpecifications.java\" file, i.e. one of the followings: \n"+ possibleDoubleMatchFunctions);
                        }
                    }
                    else if (attributeType.equalsIgnoreCase("date"))
                    {
                        final StringBuilder stringBuilder=new StringBuilder();
                        XACMLSpecifications.DATE_MATCH_FUNCTIONS.forEach(l -> stringBuilder.append(l+"\n "));

                        String possibleDateMatchFunctions=stringBuilder.toString();

                        throw new IllegalArgumentException("Date matching function should be one of the values specified in" +
                                " \"XACMLSpecifications.java\" file, i.e. one of the followings: \n"+ possibleDateMatchFunctions);
                    }
                    else if (attributeType.equalsIgnoreCase("time"))
                    {
                        final StringBuilder stringBuilder=new StringBuilder();
                        XACMLSpecifications.TIME_MATCH_FUNCTIONS.forEach(l -> stringBuilder.append(l+"\n "));

                        String possibleTimeMatchFunctions=stringBuilder.toString();

                        throw new IllegalArgumentException("Time matching function should be one of the values specified in" +
                                " \"XACMLSpecifications.java\" file, i.e. one of the followings: \n"+ possibleTimeMatchFunctions);

                    }
                }
            }
        }
    }

    public String getRuleID()
    {
        return ruleID;
    }

    public String getRuleEffect()
    {
        return ruleEffect;
    }

    public Map<String, List<Map<Attribute, String>>> getResourceAttributes() {
        return resourceAttributeMap;
    }

    public Map<String, List<Map<Attribute, String>>> getAgentAttributes() {
        return agentAttributeMap;
    }

    public Map<String, List<Map<Attribute, String>>> getEnvironmentAttributes() {
        return environmentAttributeMap;
    }

    public Map<String, List<Map<Attribute, String>>> getActionAttributes() {
        return actionAttributeMap;
    }

    public List<Permission> getPermissionsFromRule(ResourceBase resourceBase, ActionBase actionBase) throws Exception
    {
        List<Permission> permissionList=new ArrayList<>();

        Iterator<String> actionIterator = actionAttributeMap.keySet().iterator();
        List<Action> actionList=new ArrayList();

        while (actionIterator.hasNext())
        {
            String actionGroup = actionIterator.next();
            List<Map<Attribute, String>> actionAttMatchFunctionPairs = actionAttributeMap.get(actionGroup);

            for (Map<Attribute,String> attMFPair:actionAttMatchFunctionPairs)
            {
                Attribute actionAtt = attMFPair.keySet().iterator().next(); // the map here is a single-element map (a single resource att
                //and its value, so we only get a single element using "next" method

                String attributeName = actionAtt.getAttributeName();

                Action action=null;
                if(attributeName.equalsIgnoreCase("actionID"))
                {
                    String actionIDVal = actionAtt.getAttributeValueCategorical();
                    action=actionBase.getActionWithActionID(actionIDVal);
                }

                if(!actionList.contains(action))
                {
                    actionList.add(action);
                }
            }
        }


        Iterator<String> resourceIterator = resourceAttributeMap.keySet().iterator(); // iterator will return a map for each
                                                                            // <Resource> </Resource> group
        List<Resource> resourceList=new ArrayList<>();

        while (resourceIterator.hasNext())
        {
            String resourceGroup = resourceIterator.next();
            List<Map<Attribute, String>> resourceAttMatchFunctionPairs = resourceAttributeMap.get(resourceGroup);

            for (Map<Attribute,String> attMFPair:resourceAttMatchFunctionPairs)
            {
                Attribute resourceAtt = attMFPair.keySet().iterator().next(); // the map here is a single-element map (a single resource att
                //and its value, so we only get a single element using "next" method

                String attributeName = resourceAtt.getAttributeName();

                Resource resource=null;
                if(attributeName.equalsIgnoreCase("resourceID"))
                {
                    String resourceIDVal = resourceAtt.getAttributeValueCategorical();
                    resource = resourceBase.getResourceWithResourceID(resourceIDVal);
                    if(!resourceList.contains(resource))
                    {
                        resourceList.add(resource);
                    }

                    Permission permission=new Permission(resource,actionList);
                    permissionList.add(permission);
                }


            }

        }
        return permissionList;
    }
}
