package com.nuray.gagm.experiment;

import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.gagm.pathfinder.GraphInt;
import com.nuray.gagm.pathfinder.Vertex;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by TOSHIBA on 1/4/2017.
 */
public class Sparse implements GraphInt {

    private List<Vertex> vertices;
    private int numberOfVertices;
    private Random probabilities, riskValues, edgeTypes;
    private int[][] adjacencyMatrix;
    private String[][] edgeTypeMatrix;
    private double fraction;

    public Sparse(int numberOfVertices, int weightLower, int weightUpper, int riskLower, int riskUpper,
                  double fraction)
    {
        this.numberOfVertices = numberOfVertices;
        probabilities= new Random(weightLower, weightUpper);
        riskValues=new Random(riskLower,riskUpper);
        edgeTypes=new Random();
        this.fraction=fraction;
    }


    @Override
    public int[][] generateAdjacencyMatrix()
    {
        int[][] adjacencyMatrix=new int[numberOfVertices][numberOfVertices];

        for (int i = 0; i < adjacencyMatrix.length; i++)
        {
            for (int j = 0; j < adjacencyMatrix.length; j++)
            {
                if(i==j)
                {
                    adjacencyMatrix[i][j] = 0;
                }
                else
                {
                    adjacencyMatrix[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        for(int i = 1; i < numberOfVertices; i++)
        {
            int randomVertex = new Random(0, i-1).generateRandomIndex();

            adjacencyMatrix[randomVertex][i] = probabilities.generateRandomWeight();
            adjacencyMatrix[i][randomVertex] = adjacencyMatrix[randomVertex][i];
        }
        this.adjacencyMatrix=adjacencyMatrix;

        return adjacencyMatrix;
    }

    @Override
    public double[] generateRiskValues() {
        double[] riskValueArray=new double[numberOfVertices];
        for(int i=0; i < numberOfVertices;i++)
        {
            //generate risk values
            riskValueArray[i]=riskValues.generateRandomWeight();
        }
        return riskValueArray;
    }

    /**
     * In this function, I assume that the flow of graph is from the 0th vertex to the nth vertex.
     * In this case, I assume that the edges directed from vertices with smaller index to vertices with
     * larger index are action edges, such as e(0,1), e(0,2), e(3,5),... (these edges correspond to the
     * upper right half of the adjacency matrix). Therefore, all such edges have to be labelled as action edges.
     * Yet, the edges directed in the reverse direction are selectively labelled as critical edges (i.e. if
     * "generateRandomEdgeType()" function returns "critical" value, the edge is labelled critical, otherwise "action".
     */
    @Override
    public String[][] generateEdgeTypeMatrix() {
        edgeTypeMatrix=new String[numberOfVertices][numberOfVertices];

        for(int i=0; i < numberOfVertices;i++)
        {
            for(int j=0; j<=i;j++)
            {
                if(i==j || adjacencyMatrix[i][j] == Integer.MAX_VALUE)
                {
                    edgeTypeMatrix[j][i]="";
                    edgeTypeMatrix[i][j]="";
                }
                else
                {
                    edgeTypeMatrix[j][i] = "action";
                    edgeTypeMatrix[i][j]=edgeTypes.generateRandomEdgeType(fraction);
                }
            }
        }

        return edgeTypeMatrix;
    }

    /**
     * Note: Before calling this method, first the "generateEdgeTypeMatrix()" method should be called and an "edgeTypeMatrix"
     * should be constructed.
     * @return
     */
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
    public List<Vertex> getVertices()
    {
        return this.vertices;
    }

    @Override
    public void setAdjacencyMatrix(int[][] adjacencyMatrix)
    {
        this.adjacencyMatrix=adjacencyMatrix;
    }

    @Override
    public int getNumberOfVertices()
    {
        return vertices.size();
    }

    @Override
    public double formatDecimalPoints(double numberToFormat)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String numberFormatted = df.format(numberToFormat);
        double numberToReturn=Double.parseDouble(numberFormatted);
        return numberToReturn;
    }


}
