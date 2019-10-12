package com.nuray.cpacexecution.cpacmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class Attribute {

    private String attributeType;
    private String dataType;

    private String attributeName;
    private AttributeRanges attributeRange; // this is only for numeric attributes
    private double attributeValueNumeric;
    private String attributeValueCategorical;
    private Date attributeValDate;
    private LocalTime attributeValTime;
    private boolean valueIsSet;


    /**
     *
     * @param attributeName
     * @param attributeRange: this is only for numeric attributes such as age, it should be null otherwise
     * @param attributeType: Type should be one of the values specified in "CPACSpecifications.java" file, i.e. numeric, categorical,
     *            date, or time.
     *            Date: a date attribute should be in the following format: dd/mm/yyyy
     *            Time: a time attribute should be in the following format: HH:MM:SS
     */
    public Attribute(String attributeName, AttributeRanges attributeRange, String attributeType)
    {
        if(!CPACSpecifications.attributeTypes.contains(attributeType))
        {
            throw new IllegalArgumentException("Type should be one of the values specified in" +
                    " \"CPACSpecifications.java\" file, i.e. numeric, categorical, date, or time: "+ attributeType);
        }

        if(attributeType.equalsIgnoreCase("categorical"))
        {
            dataType="http://www.w3.org/2001/XMLSchema#string";
        }
        else if(attributeType.equalsIgnoreCase("numerical"))
        {
            if(attributeRange==null)
            {
                throw new IllegalArgumentException("An attribute range should be defined for numerical attributes");
            }

            dataType="http://www.w3.org/2001/XMLSchema#double";
        }
        else if(attributeType.equalsIgnoreCase("date"))
        {
            dataType="http://www.w3.org/2001/XMLSchema#date";
        }
        else if(attributeType.equalsIgnoreCase("time"))
        {
            dataType="http://www.w3.org/2001/XMLSchema#time";
        }

        this.attributeName=attributeName;
        this.attributeRange=attributeRange;
        this.attributeType =attributeType;

    }

    @Override
    public boolean equals(Object obj) {

        Attribute another= (Attribute) obj;

        return another.attributeName==this.attributeName;
    }

    public String getAttributeName()
    {
        return this.attributeName;
    }

    public AttributeRanges getAttributeRange()
    {
        return attributeRange;
    }

    public String getDataType()
    {
        return dataType;
    }

    public double getAttributeValueNumeric() throws Exception {
        if(!isValueSet())
        {
            throw new Exception("set attribute value first!");
        }
        return attributeValueNumeric;
    }

    public String getAttributeValueCategorical() throws Exception
    {
        if ((!isValueSet()))
        {
            throw new Exception("set attribute value first!");
        }
        return attributeValueCategorical;
    }

    public Date getAttributeValDate()
    {
        return attributeValDate;
    }

    public LocalTime getAttributeValTime()
    {
        return attributeValTime;
    }

    public void setAttributeValueNumeric(double attributeValue) throws Exception {
        if(attributeValue<attributeRange.getLowerLimit()
        ||attributeValue>attributeRange.getUpperLimit())
        {
            String errorMessage="Attribute value is out of the range.";
            throw new Exception(errorMessage);
        }
        else
        {
            this.attributeValueNumeric = attributeValue;
            valueIsSet=true;
        }
    }

    public void setAttributeValueCategorical(String attributeValue)
    {
        this.attributeValueCategorical=attributeValue;
        valueIsSet=true;
    }

    public void setAttributeValDate(String attributeValDate) throws ParseException {

        this.attributeValDate=new SimpleDateFormat("dd/MM/yyyy").parse(attributeValDate);
        valueIsSet=true;
    }

    /**
     *
     * @param attributeValTime should be in the following format: HH:MM:SS
     */
    public void setAttributeValTime(String attributeValTime) {

        this.attributeValTime=LocalTime.parse(attributeValTime);
    }

    public boolean isValueSet() {
        return valueIsSet;
    }

    public String getAttributeType()
    {
        return attributeType;
    }
}