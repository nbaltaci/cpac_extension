package com.nuray.cpacexecution.cpacmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Agent {
    private List<Attribute> agentAttributes;

    /*
    note that 'agentId' is not the same as 'agent_id' attribute.
    'agentId' is an int that is simply used to identify the count of attribute whereas
    'agent_id' is a string that is used to describe the agent itself (such as username for a human agent,
    machine ID for a physical resource (e.g. heparinDevice1), and any identification for a cyber resource
    (e.g. fileName, applicationName etc.).
     */
    private int agentId;
    private String agentID;
    private String type;

    private Attribute userName;
    private Attribute email;
    private Attribute role;
    private Attribute age;

    /**
     *
     * @param agentID: This basically represents the name of the agent. An example for a human agent: "bs@simpsons.com"
     * @param type: This corresponds to the type of agent. Valid options are: "human", "physical", and "cyber".
     *           (see CPACSpecifications.java)
     */
    public Agent(String agentID, String type) throws Exception {
        if(!CPACSpecifications.elementTypes.contains(type))
        {
            throw new IllegalArgumentException("Type should be one of the values specified in" +
                    " \"CPACSpecifications.java\" file, i.e. human, physical, or cyber"+ type);
        }
        this.agentID=agentID;
        this.type=type;
        agentId++;

        userName=new Attribute("agentID",null,"categorical");
        email=new Attribute("email",null,"categorical");
        role=new Attribute("role",null,"categorical");
        age=new Attribute("age",new AttributeRange(15.0,85.00),"numeric");

        userName.setAttributeValueCategorical(agentID);

        agentAttributes=new ArrayList<>(Arrays.asList(new Attribute[]{userName,email,role,age}));

    }

    @Override
    public boolean equals(Object obj)
    {
        Agent other = (Agent) obj;
        return other.agentId==this.agentId;
    }

    public String getAgentID()
    {
        return agentID;
    }

    public int getAgentId() {
        return agentId;
    }

    public Attribute getUserName()
    {
        return userName;
    }

    public Attribute getEmail()
    {
        return email;
    }

    public Attribute getRole()
    {
        return role;
    }

    public Attribute getAge()
    {
        return age;
    }

    public List<Attribute> getAgentAttributes() {
        return agentAttributes;
    }

    public void setUserName(Attribute userName)
    {
        this.userName = userName;
    }

    public void setEmail(Attribute email)
    {
        this.email = email;
    }

    public void setRole(Attribute role)
    {
        this.role = role;
    }

    public void setAge(Attribute age)
    {
        this.age = age;
    }
}
