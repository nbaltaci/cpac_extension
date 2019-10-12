package com.nuray.gagm.pathfinder;


import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;

import java.util.*;

public class VARGeneration {
    private double[] length;
    private Vertex[] previous;

    private Graph graph;
    private int graphSize;
    private Vertex startVertex;
    private Vertex targetVertex;
    private PriorityQueue<QVertex> Q;
    private Set<Vertex> finalStateSet;

    private Queue<Map<Edge, VirtualAccessRequest>> varList_f;  // this is for a single source and a single destination state in a graph
    private Queue<Map<Vertex,Queue<Map<Edge, VirtualAccessRequest>>>> varList_v;  // this is for a single source and a set of destination (final) states in a graph
    private Queue<Map<Vertex,Queue<Map<Vertex,Queue<Map<Edge, VirtualAccessRequest>>>>>> varSet_m;  // this is for a set of source and a set of destination states in a graph
    private Map<String,Queue<Map<Vertex,Queue<Map<Vertex,Queue<Map<Edge, VirtualAccessRequest>>>>>>> varSet;

    //    public VARGeneration(int[][] adjacencyMatrix, double[] riskValues,
    //                             String[][] edgeTypeMatrix, VirtualAccessRequest[][] varMatrix,
    //                             int startVertex, int targetVertex, int[] finalStates)

    public VARGeneration(Graph graph, int startVertex, int targetVertex, int[] finalStates)
    {
//        graph=new Graph(adjacencyMatrix,riskValues,edgeTypeMatrix,varMatrix);
        this.graph=graph;
        this.graphSize = graph.getNumberOfVertices();
        this.startVertex=graph.getVertices().stream().filter(item->item.getVertexID()==startVertex).findAny().orElse(null);
        this.targetVertex=graph.getVertices().stream().filter(item->item.getVertexID()==targetVertex).findAny().orElse(null);

        finalStateSet=new HashSet<>();
        for (int i=0;i<finalStates.length;i++)
        {
            int stateNo=finalStates[i];
            Vertex vertexToAdd=graph.getVertices().stream().filter(item->item.getVertexID()==stateNo).findAny().orElse(null);
            this.finalStateSet.add(vertexToAdd);
        }

        length=new double[graphSize];
        previous=new Vertex[graphSize];

    }

    public Map<String, Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>>> generateVAR(Set<Graph> graphSet)
    {
        previous=new Vertex[graphSize];
        varList_v=new LinkedList<>();
        varSet_m=new LinkedList<>();
        varSet=new HashMap<>();

        for (Graph graph:graphSet)
        {
            varList_f=new LinkedList<>();

            List<Vertex> vertices = graph.getVertices();
            //get only non-final states
//            vertices.removeAll(finalStateSet);

            for (Vertex vertex:vertices)
            {
//                previous=findOptimum();
                varList_v = findVarList(vertex, finalStateSet, previous);
                Map<Vertex,Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>> map=new HashMap<>();
                map.put(vertex,varList_v);
                varSet_m.add(map);
                varList_v=new LinkedList<>();
            }

            String operationalMode=graph.getOperationalMode();
            varSet.put(operationalMode,varSet_m);
            varSet_m=new LinkedList<>();
        }

        return varSet;
    }

    public Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> findVarList(Vertex source, Set<Vertex> finalStateSet, Vertex[] previous)
    {
        for (Vertex finalState:finalStateSet)
        {
            startVertex=source;
            targetVertex=finalState;
            previous=findOptimum();
            findVarListSingleDest(source,finalState,previous);

            Map<Vertex,Queue<Map<Edge,VirtualAccessRequest>>> map=new HashMap<>();
            map.put(finalState,varList_f);
            varList_v.add(map);
            varList_f=new LinkedList<>();
        }

        return varList_v;

    }

    private void findVarListSingleDest(Vertex sourceVertex,Vertex destinationVertex, Vertex[] previous)
    {
        if(sourceVertex==destinationVertex)
        {
            return;
        }
        Vertex previousVertex=previous[destinationVertex.getVertexID()-1];
        if(previousVertex!=startVertex)
        {
            findVarListSingleDest(sourceVertex, previousVertex,previous);
        }

        List<Edge> edgeList=previousVertex.getEdges();

        for (Edge e:edgeList)
        {
            if( e.getTargetVertex().getVertexID()== destinationVertex.getVertexID())
            {
                VirtualAccessRequest var = e.getVar();
                if(var!=null)
                {
                    Map<Edge,VirtualAccessRequest> map=new HashMap<>();
                    map.put(e,var);
                    varList_f.add(map);
                }
            }
        }
    }




    public void solve () {
        findOptimum();
        printVertexesOnShortestPath(startVertex,targetVertex);
        printShortestPathLength();
    }

    public void solveExperiment ()
    {
        findOptimum();
    }

    private Vertex[] findOptimum()
    {
        //initialize pq
        Q=new PriorityQueue<>(graphSize, new Comparator<QVertex>() {
            @Override
            public int compare(QVertex o1, QVertex o2) {
                return Double.compare(o1.getTotalRisk(), o2.getTotalRisk());
            }
        });

        //  9/12/2018 nuray: initialize set of visited vertices
        Set<Vertex> visitedVertices=new HashSet<>();

        //initialize shortest distance of all vertices to infinity, and distance of starting vertex to zero

        Arrays.fill(length,Integer.MAX_VALUE);
        Arrays.fill(previous,null);

        Q.add(new QVertex(startVertex,0));

        while(!Q.isEmpty())
        {
            QVertex minQVertex= Q.poll();
            Vertex minVertex=minQVertex.getVertex();

            // 9/12/2018 NURAY: check if a final state is reached (Fg in our paper)

            if(visitedVertices.containsAll(finalStateSet))
            {
                break;
            }

            List<Edge> edgeList=minVertex.getEdges();

            for (Edge e:edgeList)
            {
                Vertex destination=e.getTargetVertex();

                double newRisk=e.getProbability()*destination.getRisk()+minQVertex.getTotalRisk();

                if(newRisk<length[destination.getVertexID()-1])
                {
                    length[destination.getVertexID()-1]=newRisk;
                    previous[destination.getVertexID()-1]=minVertex;

                    Q.add(new QVertex(destination,newRisk));

                    // 9/12/2018  Nuray: I added this to represent visited set of nodes
                    if(!visitedVertices.contains(destination))
                    {
                        visitedVertices.add(destination);
                    }
                }
            }
        }

        return previous;
    }

    private void printVertexesOnShortestPath( Vertex sourceVertex,Vertex destinationVertex)
    {
        System.out.print("v"+sourceVertex.getVertexID()+", ");
        printIntermediateVertices(sourceVertex,destinationVertex);
    }

    private void printIntermediateVertices(Vertex sourceVertex,Vertex destinationVertex)
    {
        Vertex previousVertex=previous[destinationVertex.getVertexID()-1];
        if(previousVertex==null)
        {
            return;
        }

        if(previousVertex!=startVertex)
        {
            printIntermediateVertices(sourceVertex, previousVertex);
        }

        System.out.print("v"+(destinationVertex.getVertexID()) + ", ");
    }

    private void printShortestPathLength()
    {
        System.out.println("\nWeight:"+length[targetVertex.getVertexID()-1]);
    }

}
