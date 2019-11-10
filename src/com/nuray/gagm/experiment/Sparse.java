package com.nuray.gagm.experiment;

import com.nuray.cpacexecution.cpacmodel.*;
import com.nuray.cpacexecution.enforcementfunctions.VirtualAccessRequest;
import com.nuray.cpacexecution.storage.ActionBase;
import com.nuray.cpacexecution.storage.AgentBase;
import com.nuray.cpacexecution.storage.ResourceBase;
import com.nuray.gagm.pathfinder.GraphInt;
import com.nuray.gagm.pathfinder.Vertex;

import java.text.DecimalFormat;
import java.util.*;

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
     *
     */
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
                                                                   String operationalMode) throws Exception
    {
        VirtualAccessRequest[][] varMatrix=new VirtualAccessRequest[numberOfVertices][numberOfVertices];

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
                    List<Attribute> agentAttList=selectRandomAttributes(agentAttributes,"agent");
                    List<Attribute> resourceAttList=selectRandomAttributes(resourceAttributes,"resource");
                    List<Action> actions = selectRandomActions(actionBase.getActionList());

                    Map<Action,List<Attribute>> actionToAttMap=new HashMap<>();

                    for (Action action:actions)
                    {
                        List<Attribute> actionAttributes = action.getActionAttributes();
                        List<Attribute> randomAttributes = selectRandomAttributes(actionAttributes,"action");
                        actionToAttMap.put(action,randomAttributes);
                    }

                    varMatrix[i][j] =new VirtualAccessRequest(agentAttList,resourceAttList,actionToAttMap,operationalMode);
                }
                if(edgeTypeMatrix[j][i].equalsIgnoreCase("action"))
                {
                    List<Attribute> agentAttList=selectRandomAttributes(agentAttributes,"agent");
                    List<Attribute> resourceAttList=selectRandomAttributes(resourceAttributes,"resource");
                    List<Action> actions = selectRandomActions(actionBase.getActionList());

                    Map<Action,List<Attribute>> actionToAttMap=new HashMap<>();

                    for (Action action:actions)
                    {
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

    private List<Action> selectRandomActions(List<Action> actions)
    {
        List<Action> list = new LinkedList<>(actions);

        Random random=new Random(1,list.size());
        int nActionsToSelect=random.generateRandomIndex();

        return list.subList(0, nActionsToSelect);
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
