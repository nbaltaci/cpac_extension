package com.nuray.cpacexecution.cpacmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Environment {
    private List<Attribute> environmentAttributes;

    private Attribute date;
    private Attribute time;
    private Attribute domain;

    private String environmentID;
    private int environmentId;



    /**
     *
     * @param environmentID: This basically represents the domain.
     */
    public Environment(String environmentID)
    {
        this.environmentID=environmentID;
        environmentId++;


        domain=new Attribute("domain",null,"categorical");
        domain.setAttributeValueCategorical(environmentID);
        date=new Attribute("date",null,"date");
        time=new Attribute("time",null,"time");

        environmentAttributes=new ArrayList<>(Arrays.asList(new Attribute[]{date,time,domain}));
    }

    @Override
    public boolean equals(Object obj)
    {
        Environment other = (Environment) obj;
        return other.environmentId==this.environmentId;
    }

    public Attribute getDate()
    {
        return date;
    }

    public Attribute getTime()
    {
        return time;
    }

    public Attribute getDomain()
    {
        return domain;
    }

    public int getEnvironmentId()
    {
        return environmentId;
    }

    public String getEnvironmentID()
    {
        return environmentID;
    }

    public List<Attribute> getEnvironmentAttributes()
    {
        return environmentAttributes;
    }

    public void setDate(Attribute date) {
        this.date = date;
    }

    public void setTime(Attribute time) {
        this.time = time;
    }

    public void setDomain(Attribute domain) {
        this.domain = domain;
    }

    public void setEnvironmentID(String environmentID) {
        this.environmentID = environmentID;
    }
}
