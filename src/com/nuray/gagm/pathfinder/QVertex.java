package com.nuray.gagm.pathfinder;

/**
 * Created by TOSHIBA on 1/4/2017.
 */
public class QVertex {
    private Vertex vertex;
    private double totalRisk;

    public QVertex(Vertex vertex, double totalRisk)
    {
        this.vertex=vertex;
        this.totalRisk=totalRisk;
    }

    public Vertex getVertex()
    {
        return this.vertex;
    }

    public double getTotalRisk()
    {
        return this.totalRisk;
    }
}
