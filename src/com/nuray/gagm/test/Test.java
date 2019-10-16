package com.nuray.gagm.test;


import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.gagm.experiment.CompleteGraph;
import com.nuray.gagm.experiment.Sparse;
import com.nuray.gagm.pathfinder.Edge;
import com.nuray.gagm.pathfinder.Graph;
import com.nuray.gagm.pathfinder.VARGeneration;
import com.nuray.gagm.pathfinder.Vertex;

import java.util.*;

/**
 * Created by TOSHIBA on 1/2/2017.
 */
public class Test {

    private static int[][] adjacencyMatrix;
    private static com.nuray.gagm.pathfinder.VARGeneration VARGeneration;
    private static Sparse sparse;
    private static CompleteGraph complete;

    public static void main(String[] args) throws Exception {
//        printResultsforSparseGraph(1,1,3,new int[]{3});
//        printResultsforSparseGraph(2,1,6, new int[]{6});
//        printResultsforSparseGraph(2,5,12, new int[]{12});
//        printResultsforCompleteGraph(3,1,3, new int[]{4}); // ==> this is a complete one
//
//        System.out.println("NOW RUNNING FOR ACTUAL SET OF FINAL STATES");
//        int[] finalStates=new int[]{6,12};
//        printResultsforSparseGraph(2,5,12, finalStates);

        varSetTest("sparse",1,1,3,new int[]{3,4});


    }

    private static void printResultsforSparseGraph(int testCaseNumber, int sourceVertex,int destinationVertex,int[] finalStates) throws Exception
    {
        printResultsforTestCases("sparse",testCaseNumber,sourceVertex,destinationVertex,finalStates);
    }

    private static void printResultsforCompleteGraph(int testCaseNumber, int sourceVertex,int destinationVertex,int[] finalStates) throws Exception
    {
        printResultsforTestCases("complete",testCaseNumber,sourceVertex,destinationVertex,finalStates);
    }


    private static void printResultsforTestCases(String graphType,int testCaseNumber, int sourceVertex,int destinationVertex,
                                                 int[] finalStates) throws Exception
    {
        Graph graph=initializeGraph(graphType,testCaseNumber,finalStates);

        System.out.println("TEST CASE "+testCaseNumber+":");

        System.out.println("Shortest Paths:\n From vertex "+sourceVertex+" to vertex "+destinationVertex);

        long beginTime=new Date().getTime();
        VARGeneration =new VARGeneration(graph,sourceVertex,destinationVertex,finalStates);
        VARGeneration.solve();
        long endTime=new Date().getTime();

        System.out.println("Running time for "+adjacencyMatrix.length+" vertices: "+(endTime-beginTime));
    }


    private static void varSetTest(String graphType,int testCaseNumber, int sourceVertex,int destinationVertex,
                                   int[] finalStates) throws Exception
    {
        Graph graph = initializeGraph(graphType, testCaseNumber, finalStates);

        System.out.println("TEST CASE "+testCaseNumber+":");

        VARGeneration =new VARGeneration(graph,sourceVertex,destinationVertex,finalStates);

        Set<Graph> subGraphSet=new HashSet<Graph>();
        subGraphSet.add(graph);

        Map<String, Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>>> varSet =
                VARGeneration.generateVAR(subGraphSet);
        Iterator<String> varSetIterator = varSet.keySet().iterator();

        while (varSetIterator.hasNext()) {
            String m = varSetIterator.next();  // m = operational mode
            Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>> varSet_m = varSet.get(m);

            System.out.println("Now printing varSet for " + m + " mode:");
            System.out.println("\t Shortest Paths (vars on the shortest path):");

            while (!varSet_m.isEmpty()) {
                Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>> varListMap = varSet_m.poll();

                Iterator<Vertex> varListIterator = varListMap.keySet().iterator();

                while (varListIterator.hasNext()) {
                    Vertex sourceState = varListIterator.next();
                    Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_Map = varListMap.get(sourceState);

                    while (!varList_v_Map.isEmpty()) {
                        Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>> varList_v = varList_v_Map.poll();
                        Iterator<Vertex> varList_v_Iterator = varList_v.keySet().iterator();

                        while (varList_v_Iterator.hasNext()) {
                            Vertex finalState = varList_v_Iterator.next();
                            Queue<Map<Edge, VirtualAccessRequest>> varList_f = varList_v.get(finalState);

                            System.out.println("\t \t From vertex " + sourceState.getVertexID() + " to vertex " + finalState.getVertexID() + "\n");

                            while (!varList_f.isEmpty()) {
                                Map<Edge, VirtualAccessRequest> edgeVarMap = varList_f.poll();
                                Iterator<Edge> edgeIterator = edgeVarMap.keySet().iterator();

                                while (edgeIterator.hasNext()) {
                                    Edge edge = edgeIterator.next();
                                    VirtualAccessRequest var = edgeVarMap.get(edge);
                                    int requestId = var.getRequestId();
                                    System.out.println("\t \t \t v" + edge.getSourceVertex().getVertexID() +
                                            " --> v" + edge.getTargetVertex().getVertexID() +
                                            ": var" + requestId);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public static Graph initializeGraph(String graphType,int testCaseNumber, int[] finalStates) throws Exception {
        adjacencyMatrix = ReadAdjacency.read("src/adjacent matrix for test case-"+testCaseNumber+".txt");
        String[][] edgeTypeMatrix=null;
        VirtualAccessRequest[][] varMatrix=null;

        if(graphType.equalsIgnoreCase("sparse"))
        {
            sparse=new Sparse(adjacencyMatrix.length,0,100,1,100,0.5);
            sparse.setAdjacencyMatrix(adjacencyMatrix);
            edgeTypeMatrix=sparse.generateEdgeTypeMatrix();
            varMatrix=sparse.generateVarMatrix(5,1,1,5,
                    new ActionBase(),"emergency");
        }
        else if (graphType.equalsIgnoreCase("complete"))
        {
            complete=new CompleteGraph(adjacencyMatrix.length,0,100,1,100,0.5);
            complete.setAdjacencyMatrix(adjacencyMatrix);
            edgeTypeMatrix=complete.generateEdgeTypeMatrix();
            varMatrix=complete.generateVarMatrix(5,1,1,5,
                    new ActionBase(),"emergency");
        }
        else
        {
            throw new Exception("invalid graph type, should be either complete or sparse!");
        }

        double[] riskValues=new double[adjacencyMatrix.length];
        Arrays.fill(riskValues,100);

        Graph graph=new Graph(adjacencyMatrix,riskValues,edgeTypeMatrix,varMatrix,finalStates,"emergency");

        return graph;
    }

}
