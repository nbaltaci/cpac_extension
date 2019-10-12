package com.nuray.gagm.pathfinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TOSHIBA on 1/2/2017.
 */
public class Vertex{
    private int vertexID;
    private double risk;
    private List<Edge> edgeList;

    public Vertex(int vertexNo)
    {
        this.vertexID=vertexNo;
        this.edgeList=new ArrayList<>();
    }

    public void setRisk(double risk)
    {
        this.risk=risk;
    }

    public double getRisk()
    {
        return this.risk;
    }

    @Override
    public boolean equals(Object obj) {
        Vertex other = (Vertex)obj;

        return other.getVertexID() == this.getVertexID();
    }

    public void addEdge(Edge edge)
    {
        this.edgeList.add(edge);
    }

    public int getVertexID()
    {
        return this.vertexID;
    }

    public List<Edge> getEdges()
    {
        return this.edgeList;
    }


}
