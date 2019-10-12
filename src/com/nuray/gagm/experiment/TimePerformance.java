package com.nuray.gagm.experiment;

import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.gagm.pathfinder.Graph;
import com.nuray.gagm.pathfinder.VARGeneration;

import java.util.Date;

/**
 * Created by TOSHIBA on 1/4/2017.
 */
public class TimePerformance {

    private static int[][] adjacencyMatrix;
    private static double[] riskValues;
    private static String[][] edgeTypeMatrix;
    private static VirtualAccessRequest[][] varMatrix;
    private static com.nuray.gagm.pathfinder.VARGeneration VARGeneration;
    private static CompleteGraph completeGraph;
    private static Sparse sparse;
    private static Random random;

    public static void main(String[] args) throws Exception {
        for(int i=10;i<=100;i=i+10)
        {
            solveSparse((100*i),0,100,1,100,
                    0.5,1,10,null);
        }

//        System.out.println("Results for complete graph...");
//        for(int i=10;i<=100;i=i+10)
//        {
//            solveComplete((10*i),0,100,1,100,1,100);
//        }

        System.out.println("Results for sparse graph...");

        for(int i=10;i<=100;i=i+10)
        {
            solveSparse((100*i),0,100,1,100,
                    0.5,1,10,null);
        }

    }

    /**
     *
     * @param numberOfVertices
     * @param weightLower
     * @param weightUpper
     * @param riskLower
     * @param riskUpper
     * @param fraction: this is the ratio of critical edges to the total edges (critical edge vs. action edge)
     * @param sourceVertex
     * @param destinationVertex
     * @param finalStates: if this is null, two random vertices will be selected as final states
     */
    private static void solveComplete(int numberOfVertices,int weightLower,int weightUpper,int riskLower,
                                        int riskUpper, double fraction,int sourceVertex,int destinationVertex,
                                        int[] finalStates) throws Exception {
        completeGraph=new CompleteGraph(numberOfVertices,weightLower,weightUpper,riskLower,riskUpper,fraction);

        solve("complete",numberOfVertices,sourceVertex,destinationVertex,finalStates);

    }

    /**
     *
     * @param numberOfVertices
     * @param weightLower
     * @param weightUpper
     * @param riskLower
     * @param riskUpper
     * @param fraction: this is the ratio of critical edges to the total edges (critical edge vs. action edge)
     * @param sourceVertex
     * @param destinationVertex
     * @param finalStates: if this is null, two random vertices will be selected as final states
     */
    private static void solveSparse(int numberOfVertices,int weightLower,int weightUpper,int riskLower,
                                      int riskUpper,double fraction, int sourceVertex,int destinationVertex,
                                        int[] finalStates) throws Exception {
        sparse=new Sparse(numberOfVertices,weightLower,weightUpper,riskLower,riskUpper,fraction);

        solve("sparse",numberOfVertices,sourceVertex,destinationVertex,finalStates);
    }


    private static void solve(String graphType,int numberOfVertices,int sourceVertex,int destinationVertex,int[] finalStates) throws Exception {

        if(graphType.equalsIgnoreCase("complete"))
        {
            adjacencyMatrix=completeGraph.generateAdjacencyMatrix();
            riskValues=completeGraph.generateRiskValues();
            edgeTypeMatrix=completeGraph.generateEdgeTypeMatrix();
            varMatrix=completeGraph.generateVarMatrix(5,1,1,5,
                    new ActionBase(),"emergency");
        }
        else if (graphType.equalsIgnoreCase("sparse"))
        {
            adjacencyMatrix=sparse.generateAdjacencyMatrix();
            riskValues=sparse.generateRiskValues();
            edgeTypeMatrix=sparse.generateEdgeTypeMatrix();
            varMatrix=sparse.generateVarMatrix(5,1,1,5,
                    new ActionBase(),"emergency");
        }

        random=new Random(1,adjacencyMatrix.length);

        Graph graph=new Graph(adjacencyMatrix,riskValues,edgeTypeMatrix,varMatrix,null,"emergency");


        int totalTime=0;
        double avgTime;
        for(int i=0;i<100;i++)
        {
            int source=random.generateRandomIndex();
            int destination=random.generateRandomIndex();
            long beginTime=new Date().getTime();
            VARGeneration =new VARGeneration(graph,source,destination,finalStates);
            VARGeneration.solveExperiment();
            long endTime=new Date().getTime();
            totalTime+=(endTime-beginTime);
        }

        avgTime=totalTime/100;

        System.out.println("Running time for "+numberOfVertices+" vertices: "+(avgTime));
    }
}
