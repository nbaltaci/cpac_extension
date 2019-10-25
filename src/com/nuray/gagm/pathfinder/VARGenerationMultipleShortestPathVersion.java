package com.nuray.gagm.pathfinder;

import com.nuray.cpacexecution.cpacmodel.Action;
import com.nuray.cpacexecution.cpacmodel.Attribute;
import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;

import java.util.*;

public class VARGenerationMultipleShortestPathVersion {

    private Set<List<Vertex>> allShortestPaths;

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


    public VARGenerationMultipleShortestPathVersion(Graph graph, int startVertex, int targetVertex, int[] finalStates)
    {
        this.graph=graph;
        this.graphSize = graph.getNumberOfVertices();
        this.startVertex = graph.getVertices().stream().filter(item -> item.getVertexID() == startVertex).findAny().orElse(null);
        this.targetVertex = graph.getVertices().stream().filter(item -> item.getVertexID() == targetVertex).findAny().orElse(null);

        finalStateSet = new HashSet<>();
        for (int i = 0; i < finalStates.length; i++)
        {
            int stateNo = finalStates[i];
            Vertex vertexToAdd = graph.getVertices().stream().filter(item -> item.getVertexID() == stateNo).findAny().orElse(null);
            this.finalStateSet.add(vertexToAdd);
        }
    }


    public Map<String, Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>>> generateVAR(Set<Graph> graphSet)
    {
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
                varList_v = findVarList(vertex, finalStateSet);
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


    public Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> findVarList(Vertex source, Set<Vertex> finalStateSet)
    {
        for (Vertex finalState:finalStateSet)
        {
//            startVertex=source;
            findOptimumReduced(source);
            findVarListSingleDest(source,finalState);

            Map<Vertex,Queue<Map<Edge,VirtualAccessRequest>>> map=new HashMap<>();
            if(varList_f!=null)
            {
                map.put(finalState,varList_f);
                varList_v.add(map);
            }

            varList_f=new LinkedList<>();
        }

        return varList_v;

    }


    private Queue<Map<Edge, VirtualAccessRequest>> findVarListSingleDest(Vertex sourceVertex, Vertex destinationVertex)
    {
        if(sourceVertex==destinationVertex)
        {
            return null;
        }
        Vertex previousVertex=destinationVertex.getPrevious();

        if(previousVertex!=startVertex)
        {
            findVarListSingleDest(sourceVertex, previousVertex);
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

        return varList_f;
    }

    /**
     * This method prints the single shortest path found by the findOptimumReduced() method
     */
    public void solveReduced()
    {
//        findOptimumReduced();

        List<Vertex> path = getShortestPathTo(targetVertex);
        for (Vertex v:path)
        {
            System.out.print("v"+v.getVertexID()+" ");
        }
        printShortestPathLength(path);

    }

    public void solve() {
        findOptimum();

        Set<List<Vertex>> allShortestPaths =getAllShortestPathsTo(targetVertex);
        int i = 1;
        for (List<Vertex> path:allShortestPaths)
        {
            System.out.print("Path "+i+": ");

            for (Vertex v:path)
            {
                System.out.print("v"+v.getVertexID()+" ");
            }
            printShortestPathLength(path);
            i++;
        }

//        printVertexesOnShortestPath(startVertex, targetVertex);
//        printShortestPathLength();
    }

    public void solveExperiment() {
        findOptimum();
    }

    private void findOptimum()
    {
        //initialize pq
        Q = new PriorityQueue<>(graphSize, new Comparator<QVertex>() {
            @Override
            public int compare(QVertex o1, QVertex o2) {
                return Double.compare(o1.getTotalRisk(), o2.getTotalRisk());
            }
        });

        //  9/12/2018 nuray: initialize set of visited vertices
//        Set<Vertex> visitedVertices = new HashSet<>();

        //initialize shortest distance of all vertices to infinity, and distance of starting vertex to zero
        for (Vertex v:graph.getVertices())
        {
            v.setPrevious(null);
            v.setSourceDistance(Double.MAX_VALUE);
        }

        startVertex.sourceDistance=0;

        Q.add(new QVertex(startVertex, 0));
        List<Vertex> prev = null;

        while (!Q.isEmpty()) {
            QVertex minQVertex = Q.poll();
            Vertex minVertex = minQVertex.getVertex();

            // 10/22/2019 I commented out the following lines because it prevents the method from finding multiple paths.
            // When a single path is found and the final state is reached, it exits the loop and method stops.
//            // 9/12/2018 NURAY: check if a final state is reached (Fg in our paper)
//            if(visitedVertices.containsAll(finalStateSet))
//            {
//                break;
//            }

            List<Edge> edgeList = minVertex.getEdges();

            for (Edge e : edgeList) {
                Vertex adjacent = e.getTargetVertex();
                QVertex adjacentVertex=new QVertex(adjacent,adjacent.getRisk());
                prev = adjacent.getPrev();

//                double newDistance = e.getProbability() * adjacent.getRisk() + minQVertex.getTotalRisk();
//                double newDistance = e.getProbability()+ minVertex.sourceDistance;
                double newDistance=e.getProbability()*adjacent.getRisk()+minVertex.sourceDistance;


                if (newDistance < adjacent.sourceDistance)
                {
                    Q.remove(adjacentVertex);
                    adjacent.sourceDistance = newDistance;
                    adjacent.setPrevious(minVertex);
                    Q.add(new QVertex(adjacent, adjacent.sourceDistance));
                    prev = new ArrayList<Vertex>();
                    prev.add(minVertex);
                    adjacent.setPrev(prev);
                }
                else if (newDistance == adjacent.sourceDistance)
                {
                    if (prev != null)
                        prev.add(minVertex);
                    else {
                        prev = new ArrayList<Vertex>();
                        prev.add(minVertex);
                        adjacent.setPrev(prev);
                    }
                }

//                if (!visitedVertices.contains(adjacent))
//                {
//                    visitedVertices.add(adjacent);
//                }
            }
        }
    }

    /**
     * This method also finds multiple paths between a given start and target vertex, but it reduces to a
     * single path considering the authorization privileges assigned to edges (min privilege)
     * @return
     */
    private void findOptimumReduced(Vertex source)
    {
        //initialize pq
        Q = new PriorityQueue<>(graphSize, new Comparator<QVertex>() {
            @Override
            public int compare(QVertex o1, QVertex o2) {
                return Double.compare(o1.getTotalRisk(), o2.getTotalRisk());
            }
        });

        //  9/12/2018 nuray: initialize set of visited vertices
//        Set<Vertex> visitedVertices = new HashSet<>();

        //initialize shortest distance of all vertices to infinity, and distance of starting vertex to zero
        for (Vertex v:graph.getVertices())
        {
            v.setPrevious(null);
            v.setSourceDistance(Double.MAX_VALUE);
        }

        source.sourceDistance=0;

        Q.add(new QVertex(source, 0));

        int minPrivSize=Integer.MAX_VALUE; // for selecting edges that have equal distance

        while (!Q.isEmpty()) {
            QVertex minQVertex = Q.poll();
            Vertex minVertex = minQVertex.getVertex();
            List<Edge> edgeList = minVertex.getEdges();

            for (Edge e : edgeList) {
                Vertex adjacent = e.getTargetVertex();
                QVertex adjacentVertex=new QVertex(adjacent,adjacent.getRisk());

//                double newDistance = e.getProbability()+ minVertex.sourceDistance;
                double newDistance=e.getProbability()*adjacent.getRisk()+minVertex.sourceDistance;

                if (newDistance < adjacent.sourceDistance)
                {
                    Q.remove(adjacentVertex);
                    adjacent.sourceDistance = newDistance;
                    adjacent.setPrevious(minVertex);
                    Q.add(new QVertex(adjacent, adjacent.sourceDistance));
                }
                else if (newDistance == adjacent.sourceDistance)
                {
                    VirtualAccessRequest var = e.getVar();
                    if(var!=null)
                    {
                        Map<Action, List<Attribute>> actionAttributes = var.getActionAttributes();
                        Set<Action> actions = actionAttributes.keySet();
                        int privilegeSize=actions.size();

                        if(privilegeSize<minPrivSize)
                        {
                            minPrivSize=privilegeSize;
                            adjacent.setPrevious(minVertex);
                        }
                    }
                }
            }
        }

    }


    /**
     * @param target
     * @return A List of nodes in order as they would appear in a shortest path.
     * (There can be multiple shortest paths present. This method
     * returns just one of those paths.)
     */
    public List<Vertex> getShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex
                .getPrevious())
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }

    /**
     * @param target
     * @return A set of all possible shortest paths from the source to the given
     * target.
     */
    public Set<List<Vertex>> getAllShortestPathsTo(Vertex target)
    {
        allShortestPaths = new HashSet<List<Vertex>>();

        getShortestPath(new ArrayList<Vertex>(), target);

        return allShortestPaths;
    }


    /**
     * Recursive method to enumerate all possible shortest paths and add each
     * path in the set of all possible shortest paths.
     *
     * @param shortestPath
     * @param target
     * @return
     */
    private List<Vertex> getShortestPath(List<Vertex> shortestPath,
                                         Vertex target)
    {
        List<Vertex> prev = target.getPrev();
        if (prev == null)
        {
//            shortestPath.add(target);
            Collections.reverse(shortestPath);
            List<Vertex> tempPath=new ArrayList<>();
            tempPath.add(target);
            tempPath.addAll(shortestPath);
            shortestPath=tempPath;
            allShortestPaths.add(shortestPath);
        }
        else
        {
            List<Vertex> updatedPath = new ArrayList<Vertex>(shortestPath);
            updatedPath.add(target);

            for (Vertex v:prev)
            {
                getShortestPath(updatedPath,v);
            }
        }
        return shortestPath;
    }

    private void printShortestPathLength(List<Vertex> path)
    {
        //System.out.println("\nWeight:" + length[targetVertex.getVertexID() - 1]);
        Vertex target=path.get(path.size()-1);
        System.out.println("\n Length of shortest path: "+target.sourceDistance);
    }

}
