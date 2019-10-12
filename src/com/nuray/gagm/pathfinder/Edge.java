package com.nuray.gagm.pathfinder;


import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;

/**
 * Created by TOSHIBA on 1/2/2017.
 */
public class Edge {
    private Vertex sourceVertex, targetVertex;
    private double probability;
    private VirtualAccessRequest var;
    private String edgeType;    // based on GAGM model specification, edge can be either an action or critical edge

    public Edge(Vertex source, Vertex target, double weight, String edgeType, VirtualAccessRequest var)
    {
        this.sourceVertex =source;
        this.targetVertex =target;
        this.probability=weight;
        this.edgeType=edgeType;

        if(edgeType.equalsIgnoreCase("action"))
        {
            if(var==null)
            {
                throw new IllegalArgumentException("Please assign a virtual access request to this edge!");
            }
            else
            {
                this.var=var;
            }
        }

    }

    public Vertex getSourceVertex()
    {
        return this.sourceVertex;
    }

    public Vertex getTargetVertex()
    {
        return this.targetVertex;
    }

    public double getProbability()
    {
        return this.probability;
    }

    public String getEdgeType()
    {
        return edgeType;
    }

    public VirtualAccessRequest getVar()
    {
        return var;
    }
}
