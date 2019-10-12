package com.nuray.gagm.experiment;

import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.gagm.pathfinder.GraphInt;
import com.nuray.gagm.pathfinder.Vertex;

import java.util.List;

/**
 * Created by TOSHIBA on 1/4/2017.
 */
public class CompleteGraph implements GraphInt {
    private int numberOfVertices;
    private Random probabilities, riskValues, edgeTypes;
    private int[][] adjacencyMatrix;
    private String[][] edgeTypeMatrix;
    private double fraction;

    /**
     *
     * @param numberOfVertices
     * @param weightLower
     * @param weightUpper
     * @param riskLower
     * @param riskUpper
     * @param fraction: this is the fraction of critical edges in a graph
     */

    public CompleteGraph(int numberOfVertices, int weightLower, int weightUpper, int riskLower, int riskUpper,
                         double fraction)
    {
        this.numberOfVertices = numberOfVertices;
        probabilities= new Random(weightLower, weightUpper);
        riskValues=new Random(riskLower,riskUpper);
        edgeTypes=new Random();
        this.fraction=fraction;
    }


    public int[][] generateAdjacencyMatrix()
    {
        int[][] adjacencyMatrix=new int[numberOfVertices][numberOfVertices];

        for(int i=0; i < numberOfVertices;i++)
        {
            for(int j=0; j<=i;j++)
            {
                if(i==j)
                {
                    adjacencyMatrix[j][i]=0;
                }
                else
                {
                    adjacencyMatrix[j][i] = probabilities.generateRandomWeight();
                }

                adjacencyMatrix[i][j]=adjacencyMatrix[j][i];
            }
        }

        return adjacencyMatrix;
    }

    public double[] generateRiskValues()
    {
        double[] riskValueArray=new double[numberOfVertices];
        for(int i=0; i < numberOfVertices;i++)
        {
            //generate risk values
            riskValueArray[i]=riskValues.generateRandomWeight();
        }
        return riskValueArray;
    }

    @Override
    /**
     * In this function, I assume that the flow of graph is from the 0th vertex to the nth vertex.
     * In this case, I assume that the edges directed from vertices with smaller index to vertices with
     * larger index are action edges, such as e(0,1), e(0,2), e(3,5),... (these edges correspond to the
     * upper right half of the adjacency matrix). Therefore, all such edges have to be labelled as action edges.
     * Yet, the edges directed in the reverse direction are selectively labelled as critical edges (i.e. if
     * "generateRandomEdgeType()" function returns "critical" value, the edge is labelled critical, otherwise "action".
     */
    public String[][] generateEdgeTypeMatrix()
    {
        edgeTypeMatrix=new String[numberOfVertices][numberOfVertices];

        for(int i=0; i < numberOfVertices;i++)
        {
            for(int j=0; j<=i;j++)
            {
                if(i==j)
                {
                    edgeTypeMatrix[j][i]="";
                }
                else
                {
                    edgeTypeMatrix[j][i] = "action";
                    edgeTypeMatrix[i][j]=edgeTypes.generateRandomEdgeType(fraction);
                }

//                edgeTypeMatrix[i][j]=edgeTypes.generateRandomEdgeType(fraction);
            }
        }

        return edgeTypeMatrix;
    }

    @Override
    public VirtualAccessRequest[][] generateVarMatrix(int nAgentAtt,
                                                      int nResAtt,
                                                      int nActionAtt,
                                                      int nActions,
                                                      ActionBase actionBase,
                                                      String operationalMode) throws Exception {

        VirtualAccessRequest[][] varMatrix=new VirtualAccessRequest[numberOfVertices][numberOfVertices];
        Random random=new Random();

        for(int i=0; i < numberOfVertices;i++)
        {
            for(int j=0; j<=i;j++)
            {
//                if(i==j)
//                {
//                    varMatrix[j][i]=null;
//                }
//                else
//                {
//                    if(edgeTypeMatrix[i][j].equalsIgnoreCase("action"))
//                    {
//                        varMatrix[j][i] = random.generateRandomVar(nAgentAtt,nResAtt,nActionAtt,nActions,
//                                actionBase,operationalMode);
//                    }
//                }
//
//                varMatrix[i][j]=varMatrix[j][i];

                if(edgeTypeMatrix[i][j].equalsIgnoreCase("action"))
                {
                    varMatrix[i][j] = random.generateRandomVar(nAgentAtt,nResAtt,nActionAtt,nActions,
                            actionBase,operationalMode);
                }
                if(edgeTypeMatrix[j][i].equalsIgnoreCase("action"))
                {
                    varMatrix[j][i]= random.generateRandomVar(nAgentAtt,nResAtt,nActionAtt,nActions,
                            actionBase,operationalMode);
                }
            }
        }


        return varMatrix;

    }


    @Override
    public List<Vertex> getVertices() {
        return null;
    }

    @Override
    public void setAdjacencyMatrix(int[][] adjacencyMatrix)
    {
        this.adjacencyMatrix=adjacencyMatrix;
    }

    @Override
    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    @Override
    public double formatDecimalPoints(double numberToFormat) {
        return 0;
    }
}
