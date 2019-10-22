package com.nuray.cpacexecution.enforcementfunctions;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.cpacmodel.Resource;
import com.nuray.cpacexecution.storage.ResourceBase;

import java.time.LocalTime;
import java.util.*;

public abstract class Request {

    protected int requestId;
    protected String requestContentXACML;
    protected Permission permission;
    protected String operationalMode;

    List<Attribute> resourceAttributes;
    List<Attribute> agentAttributes;
    Map<Action,List<Attribute>> actionAttributes; //note that there may be multiple actions, so this is a map from an action to its attributes

    public Request() throws Exception {
        resourceAttributes=new ArrayList<>();
        agentAttributes=new ArrayList<>();
        actionAttributes=new HashMap<>();
        requestId++;
//        requestContentXACML=constructXACMLRequest();
    }

    public Request(String operationalMode) throws Exception {

        this();
        this.operationalMode=operationalMode;

    }

    @Override
    public boolean equals(Object obj)
    {
        Request other = (Request) obj;
        return other.requestId==this.requestId;
    }

    public List<Attribute> getAgentAttributes()
    {
        return agentAttributes;
    }

    public List<Attribute> getResourceAttributes()
    {
        return resourceAttributes;
    }

    public Map<Action, List<Attribute>> getActionAttributes() {
        return actionAttributes;
    }

    public Permission getPermission()
    {
        return this.permission;
    }

    public String getOperationalMode()
    {
        return operationalMode;
    }

    public int getRequestId()
    {
        return requestId;
    }

    public Permission extractPermission(ResourceBase resourceBase) throws Exception
    {
        Resource resource=null;
        List<Action> actionsInPermission=new ArrayList<>();

        for (Attribute attribute:resourceAttributes)
        {
            if(attribute.getAttributeName()=="resourceID")
            {
                resource = resourceBase.getResourceWithResourceID(attribute.getAttributeValueCategorical());
            }
        }

        Iterator<Action> iterator = actionAttributes.keySet().iterator();

        while (iterator.hasNext())
        {
            Action action=iterator.next();
            actionsInPermission.add(action);
        }
        Permission permission=new Permission(resource,actionsInPermission);

        return permission;
    }

    public String getRequestContentXACML()
    {
        return requestContentXACML;
    }

    /**
     * This function is used to construct an XACML request from an AAR (Agent-initated Access Request).
     * To use this method, "enhanceIAR()" method should be used first in order to enhance a given
     * IAR (initial access request) and obtained an AAR.
     *
     * @return the constructed XACML request from the AAR
     * @throws Exception
     */
    public String constructXACMLRequest() throws Exception {

        //request start line
        String startLine="<Request CombinedDecision=\"false\" ReturnPolicyIdList=\"false\" xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\">";
        String subjectLines=constructSubjectPartOfRequest();
        String resourceLines=constructResourcePartOfRequest();
        String actionLines=constructActionPartOfRequest();

        return startLine+"\n"+subjectLines+"\n"+resourceLines+"\n"+actionLines+"\n</Request>";
    }

    private String constructSubjectPartOfRequest() throws Exception {

        String subjectXACML=" <Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:subject\">\n" ;

        String agentAttributeBlock = constructAttributes(agentAttributes,false,true,false);

        subjectXACML=subjectXACML+agentAttributeBlock;

        subjectXACML= subjectXACML + "   </Attributes>";

        return subjectXACML;
    }

    private String constructResourcePartOfRequest() throws Exception {

        String resourceXACML="    <Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n";

        String resourceAttributeBlock = constructAttributes(resourceAttributes,true,false,false);

        resourceXACML=resourceXACML+resourceAttributeBlock;

        resourceXACML= resourceXACML + "   </Attributes>";

        return resourceXACML;
    }

    private String constructActionPartOfRequest() throws Exception {

        String actionXACML="    <Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n";

        Set<Action> actionSet = actionAttributes.keySet();
        Iterator<Action> actionIterator = actionSet.iterator();

        String actionAttributeBlock ="";

        while (actionIterator.hasNext())
        {
            Action nextAction = actionIterator.next();
            List<Attribute> nextActionAttributes = nextAction.getActionAttributes();

            actionAttributeBlock = constructAttributes(nextActionAttributes,false,false,true);
            actionXACML=actionXACML+actionAttributeBlock;
        }

        actionXACML= actionXACML + "   </Attributes>";

        return actionXACML;
    }


    /**
     * This is a helper method used in "constructSubjectPartOfRequest", "constructResourcePartOfRequest", and
     * "constructActionPartOfRequest" methods to eliminate repetitive code. Basically, it constructs an "attribute block"
     * as follows:
     *
     *           <Attribute AttributeId="http://sample/ATTRIBUTE1" IncludeInResult="false">
     *                           <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">abc</AttributeValue>
     *           </Attribute>
     *
     * @param attribute
     * @return
     */
    private String constructAttributeBlock(Attribute attribute, boolean isResourceAtt,
                                           boolean isSubjectAtt, boolean isActionAtt) throws Exception {
        String attributeBlock="";
        if(!attribute.isValueSet()==false)
        {
            String attributeName=attribute.getAttributeName();;
            String attributeType=attribute.getAttributeType();
            String dataType=attribute.getDataType();

            if(isResourceAtt)
            {
                attributeBlock= "        <Attribute AttributeId=\"" +attributeName+"\" IncludeInResult=\"false\">\n";
            }
            else if(isSubjectAtt)
            {
                attributeBlock= "        <Attribute AttributeId=\""+attributeName+"\" IncludeInResult=\"false\">\n";

            }
            else if (isActionAtt)
            {
                attributeBlock= "        <Attribute AttributeId=\""+attributeName+"\" IncludeInResult=\"false\">\n";

            }
            else
            {
                throw new IllegalArgumentException("Attribute can be either resource attribute, " +
                        "subject attribute, or action attribute");
            }


            attributeBlock= attributeBlock + "         <AttributeValue DataType=\""+dataType+"\">";

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

            attributeBlock=attributeBlock+"</AttributeValue>\n" + "      </Attribute>\n";

        }

        return attributeBlock;
    }


    /**
     * This method constructs multiple blocks of attributes in XACML format for a given category.
     * For example, if a list of resource attributes is given as parameter to the method, it constructs
     * resource block of an XACML request with multiple resource attributes.
     * @param attributeList
     * @return
     */
    private String constructAttributes(List<Attribute> attributeList, boolean isResourceAtt,
                                       boolean isSubjectAtt, boolean isActionAtt) throws Exception {
        String XACMLblock="";
        for (Attribute attribute:attributeList)
        {
            String attributeBlock = constructAttributeBlock(attribute,isResourceAtt,isSubjectAtt,isActionAtt);
            XACMLblock= XACMLblock+attributeBlock;
        }

        return XACMLblock;
    }
}
