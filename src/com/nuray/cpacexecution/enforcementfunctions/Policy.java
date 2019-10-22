package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Attribute;

import java.time.LocalTime;
import java.util.*;

public class Policy {

    protected List<PolicyRule> policyRules;
    protected String ruleCombiningAlgorithm;

    protected String policyID;

    protected String policyContentXACML;


    public Policy(String policyID, String ruleCombiningAlg, List<PolicyRule> policyRules) throws Exception {
        if(!XACMLSpecifications.RULE_COMBINIG_ALGORITHMS.contains(ruleCombiningAlg))
        {
            final StringBuilder stringBuilder=new StringBuilder();
            XACMLSpecifications.RULE_COMBINIG_ALGORITHMS.forEach(l -> stringBuilder.append(l+"\n "));

            String possibleRuleCombiningAlgorithms=stringBuilder.toString();


            throw new IllegalArgumentException("Rule combining algorithm should be one of the values specified in" +
                    " \"XACMLSpecifications.java\" file, i.e. one of the followings: \n"+ possibleRuleCombiningAlgorithms);
        }
        this.policyRules=policyRules;
        this.ruleCombiningAlgorithm=ruleCombiningAlg;
        this.policyID=policyID;

        policyContentXACML=constructXACMLPolicy();
    }

    public String getRuleCombiningAlgorithm()
    {
        return ruleCombiningAlgorithm;
    }

    public List<PolicyRule> getPolicyRules()
    {
        return policyRules;
    }

    public String getPolicyID()
    {
        return policyID;
    }

    public String getPolicyContentXACML()
    {
        return policyContentXACML;
    }

    public void setPolicyContentXACML(String policyContentXACML) {
        this.policyContentXACML = policyContentXACML;
    }

    public String constructXACMLPolicy() throws Exception
    {
        String policyXACML="";

        //policy start line
        String policyStart="<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
                "PolicyId=\""+policyID+"\" " +
                "RuleCombiningAlgId=\""+ruleCombiningAlgorithm+"\" " +
                "Version=\"1.0\">\n";

                policyStart=
                        "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  PolicyId=\""+policyID+"\" RuleCombiningAlgId=\""+ruleCombiningAlgorithm+"\" Version=\"1.0\">\n";

                policyXACML+=policyStart;

        // policy target lines (for now, there is no constraint in a policy target
        String targetLines=
                "    <Target>\n" +
                "        <Subjects>\n" +
                "            <AnySubject/>\n" +
                "        </Subjects>\n" +
                "        <Resources>\n" +
                "            <AnyResource/>\n" +
                "        </Resources>\n" +
                "        <Actions>\n" +
                "            <AnyAction/>\n" +
                "        </Actions>\n" +
                "    </Target>\n";

        targetLines="   <Target></Target>\n" ;

        policyXACML+=targetLines;

        String ruleLines="";

        for (PolicyRule rule:policyRules)
        {
            // rule start line
            String ruleStart=
                    "    <Rule\n" +
                    "          RuleId=\""+rule.getRuleID()+"\"\n" +
                    "          Effect=\""+rule.getRuleEffect()+"\">\n";

            ruleStart="   <Rule Effect=\""+rule.getRuleEffect()+"\" RuleId=\""+rule.getRuleID()+"\">\n";

                    // rule target lines

            String ruleTargetStart="      <Target>\n";

            String ruleSubjectsStart="            <Subjects>\n";
            ruleSubjectsStart="         <AnyOf>\n";
            String targetSubjectLines=constructTargetSubject(rule);
            String ruleSubjectsEnd="                </Subjects>\n";
            ruleSubjectsEnd="         </AnyOf>\n";

            String ruleResourcesStart="            <Resources>\n";
            ruleResourcesStart="         <AnyOf>\n";
            String targetResourceLines=constructTargetResource(rule);
            String ruleResourcesEnd="            </Resources>\n";
            ruleResourcesEnd="         </AnyOf>\n";

            String ruleTargetEnd="      </Target>\n";

            // actions are written into the condition part of policy rules (e.g. if actionID=..., permit a request)
            String ruleConditionsStart="      <Condition>\n";
            String ruleConditionOnActions = constructConditionOnActions(rule);
            String ruleConditionsEnd="      </Condition>\n";


//            String ruleActionsStart="            <Actions>\n";
//            ruleActionsStart="         <AnyOf>\n";
//            String targetActionLines=constructTargetAction(rule);
//            String ruleActionsEnd="            </Actions>\n";
//            ruleActionsEnd="         </AnyOf>\n";

            String ruleEnd="   </Rule>\n";


            ruleLines+=ruleStart+ruleTargetStart+ruleSubjectsStart+targetSubjectLines+ruleSubjectsEnd
                    +ruleResourcesStart+targetResourceLines+ruleResourcesEnd+ruleTargetEnd
                    +ruleConditionsStart+ruleConditionOnActions+ruleConditionsEnd+ruleEnd;

//            ruleLines+=ruleStart+ruleTargetStart+ruleSubjectsStart+targetSubjectLines+ruleSubjectsEnd
//                    +ruleResourcesStart+targetResourceLines+ruleResourcesEnd
//                    +ruleActionsStart+targetActionLines+ruleActionsEnd
//                    +ruleTargetEnd+ruleEnd;
        }

        policyXACML+=ruleLines;
        policyXACML+="</Policy>        ";

        return policyXACML;
    }

    private String constructConditionOnActions(PolicyRule rule) throws Exception {
        String attributeBlock = "";

        Map<String, List<Map<Attribute, String>>> actionAttributeMap = rule.getActionAttributes();

        Set<String> actionGroups = actionAttributeMap.keySet(); //<Action></Action> groups in XACML
        Iterator<String> iterator = actionGroups.iterator();

        while (iterator.hasNext()) {
            String nextActionGroup = iterator.next();
            List<Map<Attribute, String>> attributeMatchingFunctionMap = actionAttributeMap.get(nextActionGroup);

            for (Map<Attribute, String> map : attributeMatchingFunctionMap) {
                String matchFunction = getMatchingFunctionFromPolicyRuleMap(map);

                Attribute attribute = getAttributeFromPolicyRuleMap(map);
                String attributeName = attribute.getAttributeName();
                String dataType = attribute.getDataType();
                String attributeType = attribute.getAttributeType();

                attributeBlock += "         <Apply FunctionId=\"" + matchFunction + "\">\n";
                if(attributeType.equalsIgnoreCase("categorical"))
                {
                    attributeBlock+="            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\">\n";
                }
                else if (attributeType.equalsIgnoreCase("numeric"))
                {
                    attributeBlock+="            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:double-one-and-only\">\n";
                }
                else if (attributeType.equalsIgnoreCase("date"))
                {
                    attributeBlock+="            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:date-one-and-only\">\n";
                }
                else if (attributeType.equalsIgnoreCase("time"))
                {
                    attributeBlock+="            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:time-one-and-only\">\n";
                }

                attributeBlock+="               <AttributeDesignator AttributeId=\""+attributeName+"\"" +
                                " Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\"" +
                                " DataType=\""+dataType+"\" MustBePresent=\"true\"></AttributeDesignator>\n";



                attributeBlock+=
                                "            </Apply>\n" +
                                "            <AttributeValue DataType=\""+dataType+"\">";

                attributeBlock = appendAttributeValueToRuleBlock(attribute, attributeBlock);

                attributeBlock+=
                                "</AttributeValue>\n" +
                                "         </Apply>\n";
            }
        }
        return attributeBlock;
    }



    /**
     * !!!!Note: functions for rule matching (Part that starts with "MatchId") may need to be changed later, based on
     * the policy rules I generate for experiments!!!!!
     * @param rule
     * @return
     */
    private String constructTargetSubject(PolicyRule rule) throws Exception {
        String attributeBlock="";

        Map<String, List<Map<Attribute, String>> > agentAttributeMap=rule.getAgentAttributes();

        Set<String> subjectGroups = agentAttributeMap.keySet(); //<Subject></Subject> groups in XACML
        Iterator<String> iterator = subjectGroups.iterator();

        while (iterator.hasNext())
        {
            String nextSubjectGroup = iterator.next();
            List<Map<Attribute, String>> attributeMatchingFunctionMap = agentAttributeMap.get(nextSubjectGroup);

            for (Map<Attribute,String> map:attributeMatchingFunctionMap)
            {
//                attributeBlock+="                <Subject>\n";
                attributeBlock+="            <AllOf>\n";
                String matchFunction = getMatchingFunctionFromPolicyRuleMap(map);

                Attribute attribute = getAttributeFromPolicyRuleMap(map);
                String attributeName=attribute.getAttributeName();
                String dataType=attribute.getDataType();

//                attributeBlock+=
//                        "                    <SubjectMatch\n" +
//                                "                          MatchId=\""+matchFunction+"\">\n" +
//                                "                        <AttributeValue\n" +
//                                "                              DataType=\""+dataType+"\">"+"";

                attributeBlock+=
                                "       <Match MatchId=\""+matchFunction+"\">\n" +
                                "                  <AttributeValue DataType=\""+dataType+"\">";

                        attributeBlock=appendAttributeValueToRuleBlock(attribute,attributeBlock);

//                attributeBlock+=
//                                "</AttributeValue>\n" +
//                                "                        <SubjectAttributeDesignator\n" +
////                                "                              SubjectCategory=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\"\n" +
//                                "                              AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:"+attributeName+"\"\n" +
//                                "                              MustBePresent=\"true\"\n" +
//                                "                              DataType=\""+dataType+"\"/>\n" +
//                                "                    </SubjectMatch>\n";

                attributeBlock+=
                        "</AttributeValue>\n" +
                        "                  <AttributeDesignator AttributeId=\""+attributeName+"\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:subject\" DataType=\""+dataType+"\" MustBePresent=\"true\"></AttributeDesignator>\n";

//                                attributeBlock+="                </Subject>\n";
                attributeBlock+=
                        "               </Match>\n" +
                        "            </AllOf>\n";
            }

        }

        return attributeBlock;
    }

    private String constructTargetResource(PolicyRule rule) throws Exception {

        String attributeBlock="";

        Map<String, List<Map<Attribute, String>> > resourceAttributeMap=rule.getResourceAttributes();

        Set<String> resourceGroups = resourceAttributeMap.keySet(); //<Subject></Subject> groups in XACML
        Iterator<String> iterator = resourceGroups.iterator();

        while (iterator.hasNext())
        {
            String nextResourceGroup = iterator.next();
            List<Map<Attribute, String>> attributeMatchingFunctionMap = resourceAttributeMap.get(nextResourceGroup);

            for (Map<Attribute,String> map:attributeMatchingFunctionMap)
            {
//                attributeBlock+="                <Resource>\n";
                attributeBlock+="            <AllOf>\n";
                String matchFunction = getMatchingFunctionFromPolicyRuleMap(map);

                Attribute attribute = getAttributeFromPolicyRuleMap(map);
                String attributeName=attribute.getAttributeName();
                String dataType=attribute.getDataType();

//                attributeBlock+=
//                        "                    <ResourceMatch\n" +
//                                "                          MatchId=\""+matchFunction+"\">\n" +
//                                "                        <AttributeValue\n" +
//                                "                              DataType=\""+dataType+"\">"+"";
                attributeBlock+=
                        "       <Match MatchId=\""+matchFunction+"\">\n" +
                                "                  <AttributeValue DataType=\""+dataType+"\">";

                attributeBlock=appendAttributeValueToRuleBlock(attribute,attributeBlock);

//                attributeBlock+=
//                                "</AttributeValue>\n" +
//                                "                        <ResourceAttributeDesignator\n" +
////                                "                              ResourceCategory=\"urn:oasis:names:tc:xacml:1.0:resource-category:access-resource\"\n" +
//                                "                              AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:"+attributeName+"\"\n" +
//                                "                              MustBePresent=\"true\"\n" +
//                                "                              DataType=\""+dataType+"\"/>\n" +
//                                "                    </ResourceMatch>\n";
                attributeBlock+=
                        "</AttributeValue>\n" +
                                "                  <AttributeDesignator AttributeId=\""+attributeName+"\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\""+dataType+"\" MustBePresent=\"true\"></AttributeDesignator>\n";

//                attributeBlock+="                </Resource>\n";
                attributeBlock+=
                        "               </Match>\n" +
                                "            </AllOf>\n";
            }
        }

        return attributeBlock;
    }

    private String constructTargetAction(PolicyRule rule) throws Exception {

        String attributeBlock="";

        Map<String, List<Map<Attribute, String>> > actionAttributeMap=rule.getActionAttributes();

        Set<String> actionGroups = actionAttributeMap.keySet(); //<Action></Action> groups in XACML
        Iterator<String> iterator = actionGroups.iterator();

        while (iterator.hasNext())
        {
            String nextActionGroup = iterator.next();
            List<Map<Attribute, String>> attributeMatchingFunctionMap = actionAttributeMap.get(nextActionGroup);

            for (Map<Attribute,String> map:attributeMatchingFunctionMap)
            {
//                attributeBlock+="                <Action>\n";
                attributeBlock+="            <AllOf>\n";
                String matchFunction = getMatchingFunctionFromPolicyRuleMap(map);

                Attribute attribute = getAttributeFromPolicyRuleMap(map);
                String attributeName=attribute.getAttributeName();
                String dataType=attribute.getDataType();

//                attributeBlock+=
//                        "                    <ActionMatch\n" +
//                                "                          MatchId=\""+matchFunction+"\">\n" +
//                                "                        <AttributeValue\n" +
//                                "                              DataType=\""+dataType+"\">"+"";
                attributeBlock+=
                        "       <Match MatchId=\""+matchFunction+"\">\n" +
                                "                  <AttributeValue DataType=\""+dataType+"\">";

                attributeBlock=appendAttributeValueToRuleBlock(attribute,attributeBlock);

//                attributeBlock+=
//                                "</AttributeValue>\n" +
//                                "                        <ActionAttributeDesignator\n" +
////                                "                              SubjectCategory=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\"\n" +
//                                "                              AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:"+attributeName+"\"\n" +
//                                "                              MustBePresent=\"true\"\n" +
//                                "                              DataType=\""+dataType+"\"/>\n" +
//                                "                    </ActionMatch>\n";

                attributeBlock+=
                        "</AttributeValue>\n" +
                                "                  <AttributeDesignator AttributeId=\""+attributeName+"\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\""+dataType+"\" MustBePresent=\"true\"></AttributeDesignator>\n";

//                attributeBlock+="                </Action>\n";
                attributeBlock+=
                        "               </Match>\n" +
                                "            </AllOf>\n";
            }
        }

        return attributeBlock;
    }


    private String appendAttributeValueToRuleBlock(Attribute attribute, String attributeBlock) throws Exception {
        String attributeType = attribute.getAttributeType();

        if(attributeType.equalsIgnoreCase("categorical"))
        {
            String attributeValueCategorical = attribute.getAttributeValueCategorical();
            attributeBlock=attributeBlock+attributeValueCategorical;
        }
        else if(attributeType.equalsIgnoreCase("numeric"))
        {
            double attributeValueNumeric = attribute.getAttributeValueNumeric();
            attributeBlock=attributeBlock+attributeValueNumeric;
        }
        else if(attributeType.equalsIgnoreCase("date"))
        {
            Date attributeValDate = attribute.getAttributeValDate();
            attributeBlock=attributeBlock+attributeValDate;
        }
        else if(attributeType.equalsIgnoreCase("time"))
        {
            LocalTime attributeValTime = attribute.getAttributeValTime();
            attributeBlock = attributeBlock+attributeValTime;
        }

        return attributeBlock;
    }

    private Attribute getAttributeFromPolicyRuleMap(Map<Attribute,String> map)
    {
        Map.Entry<Attribute, String> attributeMatchingFunctionEntry = map.entrySet().iterator().next();
        Attribute attribute = attributeMatchingFunctionEntry.getKey();

        return attribute;
    }

    private String getMatchingFunctionFromPolicyRuleMap(Map<Attribute,String> map)
    {
        Map.Entry<Attribute, String> attributeMatchingFunctionEntry = map.entrySet().iterator().next();
        String matchFunction = attributeMatchingFunctionEntry.getValue();

        return matchFunction;
    }
}
