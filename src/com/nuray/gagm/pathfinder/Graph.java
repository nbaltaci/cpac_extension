package com.nuray.gagm.pathfinder;

import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.gagm.experiment.Random;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by TOSHIBA on 1/2/2017.
 */
public class Graph {

    private List<Vertex> vertices;
    private Random finalStateGenerator;
    private String operationalMode;
    int[] finalStates;


    public Graph()
    {

    }

    /**
     *
     * @param adjacencyMatrix
     * @param riskOfVertices
     * @param edgeTypeMatrix
     * @param varMatrix
     * @param finalStates: if this is null, two random vertices will be selected as final states
     * @param operationalMode
     */

    public Graph(int[][] adjacencyMatrix, double[] riskOfVertices,
                 String[][] edgeTypeMatrix, VirtualAccessRequest[][] varMatrix,
                 int[] finalStates,String operationalMode)
    {
        this.operationalMode=operationalMode;

        vertices=new ArrayList<>();

        for(int i=0;i<adjacencyMatrix.length;i++)
        {
            Vertex v=new Vertex(i+1);
            v.setRisk(riskOfVertices[i]);
            vertices.add(v);
        }

        for(int i=0;i<adjacencyMatrix.length;i++)
        {
            for (int j = 0; j < adjacencyMatrix.length; j++)
            {
                if (adjacencyMatrix[i][j] != 0&&adjacencyMatrix[i][j] != Integer.MAX_VALUE)
                {
                    double weight=adjacencyMatrix[i][j]/100.00;
                    weight=formatDecimalPoints(weight);
                    Edge edge=new Edge(vertices.get(i), vertices.get(j), weight,
                            edgeTypeMatrix[i][j],varMatrix[i][j]);
                    vertices.get(i).addEdge(edge);
                }
            }
        }

        finalStateGenerator=new Random(1,vertices.size());

        if(finalStates==null)
        {
            this.finalStates=generateRandomFinalStates(2);
        }
        else
        {
            this.finalStates=finalStates;
        }

    }

    public List<Vertex> getVertices()
    {
        return this.vertices;
    }

    public int getNumberOfVertices()
    {
        return vertices.size();
    }

    public String getOperationalMode()
    {
        return operationalMode;
    }

    private double formatDecimalPoints(double numberToFormat)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String numberFormatted = df.format(numberToFormat);
        double numberToReturn=Double.parseDouble(numberFormatted);
        return numberToReturn;
    }

    public int[] generateRandomFinalStates(int numberOfFinalStates)
    {
        List<Integer> finalStateList=new LinkedList<>();

        for(int i=0;i<numberOfFinalStates;i++)
        {
            int randomVertex=finalStateGenerator.generateRandomIndex();
            if(!finalStateList.contains(randomVertex))
            {
                finalStateList.add(randomVertex);
            }
        }
        finalStates=finalStateList.stream().mapToInt(Integer::intValue).toArray();

        return finalStates;

    }

    public int[] getFinalStates()
    {
        return finalStates;
    }
}
