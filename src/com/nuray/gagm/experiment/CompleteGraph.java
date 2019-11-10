package com.nuray.gagm.experiment;

import com.nuray.cpacexecution.cpacmodel.*;
import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.cpacexecution.storage.AgentBase;
import com.nuray.cpacexecution.storage.ResourceBase;
import com.nuray.gagm.pathfinder.GraphInt;
import com.nuray.gagm.pathfinder.Vertex;

import java.util.*;

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
     * @param weightLower This is the lower limit to generate edge probabilities. From this value, edge probabilities
     *                   will be calculated by dividing it to 100.0 (refer to Graph class). So, this value needs to be set zero.
     * @param weightUpper This is the upper limit to generate edge probabilities. From this value, edge probabilities
     *                    will be calculated by dividing it to 100.0 (refer to Graph class). So, this value needs to be set
     *                    hundred.
     * @param riskLower The lower limit to generate the risk of a vertex. There is no limit for this parameter.
     * @param riskUpper The upper limit to generate the risk of a vertex. There is no limit for this parameter.
     * @param fraction The proportion of critical and action edges. It should be between 0.0 and 1.0
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

    /**
     * This method overloads original generateVarMatrix method. Different than the original method, this method generates
     * vars for each edge with different size. In other words, number of agent and resource attributes, number of
     * actions etc. is not fixed among vars, but is generated randomly w.r.t. lower and upper limits that are given as parameters
     * to the method.
     * @param nAgentAttLower
     * @param nAgentAttUpper
     * @param nResAttLower
     * @param nResAttUpper
     * @param nActionAttLower
     * @param nActionAttUpper
     * @param nActionsLower
     * @param nActionsUpper
     * @param actionBase
     * @param operationalMode
     * @return
     * @throws Exception
     */
    public VirtualAccessRequest[][] generateVarMatrix(int nAgentAttLower,int nAgentAttUpper,
                                                      int nResAttLower,int nResAttUpper,
                                                      int nActionAttLower,int nActionAttUpper,
                                                      int nActionsLower, int nActionsUpper,
                                                      ActionBase actionBase,
                                                      String operationalMode) throws Exception {

        VirtualAccessRequest[][] varMatrix=new VirtualAccessRequest[numberOfVertices][numberOfVertices];
        Random random=new Random();
        Random random2=new Random(nAgentAttLower,nAgentAttUpper);
        Random random3=new Random(nResAttLower,nResAttUpper);
        Random random4=new Random(nActionAttLower,nActionAttUpper);
        Random random5=new Random(nActionsLower,nActionsUpper);

        int nAgentAtt,nResAtt,nActionAtt,nActions;

        for(int i=0; i < numberOfVertices;i++)
        {
            for(int j=0; j<=i;j++)
            {
                if(edgeTypeMatrix[i][j].equalsIgnoreCase("action"))
                {
                    nAgentAtt=random2.generateRandomIndex();
                    nResAtt=random3.generateRandomIndex();
                    nActionAtt=random4.generateRandomIndex();
                    nActions=random5.generateRandomIndex();

                    varMatrix[i][j] = random.generateRandomVar(nAgentAtt,nResAtt,nActionAtt,nActions,
                            actionBase,operationalMode);
                }
                if(edgeTypeMatrix[j][i].equalsIgnoreCase("action"))
                {
                    nAgentAtt=random2.generateRandomIndex();
                    nResAtt=random3.generateRandomIndex();
                    nActionAtt=random4.generateRandomIndex();
                    nActions=random5.generateRandomIndex();

                    varMatrix[j][i]= random.generateRandomVar(nAgentAtt,nResAtt,nActionAtt,nActions,
                            actionBase,operationalMode);
                }
            }
        }
        return varMatrix;
    }

    public VirtualAccessRequest[][] generateVarMatrixForExperiment(ResourceBase resourcebase,
                                                                   AgentBase agentBase,
                                                                   ActionBase actionBase,
                                                                   int nAgentAttLower,int nAgentAttUpper,
                                                                   int nResAttLower,int nResAttUpper,
                                                                   int nActionAttLower,int nActionAttUpper,
                                                                   int nActionsLower, int nActionsUpper,
                                                                   String operationalMode) throws Exception {
        VirtualAccessRequest[][] varMatrix=new VirtualAccessRequest[numberOfVertices][numberOfVertices];
//        Random random=new Random();
//        Random random2=new Random(nAgentAttLower,nAgentAttUpper);
//        Random random3=new Random(nResAttLower,nResAttUpper);
//        Random random4=new Random(nActionAttLower,nActionAttUpper);
//        Random random5=new Random(nActionsLower,nActionsUpper);
//
//        int nAgentAtt,nResAtt,nActionAtt,nActions;

        Resource resource = resourcebase.getResourceList().get(0); // in experiments, I assign same set of atts to all resources
        List<Attribute> resourceAttributes = resource.getResourceAttributes();

        Agent agent = agentBase.getAgentList().get(0);// in experiments, I assign same set of atts to all agents, so I can use only one of them
        List<Attribute> agentAttributes = agent.getAgentAttributes();

        for(int i=0; i < numberOfVertices;i++)
        {
            for(int j=0; j<=i;j++)
            {
                if(edgeTypeMatrix[i][j].equalsIgnoreCase("action"))
                {
//                    nAgentAtt=random2.generateRandomIndex();
//                    nResAtt=random3.generateRandomIndex();
//                    nActionAtt=random4.generateRandomIndex();
//                    nActions=random5.generateRandomIndex();
//
//                    List<Attribute> agentAttList=selectRandomAttributes(agentAttributes,nAgentAtt);
//                    List<Attribute> resourceAttList=selectRandomAttributes(resourceAttributes,nResAtt);
//                    List<Action> actions = selectRandomActions(actionBase.getActionList(),nActions);

                    List<Attribute> agentAttList=selectRandomAttributes(agentAttributes,"agent");
                    List<Attribute> resourceAttList=selectRandomAttributes(resourceAttributes,"resource");
                    List<Action> actions = selectRandomActions(actionBase.getActionList());

                    Map<Action,List<Attribute>> actionToAttMap=new HashMap<>();

                    for (Action action:actions)
                    {
//                        List<Attribute> actionAttributes = action.getActionAttributes();
//                        List<Attribute> randomAttributes = selectRandomAttributes(actionAttributes, nActionAtt);
//                        actionToAttMap.put(action,randomAttributes);
                        List<Attribute> actionAttributes = action.getActionAttributes();
                        List<Attribute> randomAttributes = selectRandomAttributes(actionAttributes,"action");
                        actionToAttMap.put(action,randomAttributes);
                    }

                    varMatrix[i][j] = new VirtualAccessRequest(agentAttList,resourceAttList,actionToAttMap,operationalMode);
                }
                if(edgeTypeMatrix[j][i].equalsIgnoreCase("action"))
                {
//                    nAgentAtt=random2.generateRandomIndex();
//                    nResAtt=random3.generateRandomIndex();
//                    nActionAtt=random4.generateRandomIndex();
//                    nActions=random5.generateRandomIndex();
//
//                    List<Attribute> agentAttList=selectRandomAttributes(agentAttributes,nAgentAtt);
//                    List<Attribute> resourceAttList=selectRandomAttributes(resourceAttributes,nResAtt);
//                    List<Action> actions = selectRandomActions(actionBase.getActionList(),nActions);

                    List<Attribute> agentAttList=selectRandomAttributes(agentAttributes,"agent");
                    List<Attribute> resourceAttList=selectRandomAttributes(resourceAttributes,"resource");
                    List<Action> actions = selectRandomActions(actionBase.getActionList());

                    Map<Action,List<Attribute>> actionToAttMap=new HashMap<>();

                    for (Action action:actions)
                    {
//                        List<Attribute> actionAttributes = action.getActionAttributes();
//                        List<Attribute> randomAttributes = selectRandomAttributes(actionAttributes, nActionAtt);
//                        actionToAttMap.put(action,randomAttributes);

                        List<Attribute> actionAttributes = action.getActionAttributes();
                        List<Attribute> randomAttributes = selectRandomAttributes(actionAttributes,"action");
                        actionToAttMap.put(action,randomAttributes);
                    }

                    varMatrix[j][i]= new VirtualAccessRequest(agentAttList,resourceAttList,actionToAttMap,operationalMode);
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

    private List<Attribute> selectRandomAttributes(List<Attribute> attributes, String elementType)
    {
        List<Attribute> list = new LinkedList<>(attributes);
        // remove any predefined attributes inside resource, agent, and action classes since they are not assigned with
        // initial value automatically. This hurts my experiment implementation. (only leave ID attributes for retrival purposes).

        if(elementType.equalsIgnoreCase("resource"))
        {
            List<String> predefinedResAtt = CPACSpecifications.resourceAttributes;
            list = removePredefinedAttributes(predefinedResAtt, list,"resource");
        }
        else if(elementType.equalsIgnoreCase("agent"))
        {
            List<String> predefinedAgnAtt = CPACSpecifications.agentAttributes;
            list = removePredefinedAttributes(predefinedAgnAtt,list,"agent");
        }
        else
        {
            List<String> predefinedActAtt = CPACSpecifications.actionAttributes;
            list = removePredefinedAttributes(predefinedActAtt,list,"action");
        }

        Random random=new Random(1,list.size());
        int nAttributesToSelect=random.generateRandomIndex();

        if(list.size()>0)
        {
            return list.subList(0, nAttributesToSelect);
        }
        else
        {
            return attributes;
        }
    }

    private List<Attribute> selectRandomAttributes(List<Attribute> attributes, int nAttributes)
    {
        List<Attribute> list = new LinkedList<>(attributes);
        Collections.shuffle(list);
        return list.subList(0, nAttributes);
    }

    private List<Action> selectRandomActions(List<Action> actions)
    {
        List<Action> list = new LinkedList<>(actions);

        Random random=new Random(1,list.size());
        int nActionsToSelect=random.generateRandomIndex();

        return list.subList(0, nActionsToSelect);
    }

    private List<Action> selectRandomActions(List<Action> actions, int nActions)
    {
        List<Action> list = new LinkedList<>(actions);
        Collections.shuffle(list);
        return list.subList(0, nActions);
    }

    private List<Attribute> removePredefinedAttributes(List<String> predefinedAttList, List<Attribute> attributes,String elementType)
    {
        for (String predefinedAtt:predefinedAttList)
        {
            /*
            If agent attributes are given as the parameter, we remove "agentID" as including "agentID" in a var causes
            to select only one single agent as a set of eligible agents (agentList_v_cur/agentList_v_next) in
            executeCPAC() method.
             */
            if(elementType.equalsIgnoreCase("agent"))
            {
                attributes.removeIf(a->(a.getAttributeName().equalsIgnoreCase(predefinedAtt)));
            }
            /*
            If resource or action attributes are given as the parameter, we do not remove "resourceID" or "actionID" as
            those are used to retrieve resource and actions to generate "perm_var" out of given "var" in
            executeCPAC() method.
             */
            else
            {
                attributes.removeIf(a->(a.getAttributeName().equalsIgnoreCase(predefinedAtt) && !a.getAttributeName().endsWith("ID")));
            }
        }
        return attributes;
    }
}
