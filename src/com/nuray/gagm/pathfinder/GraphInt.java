package com.nuray.gagm.pathfinder;



import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;

import java.util.List;

/**
 * Created by TOSHIBA on 1/4/2017.
 */
public interface GraphInt {

    public int[][] generateAdjacencyMatrix();
    public double[] generateRiskValues();
    String[][] generateEdgeTypeMatrix();
    VirtualAccessRequest[][] generateVarMatrix(int nAgentAtt,
                                               int nResAtt,
                                               int nActionAtt,
                                               int nActions,
                                               ActionBase actionBase,
                                               String operationalMode) throws Exception; // this is for generating virtual access requests for labels of critical edges
    public List<Vertex> getVertices();
    public void setAdjacencyMatrix(int[][] adjacencyMatrix);

    public int getNumberOfVertices();
    public double formatDecimalPoints(double numberToFormat);

}
