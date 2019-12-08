package com.nuray.cpacexecution.test;

import com.nuray.cpacexecution.cpacmodel.Agent;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.AttributeRange;
import com.nuray.cpacexecution.enforcementfunctions.PolicyRule;

import java.sql.SQLOutput;
import java.util.*;

public class SimpleTest {

    public static void main(String[] args) throws Exception {
        Agent agent=new Agent("agn1","human");

        AttributeRange ar=new AttributeRange(5,10);
        Attribute attribute=new Attribute("name",ar,"numeric");
        attribute.setAttributeValueNumeric(7);

        agent.addAgentAttribute(attribute);

        Attribute attToTest=new Attribute("name",ar,"numeric");
        attToTest.setAttributeValueNumeric(7);

        System.out.println(agent.hasAttribute(attToTest));


    }


}
