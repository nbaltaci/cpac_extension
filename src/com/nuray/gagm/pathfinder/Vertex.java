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


    /**
     * To hold a previous node in the shortest path. (This would hold a single
     * node only in one of the possible shortest paths.)
     */
    private Vertex previous;
    /**
     * A distance measure for this vertex from source vertex.
     */
    public double sourceDistance = Double.POSITIVE_INFINITY;
    /**
     * A List of all previous nodes in all the possible shortest paths.
     */
    private List<Vertex> prev;

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


    // The following methods are added for supporting multiple shortest path between a given source node and destination node
    public List<Vertex> getPrev() {
        return prev;
    }

    public void setPrev(List<Vertex> prev)
    {
        this.prev = prev;
    }

    public Vertex getPrevious() {
        return previous;
    }

    public void setPrevious(Vertex previous) {
        this.previous = previous;
    }

    public double getSourceDistance()
    {
        return sourceDistance;
    }

    public void setSourceDistance(double sourceDistance)
    {
        this.sourceDistance = sourceDistance;
    }
}
