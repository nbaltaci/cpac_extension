package com.nuray.cpacexecution.cpacmodel;

import java.util.List;

public class AttributeRange {

    private double lowerLimit; // lower an upper limit are for enforcing a limit on the possible values for a numerical attribute
    private double upperLimit;
    private List<String> possibleValues; // this is for enforcing a limit on the possible values for a categorical attribute


    public AttributeRange(double lowerLimit, double upperLimit)
    {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public AttributeRange(List<String> possibleValues)
    {
        this.possibleValues=possibleValues;
    }

    public double getLowerLimit()
    {
        return this.lowerLimit;
    }

    public double getUpperLimit()
    {
        return upperLimit;
    }

    public List<String> getPossibleValues()
    {
        return possibleValues;
    }
}
