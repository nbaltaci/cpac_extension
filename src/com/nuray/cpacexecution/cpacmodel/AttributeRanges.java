package com.nuray.cpacexecution.cpacmodel;

public class AttributeRanges {

    private double lowerLimit;
    private double upperLimit;


    public AttributeRanges(double lowerLimit, double upperLimit)
    {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public double getLowerLimit()
    {
        return this.lowerLimit;
    }

    public double getUpperLimit()
    {
        return upperLimit;
    }


}
