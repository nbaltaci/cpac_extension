package com.nuray.cpacexecution.experiment;


import com.nuray.cpacexecution.ExecutionOfCPAC;
import com.nuray.cpacexecution.ExecutionOfCPAC_MERVersion;
import com.nuray.cpacexecution.cpacmodel.*;
import com.nuray.cpacexecution.enforcementfunctions.*;
import com.nuray.cpacexecution.storage.*;
import com.nuray.gagm.experiment.CompleteGraph;
import com.nuray.gagm.experiment.Random;
import com.nuray.gagm.experiment.Sparse;
import com.nuray.gagm.pathfinder.Edge;
import com.nuray.gagm.pathfinder.Graph;
import com.nuray.gagm.pathfinder.VARGenerationMultipleShortestPathVersion;
import com.nuray.gagm.pathfinder.Vertex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ExperimentTimePerformance
{

    private static ResourceBase resourceBase;
    private static ActionBase actionBase;
    private static AgentBase agentBase;
    private static PolicyBase policyBase;
    private static SODBase sodBase;

    private static ExecutionOfCPAC execution;


    public static void main(String args[]) throws Exception
    {
        experimentTimePerformance(100);
//        experimentTimePerformanceImproved(100);
    }

    private static void experimentTimePerformance(int nIterations) throws Exception
    {
//        // experiment for different fraction values and write them to a file
//        experimentTimeForFractions(nIterations);
//
//        // experiment for different number of vertices and write them to a file
//        experimentTimeForNVertices(nIterations);

        // The following is not a crucial experiment actually since it only generates two values:
        // one avg time value for a sparse graph and one avg time value for a complete graph.
        // Also, we only use a single path taken from a randomly generated graph and use two edges from that
        // path. So, the graph itself is not used in the executeCPAC() alg. Hence, these experiments are
        // not so necessary.
//        // experiment for different graph types (sparse vs complete) and write them to a file
//        experimentTimeForGraphType(nIterations);

        // experiment for different number of resources and write them to a file
        experimentTimeForNResources(nIterations);

        // experiment for different number of agents and write them to a file
        experimentTimeForNAgents(nIterations);

        // experiment for different number of resource attributes and write them to a file
        experimentTimeForNResourceAtts(nIterations);

        // experiment for different number of agent attributes and write them to a file
        experimentTimeForNAgentAtts(nIterations);

        // experiment for different number of sod constraints and write them to a file
        experimentTimeForNSoDRules(nIterations);

    }

    /*
        This version of the experiment considers time performance when sod verification process
        (checking if a given state respects to sod constraints) is improved. In the previous version of the experiments
        (experimentTimePerformance() method), sod verification was being handled over "permissions". This method
        is proven to be intractable in the following articles. Instead, converting sod policy rules to mutually exclusive
        policy rules (MERs) is proposed for sod verification. Thus, in this experiment, we convert sod constraints to
        MER constraints and use them in executeCPAC() algorithm to see if there is an improvement in time performance.

            [1] S.  Jha,  S.  Sural,  V.  Atluri,  and  J.  Vaidya,  “Enforcing  separation  of  duty  in  attribute  based
              access  control  systems,”inInternational Conference on Information Systems Security.Springer, 2015, pp. 61–78
            [2] Specification  and  verification  of  separation  of  dutyconstraints in attribute-based access control,”IEEE
             Transactions  on  Information  Forensics  and  Security,  vol.  13,  no.  4,pp. 897–911, 2017.
     */
    private static void experimentTimePerformanceImproved(int nIterations) throws Exception
    {
        // experiment for different fraction values and write them to a file
        experimentTimeForFractionsImproved(nIterations);

        // experiment for different number of vertices and write them to a file
        experimentTimeForNVerticesImproved(nIterations);

//        // experiment for different graph types (sparse vs complete) and write them to a file
//        experimentTimeForGraphTypeImproved(nIterations);

        // experiment for different number of resources and write them to a file
        experimentTimeForNResourcesImproved(nIterations);

        // experiment for different number of agents and write them to a file
        experimentTimeForNAgentsImproved(nIterations);

        // experiment for different number of resource attributes and write them to a file
        experimentTimeForNResourceAttsImproved(nIterations);

        // experiment for different number of agent attributes and write them to a file
        experimentTimeForNAgentAttsImproved(nIterations);

        // experiment for different number of sod constraints and write them to a file
        experimentTimeForNSoDRulesImproved(nIterations);

    }

    private static void experimentTimeForFractions(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different fraction values and write them to a file
        double[] fractions={0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};

        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};
//        int[] nPolicyRules={10};


        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentFractions.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|=" + nPolicyRule + ": \n");
            for (double f:fractions)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(50, 5,
                            50, 5,
                            5,nPolicyRule,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, f,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when fraction of critical edges="+f+" is "+avgTime+" milliseconds \n");
            }

        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNVertices(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of vertices and write them to a file
        int[] nVertices={5,10,20,40,60,80,100,1000};

        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};
//        int[] nPolicyRules={10};

        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNVertices.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|=" + nPolicyRule + ": \n");

            for (int n:nVertices)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(50, 5,
                            50, 5,
                            5,nPolicyRule,
                            2, 1, 100,
                            "sparse", "emergency",
                            n, 1, 100, 0.5,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when number of vertices="+n+" is "+avgTime+" milliseconds \n");
            }
        }
        bw.close();
        fw.close();
    }

    private static void experimentTimeForGraphType(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different graph types and write them to a file
        String[] graphTypes={"complete","sparse"};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForCompleteVsSparse.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (String gt:graphTypes)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtility(50, 5,
                        50, 5,
                        5,100,
                        2, 1, 100,
                        gt, "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when graph type= "+gt+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNResources(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of resources and write them to a file
        int[] nResources={10,20,30,40,50,60,70,80,90,100};


        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};
//        int[] nPolicyRules={10};


        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNResources.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|=" + nPolicyRule + ": \n");
            for (int nRes:nResources)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(nRes, 5,
                            50, 5,
                            5,nPolicyRule,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when number of resources="+nRes+" is "+avgTime+" milliseconds \n");
            }
        }
        bw.close();
        fw.close();
    }

    private static void experimentTimeForNAgents(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nAgents={10,20,30,40,50,60,70,80,90,100};

        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};
//        int[] nPolicyRules={10};

        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNAgents.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|=" + nPolicyRule + ": \n");
            for (int nAgn:nAgents)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(50, 5,
                            nAgn, 5,
                            5,nPolicyRule,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when number of agents="+nAgn+" is "+avgTime+" milliseconds \n");
            }
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNResourceAtts(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of resource attributes and write them to a file
//        int[] nResourceAtts={5,10,20,30,40,50,60,70,80,90,100,150,200,250,300};
        int[] nResourceAtts={5,10,20,30,40,50,60,70,80,90,100};

        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};

        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNResourceAtts.txt");
        BufferedWriter bw=new BufferedWriter(fw);

//        // record running times of each method under "executeCPAC()" algorithm
//        Map<String,Long> totalMethodRunningTimes=new HashMap<>();
//        Map<String,Long> avgMethodRunningTimes=new HashMap<>();

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|="+nPolicyRule+": \n");
            for (int nResAtt:nResourceAtts)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(50, nResAtt,
                            50, 5,
                            5,nPolicyRule,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
//                    Map<String, Long> methodRunningTimes = execution.methodRunningTimes;
//                    if(totalMethodRunningTimes.size()==0)
//                    {
//                        totalMethodRunningTimes=methodRunningTimes;
//                    }
//                    else
//                    {
//                        Iterator<String> iterator = totalMethodRunningTimes.keySet().iterator();
//                        while (iterator.hasNext())
//                        {
//                            String methodName = iterator.next();
//                            Long totalMethodRunTime = totalMethodRunningTimes.get(methodName);
//                            totalMethodRunTime=totalMethodRunTime+methodRunningTimes.get(methodName);
//                            totalMethodRunningTimes.put(methodName,totalMethodRunTime);
//                        }
//                    }

                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when the number of resource attributes (of resources stored in the system="
                        +nResAtt+" is "+avgTime+" milliseconds \n");

//            bw.write("\tTime for methods inside the executeCPAC(): \n");
//            Iterator<String> iterator = totalMethodRunningTimes.keySet().iterator();
//            while (iterator.hasNext())
//            {
//                String methodName = iterator.next();
//                Long totalMethodRunningTime = totalMethodRunningTimes.get(methodName);
//                double avgMethodTime=(double) totalMethodRunningTime/countForNonNegativeResults;
//
//                bw.write("\t \t Time for "+methodName+" method: "+avgMethodTime+" ms\n");
//            }
            }
        }



        bw.close();
        fw.close();
    }

    private static void experimentTimeForNAgentAtts(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nAgentAtts={5,10,20,30,40,50,60,70,80,90,100};

        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};

        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNAgentAtts.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|="+nPolicyRule+": \n");
            for (int nAgnAtt:nAgentAtts)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(50, 5,
                            50, nAgnAtt,
                            5,nPolicyRule,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when the number of agent attributes (of agents stored in the system="
                        +nAgnAtt+" is "+avgTime+" milliseconds \n");
            }
        }

        bw.close();
        fw.close();
    }


    private static void experimentTimeForNSoDRules(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nSoDRules={2,5,10,20,30,40,50,60,70,80,90,100};


        // repeat experiment for different policy size (when policy rules are generated independent
        // from sod policy rules in "experimentTimePerformanceUtility" method.
        int[] nPolicyRules={10,30,50,70,100};


        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNSoDConstraints.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        // first, warm-up experiments to prevent time results being affected from java JIT compilation,
        // class-loading etc.
        warmup();

        // continue with real experiments
        for (int nPolicyRule:nPolicyRules)
        {
            bw.write("RESULTS FOR |P|=" + nPolicyRule + ": \n");
            for (int nSoD:nSoDRules)
            {
                int count=0;
                while (count<nIterations)
                {
                    long time = experimentTimePerformanceUtility(50, 5,
                            50, 5,
                            5,nPolicyRule,
                            nSoD, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            1, 5, 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
                    }
                    count++;
                }
                double avgTime=(double) totalTime/countForNonNegativeResults;
                bw.write("Average time when the number of SoD constraints="
                        +nSoD+" is "+avgTime+" milliseconds \n");
            }
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNAgnAttInVars(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nAgnAttsInVars={1,5,10,20,30,40,50};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNAgnAttInVars.txt");
        BufferedWriter bw=new BufferedWriter(fw);

//        for (int nAgnAtt:nAgnAttsInVars)
        for (int i=0;i<nAgnAttsInVars.length;i++)
        {
            int count=0;
            while (count<nIterations)
            {
                if(i==nAgnAttsInVars.length-1) // if this is last element in the # agent attributes array
                {

                }
                else
                {
                    long time = experimentTimePerformanceUtility(50, 5,
                            50, 5,
                            5,100,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            nAgnAttsInVars[i], nAgnAttsInVars[i+1], 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                    }
                }

                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when the number of agent attributes in vars changes between ["
                    +nAgnAttsInVars[i]+", "+nAgnAttsInVars[i+1]+" is: "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }


    /*
    This method generate random policy elements, graph/ varList,policies and calls solveForTimePerformance() method
    that runs executeCPAC() algorithm and returns its running time.
     */
    private static long experimentTimePerformanceUtility(int nResources, int nResAttributes,
                                                         int nAgents, int nAgentAttributes,
                                                         int nActions,int nPolicyRules,
                                                         int nSoDRules, int riskLowLimit, int riskUpperLimit,
                                                         String graphType,String operationalMode,
                                                         int numberOfVertices, int riskLower, int riskUpper, double fraction,
                                                         int nAgentAttLower,int nAgentAttUpper, int nResAttLower,int nResAttUpper,
                                                         int nActionAttLower,int nActionAttUpper,
                                                         int nActionsLower, int nActionsUpper) throws Exception
    {
        long startTime=System.nanoTime();
        generatePolicyElements(nResources,nResAttributes, nAgents,nAgentAttributes, nActions);
        long endTime=System.nanoTime();
        long timeDiff = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("Time for generating policy elements: "+timeDiff);


        // 4. call executeCPAC algorithm
        ExecutionOfCPAC executionOfCPAC=new ExecutionOfCPAC(resourceBase,actionBase,agentBase,policyBase,sodBase);

        // 4.a. generate a random graph
        // when "finalStates" is passed as null, Graph class randomly generates final states
        startTime=System.nanoTime();
        Graph graph = generateRandomGraph(graphType,operationalMode,numberOfVertices,
                riskLower,riskUpper, fraction,nAgentAttLower,nAgentAttUpper,nResAttLower,nResAttUpper,
                nActionAttLower,nActionAttUpper,nActionsLower,nActionsUpper,null);
        endTime=System.nanoTime();
        timeDiff=TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("Time for generating graph: "+timeDiff);


        // 4.b. generate random start and target vertex
        Random random=new Random(1,graph.getNumberOfVertices());
        int startVertexNo=random.generateRandomIndex();
        int targetVertexNo=random.generateRandomIndex();

        // 4.c. generate varList_v_cur
        startTime=System.nanoTime();
        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur =
                generateVarListForExperiment(graph,startVertexNo,targetVertexNo);
        endTime=System.nanoTime();
        timeDiff=TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("Time for shortest path calculation: "+timeDiff);

        Vertex v_cur=null;
        List<Vertex> vertices = graph.getVertices();
        for(int i=0;i<vertices.size();i++)
        {
            if(vertices.get(i).getVertexID()==startVertexNo)
            {
                v_cur=vertices.get(i);
                break;
            }
        }


        VirtualAccessRequest[] vars = executionOfCPAC.extractVars(varList_v_cur,v_cur); // vars[] hold var_v_cur and var_v_next
        if(vars[0]!=null)
        {
            // 4.d. find v_next from varList_v
            Vertex v_next=executionOfCPAC.findVarNextOnShortestPath(v_cur,varList_v_cur);

            // extract the permissions for var_v_cur and var_v_next off-line
            vars[0].setPermission(resourceBase);
            if(vars[1]!=null)
            {
                vars[1].setPermission(resourceBase);
            }


            // 4.e. Generate an sod policy with multiple rules
            /*
            Note that for this experiment, sod policy needs to be generated after varList_v_cur is generated (look to the
            explanation of generateSoDPolicyForExperiment() method for further explanation).
             */
            SODPolicy sodPolicy=generateSoDPolicyForExperiment(nSoDRules,riskLowLimit,riskUpperLimit,vars[0]);
            // 4.f. Add sod policy to repository
            sodBase.addSODPolicy(sodPolicy);

            // 4.g. Generate authorization policy w.r.t. given sod
//            Policy policy=generatePolicyForExperiment(sodPolicy,operationalMode);
            // 4.g. Or generate a fixed number of authorization policies independent from sod policy rules
            Policy policy=generatePolicy(nPolicyRules,operationalMode);
            policyBase.addPolicy(policy);

            // 4.h. call solveForTimePerformance() method

            startTime=System.nanoTime();
            long time = solveForTimePerformance(executionOfCPAC, varList_v_cur, operationalMode, v_cur, v_next,vars);
//            execution.methodRunningTimes=executionOfCPAC.methodRunningTimes;
            endTime=System.nanoTime();

            timeDiff=TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            System.out.println("Time for executeCPAC: "+timeDiff);

            return time;
        }
        else
        {
            return -1;
        }
    }

    /*
        Run the algorithm for 500 times for benchmarking purposes before starting the actual experiments.
        This helps to obtain stable running time results and avoid from outlier results for the first couple
        of running a method (Due to lazy compilation feature of Java JIT).
     */
    public static void warmup() throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        int count=0;
        while (count<500)
        {
            long time = experimentTimePerformanceUtility(50, 5,
                    50, 5,
                    5,100,
                    2, 1, 100,
                    "sparse", "emergency",
                    100, 1, 100, 0.5,
                    1, 5, 1, 5,
                    1, 1,
                    1, 5);
            if(time>0)
            {
                totalTime+=time;
                countForNonNegativeResults++;
            }
            count++;
        }

    }


        /*
        These methods consider improvement for time performance such that sod constraints (sod policy rules) are
        converted to MER constraints.
     */

    private static void experimentTimeForFractionsImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different fraction values and write them to a file
        double[] fractions={0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentFractions_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (double f:fractions)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, 5,
                        50, 5,
                        5,100,
                        2, 1, 100,
                        "sparse", "emergency",
                        100, 1, 100, f,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when fraction of critical edges="+f+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNVerticesImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of vertices and write them to a file
        int[] nVertices={5,10,20,40,60,80,100,1000};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNVertices_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (int n:nVertices)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, 5,
                        50, 5,
                        5,100,
                        2, 1, 100,
                        "sparse", "emergency",
                        n, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when number of vertices="+n+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForGraphTypeImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different graph types and write them to a file
        String[] graphTypes={"complete,sparse"};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForCompleteVsSparse_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (String gt:graphTypes)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, 5,
                        50, 5,
                        5,100,
                        2, 1, 100,
                        gt, "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when graph type= "+gt+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNResourcesImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of resources and write them to a file
        int[] nResources={10,20,30,40,50,60,70,80,90,100};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNResources_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (int nRes:nResources)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(nRes, 5,
                        50, 5,
                        5,100,
                        2, 1, 100,
                        "sparse", "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when number of resources="+nRes+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNAgentsImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nAgents={10,20,30,40,50,60,70,80,90,100};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNAgents_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (int nAgn:nAgents)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, 5,
                        nAgn, 5,
                        5,100,
                        2, 1, 100,
                        "sparse", "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when number of agents="+nAgn+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNResourceAttsImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nResourceAtts={5,10,20,30,40,50,60,70,80,90,100};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNResourceAtts_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (int nResAtt:nResourceAtts)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, nResAtt,
                        50, 5,
                        5,100,
                        2, 1, 100,
                        "sparse", "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when the number of resource attributes (of resources stored in the system="
                    +nResAtt+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNAgentAttsImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nAgentAtts={5,10,20,30,40,50,60,70,80,90,100};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNAgentAtts_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (int nAgnAtt:nAgentAtts)
        {
            int count=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, 5,
                        50, nAgnAtt,
                        5,100,
                        2, 1, 100,
                        "sparse", "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when the number of agent attributes (of agents stored in the system="
                    +nAgnAtt+" is "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }


    private static void experimentTimeForNSoDRulesImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
//        int[] nSoDRules={2,5,10,20,30,40};
        int[] nSoDRules={2,5,10};

        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNSoDConstraints_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

        for (int nSoD:nSoDRules)
        {
            int count=0;
            int totalNPolicyRules=0;
            while (count<nIterations)
            {
                long time = experimentTimePerformanceUtilityImproved(50, 5,
                        50, 5,
                        5,100,
                        nSoD, 1, 100,
                        "sparse", "emergency",
                        100, 1, 100, 0.5,
                        1, 5, 1, 5,
                        1, 1,
                        1, 5);
                if(time>0)
                {
                    totalTime+=time;
                    countForNonNegativeResults++;

                    Iterator<Policy> policyIterator = policyBase.getPolicyList().iterator();
                    while (policyIterator.hasNext())
                    {
                        Policy policy = policyIterator.next();
                        int nPolicyRules=policy.getPolicyRules().size();
                        totalNPolicyRules+=nPolicyRules;
                    }

                }
                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            double avgNPolicyRules=(double) totalNPolicyRules/countForNonNegativeResults;
            bw.write("Average time when the number of SoD constraints="
                    +nSoD+" is "+avgTime+" milliseconds (avg number of policy rules generated= "+avgNPolicyRules+"\n");
        }

        bw.close();
        fw.close();
    }

    private static void experimentTimeForNAgnAttInVarsImproved(int nIterations) throws Exception
    {
        long totalTime=0;
        int countForNonNegativeResults=0;

        // experiment for different number of agents and write them to a file
        int[] nAgnAttsInVars={1,5,10,20,30,40,50};
        FileWriter fw=new FileWriter("src/com/nuray/cpacexecution/experiment/timeForDifferentNAgnAttInVars_improved.txt");
        BufferedWriter bw=new BufferedWriter(fw);

//        for (int nAgnAtt:nAgnAttsInVars)
        for (int i=0;i<nAgnAttsInVars.length;i++)
        {
            int count=0;
            while (count<nIterations)
            {
                if(i==nAgnAttsInVars.length-1) // if this is last element in the # agent attributes array
                {

                }
                else
                {
                    long time = experimentTimePerformanceUtilityImproved(50, 5,
                            50, 5,
                            5,100,
                            2, 1, 100,
                            "sparse", "emergency",
                            100, 1, 100, 0.5,
                            nAgnAttsInVars[i], nAgnAttsInVars[i+1], 1, 5,
                            1, 1,
                            1, 5);
                    if(time>0)
                    {
                        totalTime+=time;
                        countForNonNegativeResults++;
//                System.out.println("Time for running the alg: "+time);
                    }
                }

                count++;
            }
            double avgTime=(double) totalTime/countForNonNegativeResults;
            bw.write("Average time when the number of agent attributes in vars changes between ["
                    +nAgnAttsInVars[i]+", "+nAgnAttsInVars[i+1]+" is: "+avgTime+" milliseconds \n");
        }

        bw.close();
        fw.close();
    }

    private static long experimentTimePerformanceUtilityImproved(int nResources, int nResAttributes,
                                                         int nAgents, int nAgentAttributes,
                                                         int nActions,int nPolicyRules,
                                                         int nSoDRules, int riskLowLimit, int riskUpperLimit,
                                                         String graphType,String operationalMode,
                                                         int numberOfVertices, int riskLower, int riskUpper, double fraction,
                                                         int nAgentAttLower,int nAgentAttUpper, int nResAttLower,int nResAttUpper,
                                                         int nActionAttLower,int nActionAttUpper,
                                                         int nActionsLower, int nActionsUpper) throws Exception
    {
        generatePolicyElements(nResources,nResAttributes, nAgents,nAgentAttributes, nActions);

        // 4. call executeCPAC algorithm
        ExecutionOfCPAC_MERVersion executionOfCPAC=
                                        new ExecutionOfCPAC_MERVersion(resourceBase,actionBase,agentBase,policyBase,sodBase);

        // 4.a. generate a random graph
        // when "finalStates" is passed as null, Graph class randomly generates final states
        Graph graph = generateRandomGraph(graphType,operationalMode,numberOfVertices,
                riskLower,riskUpper, fraction,nAgentAttLower,nAgentAttUpper,nResAttLower,nResAttUpper,
                nActionAttLower,nActionAttUpper,nActionsLower,nActionsUpper,null);

        // 4.b. generate random start and target vertex
        Random random=new Random(1,graph.getNumberOfVertices());
        int startVertexNo=random.generateRandomIndex();
        int targetVertexNo=random.generateRandomIndex();

        // 4.c. generate varList_v_cur
        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur =
                generateVarListForExperiment(graph,startVertexNo,targetVertexNo);

        Vertex v_cur=null;
        List<Vertex> vertices = graph.getVertices();
        for(int i=0;i<vertices.size();i++)
        {
            if(vertices.get(i).getVertexID()==startVertexNo)
            {
                v_cur=vertices.get(i);
                break;
            }
        }


        VirtualAccessRequest[] vars = executionOfCPAC.extractVars(varList_v_cur,v_cur); // vars[] hold var_v_cur and var_v_next
        if(vars[0]!=null)
        {
            // 4.d. find v_next from varList_v
            Vertex v_next=executionOfCPAC.findVarNextOnShortestPath(v_cur,varList_v_cur);

            // 4.e. Generate an sod policy with multiple rules
            /*
            Note that for this experiment, sod policy needs to be generated after varList_v_cur is generated (look to the
            explanation of generateSoDPolicyForExperiment() method for further explanation).
             */
            SODPolicy sodPolicy=generateSoDPolicyForExperiment(nSoDRules,riskLowLimit,riskUpperLimit,vars[0]);
            // 4.f. Add sod policy to repository
            sodBase.addSODPolicy(sodPolicy);

            // 4.g. Generate authorization policy w.r.t. given sod
            Policy policy=generatePolicyForExperiment(sodPolicy,operationalMode);
//            Policy policy=generatePolicy(nPolicyRules,operationalMode);
            policyBase.addPolicy(policy);

            // convert each SODPolicyRule to a set of MER constraints for performance improvement purposes
            convertSoDToMER(sodPolicy);

            // 4.h. call solveForTimePerformance() method

            long time = solveForTimePerformanceImproved(executionOfCPAC, varList_v_cur, operationalMode, v_cur, v_next,vars);
            return time;
        }
        else
        {
            return -1;
        }
    }

    private static void convertSoDToMER(SODPolicy sodPolicy)
    {
        List<SODPolicyRule> sodPolicyRules = sodPolicy.getSodPolicyRules();
        for (SODPolicyRule sodPolicyRule:sodPolicyRules)
        {
            try
            {
                SODPolicyRule.SoAPPolicyRule soAP =sodPolicyRule.createSoAP(policyBase,agentBase);
                Set<SODPolicyRule.MERConstraint> merConstraints =sodPolicyRule.generateMER(soAP);
                sodPolicyRule.setMERSet(merConstraints);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * This method generates policy components, policy rules (both authorization and sod), graph, varList_v_cur
     * every time it is called.
     *
     *
     * @param nResources
     * @param nResAttributes
     * @param nAgents
     * @param nAgentAttributes
     * @param nActions
     * @param nSoDRules
     * @param riskLowLimit lower limit for randomly generated risk values of sod policy rules
     * @param riskUpperLimit upper limit for randomly generated risk values of sod policy rules
     * @param graphType
     * @param operationalMode
     * @param numberOfVertices
     * @param riskLower lower limit for randomly generated risk values of vertices in a graph
     * @param riskUpper upper limit for randomly generated risk values of vertices in a graph
     * @param fraction
     * @param nAgentAttLower
     * @param nAgentAttUpper
     * @param nResAttLower
     * @param nResAttUpper
     * @param nActionAttLower
     * @param nActionAttUpper
     * @param nActionsLower
     * @param nActionsUpper
     * @return number of eligible agents for the given experiment settings (i.e. risk values). If there is no eligible agents,
     * it returns zero. If the randomly generated graph inside this method yields no "var" for the randomly created "v_cur",
     * the method returns -1.
     * @throws Exception
     */
    private static int solveWithGeneration(int nResources, int nResAttributes,
                                             int nAgents, int nAgentAttributes,
                                             int nActions,
                                             int nSoDRules, int riskLowLimit, int riskUpperLimit,
                                             String graphType,String operationalMode,
                                             int numberOfVertices, int riskLower, int riskUpper, double fraction,
                                             int nAgentAttLower,int nAgentAttUpper, int nResAttLower,int nResAttUpper,
                                             int nActionAttLower,int nActionAttUpper,
                                             int nActionsLower, int nActionsUpper) throws Exception
    {
//        generatePoliciesAndPolicyElements(isSoDViolation,
//                                nResources,nResAttributes,
//                                nAgents,nAgentAttributes,
//                                nActions,
//                                nPolicyRules,
//                                riskLowLimit,riskUpperLimit,
//                                operationalMode);

        generatePolicyElements(nResources,nResAttributes, nAgents,nAgentAttributes, nActions);

        // 4. call executeCPAC algorithm
        ExecutionOfCPAC executionOfCPAC=new ExecutionOfCPAC(resourceBase,actionBase,agentBase,policyBase,sodBase);

        Date date=new Date();

            // 4.a. generate a random graph
        // when "finalStates" is passed as null, Graph class randomly generates final states
        Graph graph = generateRandomGraph(graphType,operationalMode,numberOfVertices,
                riskLower,riskUpper, fraction,nAgentAttLower,nAgentAttUpper,nResAttLower,nResAttUpper,
                nActionAttLower,nActionAttUpper,nActionsLower,nActionsUpper,null);

        Date date2=new Date();
//        System.out.println("Time for graph generation: "+(date2.getTime()-date.getTime()));

            // 4.b. generate random start and target vertex
        Random random=new Random(1,graph.getNumberOfVertices());
        int startVertexNo=random.generateRandomIndex();
        int targetVertexNo=random.generateRandomIndex();

            // 4.c. generate varList_v_cur

        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur =
                generateVarListForExperiment(graph,startVertexNo,targetVertexNo);
        Date date3=new Date();
//        System.out.println("Time for varList generation (shortest path computation): "+(date3.getTime()-date2.getTime()));



        Vertex v_cur=null;
        List<Vertex> vertices = graph.getVertices();
        for(int i=0;i<vertices.size();i++)
        {
            if(vertices.get(i).getVertexID()==startVertexNo)
            {
                v_cur=vertices.get(i);
                break;
            }
        }


        VirtualAccessRequest[] vars = executionOfCPAC.extractVars(varList_v_cur,v_cur); // vars[] hold var_v_cur and var_v_next
        if(vars[0]!=null)
        {
            // 4.d. find v_next from varList_v
            Vertex v_next=executionOfCPAC.findVarNextOnShortestPath(v_cur,varList_v_cur);

            // 4.e. Generate an sod policy with multiple rules
            /*
            Note that for this experiment, sod policy needs to be generated after varList_v_cur is generated (look to the
            explanation of generateSoDPolicyForExperiment() method for further explanation).
             */
            SODPolicy sodPolicy=generateSoDPolicyForExperiment(nSoDRules,riskLowLimit,riskUpperLimit,vars[0]);
            // 4.f. Add sod policy to repository
            sodBase.addSODPolicy(sodPolicy);

            // 4.g. Generate authorization policy w.r.t. given sod
            Policy policy=generatePolicyForExperiment(sodPolicy,operationalMode);
            policyBase.addPolicy(policy);

            // 4.h. call executeCPAC() method

            List<Agent> agentList = executionOfCPAC.executeCPAC(null, varList_v_cur, operationalMode, v_cur,v_next,vars);
            return agentList.size();
        }
        else
        {
            return -1;
        }

    }

    /**
     * This method calculates the time taken by a single run of executeCPAC() method. Note that this method is called inside
     * experimentTimePerformance() method and consumes already generated policy elements, policies and graph/varList by it.
     * So, if we need to change the size of the graph/ policy element sets/ policies, we need to change the parameters of
     * the experimentTimePerformance() method.
     *
     * @param executionOfCPAC should be generated inside experimentTimePerformance() method using the generated agentBase,
     *                        resourceBase, actionBase, policyBase, and sodBase
     * @param varList_v_cur should be generated inside experimentTimePerformance() method
     * @param operationalMode
     * @param v_cur should be generated inside experimentTimePerformance() method
     * @param v_next should be generated inside experimentTimePerformance() method
     * @return time taken to run the executeCPAC() method for one time
     * @throws Exception
     */
    private static long solveForTimePerformance(ExecutionOfCPAC executionOfCPAC,
                                                Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur,
                                                String operationalMode,
                                                Vertex v_cur,
                                                Vertex v_next,
                                                VirtualAccessRequest[] vars) throws Exception
    {

        long startTime=System.nanoTime();
        List<Agent> agentList = executionOfCPAC.executeCPAC(null, varList_v_cur, operationalMode, v_cur,v_next,vars);
        long endTime=System.nanoTime();

        long timeDiff = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        return timeDiff;
    }

    /*
        This method is the same as the solveForTimePerformance() method, except that it considers MER improvement
        for time cost. So, it uses the modified version of "executeCPAC()" algorithm.
     */
    private static long solveForTimePerformanceImproved(ExecutionOfCPAC_MERVersion executionOfCPAC,
                                                Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_cur,
                                                String operationalMode,
                                                Vertex v_cur,
                                                Vertex v_next,
                                                        VirtualAccessRequest[] vars) throws Exception
    {

        long startTime=System.nanoTime();
        List<Agent> agentList = executionOfCPAC.executeCPAC(null, varList_v_cur, operationalMode, v_cur,v_next,vars);
        long endTime=System.nanoTime();

        long timeDiff = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        return timeDiff;
    }






    private static void generatePolicyElements(int nResources, int nResAttributes,
                                               int nAgents, int nAgentAttributes,
                                               int nActions) throws Exception {
        // 1. First, generate resource, action, agent, authorization policy, and SoD policy bases.
        resourceBase=new ResourceBase();
        actionBase=new ActionBase();
        agentBase=new AgentBase();

        policyBase=new PolicyBase();
        sodBase=new SODBase();

        // 2. Generate policy elements (resource(s), action(s), agent(s)) and add them to repositories
        Random random=new Random(1,3);
        // 2a. Generate resources
        // generate attributes for the resource
        List<Attribute> resourceAttributes = random.generateAttributes(nResAttributes);
//        List<Attribute> resourceAttributes = generateAttributes(nResAttributes);
        for(int i=0;i<nResources;i++)
        {
//            Resource resource=new Resource("patient123","human");
            String resourceType = random.generateResourceOrAgentType(random);
            Resource resource = new Resource("resource" + (i + 1), resourceType);

            // add resource attributes to the resource
            for (Attribute attribute:resourceAttributes)
            {
                resource.addResourceAttribute(attribute);
            }
            // add resource to the resource base
            resourceBase.addResource(resource);
        }


        // 2b. Generate agents
        // generate attributes for the agent
        List<Attribute> agentAttributes = random.generateAttributes(nAgentAttributes);
//        List<Attribute> agentAttributes = generateAttributes(nAgentAttributes);
        for(int i=0;i<nAgents;i++)
        {
//            Agent agent=new Agent("heparinDevice1","physical");
            String agentType = random.generateResourceOrAgentType(random);
            Agent agent=new Agent("agent"+(i+1),agentType);

            // add agent attributes to the agent
            for (Attribute attribute:agentAttributes)
            {
                agent.addAgentAttribute(attribute);
            }
            // add agent to the agent base
            agentBase.addAgent(agent);
        }

        // 2c. Generate actions
        for(int i=0;i<nActions;i++)
        {
            Action action=new Action("action"+(i+1),random.generateActionType());
            actionBase.addAction(action);
        }
    }



    private static Policy generatePolicy(int nPolicyRules,
                                         String operationalMode) throws Exception {
        // 2c. Generate policy rules and add it to a policy
        List<PolicyRule> rules=new LinkedList<>();

        for (int i=0;i<nPolicyRules;i++)
        {
            // 2c.1. generate resource attributes with their matching functions to be used in the policy rule
            // (Map attributes to their attribute matching functions for XACML evaluation)
            Resource resource = resourceBase.getResourceList().get(0);// => since I added all resources with the same set of atts, I just use the first one
            List<Attribute> resourceAttributes = resource.getResourceAttributes();
            List<Map<Attribute, String>> resourceAttsForPolicyRule =
                    generateAttributesForPolicyRuleForAuthorizedAgents(resourceAttributes);
            Map<String, List<Map<Attribute, String>>> resourceAttMatchFuncListMap=new HashMap<>();
            resourceAttMatchFuncListMap.put("resource group1",resourceAttsForPolicyRule); // this is a <Resource></Resource> group in XACML

            // 2c.2. generate agent attributes with their matching functions to be used in the policy rule
            // (Map attributes to their attribute matching functions for XACML evaluation)
            Agent agent = agentBase.getAgentList().get(0);// => since I added all resources with the same set of atts, I just use the first one
            List<Attribute> agentAttributes = agent.getAgentAttributes();
            List<Map<Attribute, String>> agentAttsForPolicyRule =
                    generateAttributesForPolicyRuleForAuthorizedAgents(agentAttributes);
            Map<String, List<Map<Attribute, String>>> agentAttMatchFuncListMap=new HashMap<>();
            agentAttMatchFuncListMap.put("agent group1",agentAttsForPolicyRule);

            // 2c.3. generate environment attributes to be used in the policy rule
            // will be null for our sample CPAC policy

            // 2c.4. generate action attributes with their matching functions to be used in the policy rule
            // (Map attributes to their attribute matching functions for XACML evaluation)
            Action action = actionBase.getActionList().get(0);// => since I added all resources with the same set of atts, I just use the first one
            List<Attribute> actionAttributes = action.getActionAttributes();
            List<Map<Attribute, String>> actionAttsForPolicyRule =
                    generateAttributesForPolicyRuleForAuthorizedAgents(actionAttributes);
            Map<String, List<Map<Attribute, String>>> actionAttMatchFuncListMap=new HashMap<>();
            actionAttMatchFuncListMap.put("action group 1",actionAttsForPolicyRule);

            // Note that policy effect should start with uppercase letter, otherwise WSO2 identity server throws an error for invalid policy.

            /**
             *  If we want the "ruleEffect" to be randomly generated, we will comment in the following lines
             *  (initially I implemented such that rule effects are randomly generated. Yet, this caused some problems.
             *  More precisely, it is possible that all generated policy rules has "deny" type of ruleEffect. This causes
             *  "computeValidSetForAgent()" method in "executeCPAC()" method to return an empty set of permissions.
             *  This is completely OK in normal cases, but for experiments, I would like to increase the chance of violating
             *  sod so that I can execute lines 27 and 28 of the "executeCPAC()" method.
             */

//            Random random=new Random();
//            boolean isPermit = random.getRandomBoolean();
//            String ruleEffect=null;
//            if(isPermit)
//            {
//                ruleEffect="Permit";
//            }
//            else
//            {
//                ruleEffect="Deny";
//            }
            String ruleEffect="Permit";
            PolicyRule policyRule=new PolicyRule(resourceAttMatchFuncListMap,agentAttMatchFuncListMap,
                    actionAttMatchFuncListMap,null,
                    operationalMode,"sample-CPAC-policy-rule"+(i+1),ruleEffect,
                    resourceBase,actionBase);

            rules.add(policyRule);
        }

        Policy policy=new Policy("sample-CPAC-policy",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit",
                rules);

        return policy;
    }


    /**
     * The method below generates policies for "risk experiment" based on the sod policies generated. The reason to do so
     * is to obtain a higher number of unsafe permissions than "k" values of sod policy rules so that lines 25-27 of the
     * executeCPAC algorithm is executed.
     *
     * Note: We don't need to give the number of policy rules as a parameter since this method decides the number of rules based
     * on the given sod policy.
     *
     * @param operationalMode
     * @return
     * @throws Exception
     */
    private static Policy generatePolicyForExperiment(SODPolicy sodPolicy, String operationalMode) throws Exception {
        // 2c. Generate policy rules and add it to a policy
        List<PolicyRule> rules=new LinkedList<>();

//        for (int i=0;i<nPolicyRules;i++)
//        {
            // 2c.1. generate resource attributes with their matching functions to be used in the policy rule
            // (Map attributes to their attribute matching functions for XACML evaluation)
            Resource resource = resourceBase.getResourceList().get(0);// => since I added all resources with the same set of atts, I just use the first one
            List<Attribute> resourceAttributes = resource.getResourceAttributes();
            List<Map<Attribute, String>> resourceAttsForPolicyRule =
                    generateAttributesForPolicyRuleForAuthorizedAgents(resourceAttributes);
            Map<String, List<Map<Attribute, String>>> resourceAttMatchFuncListMap=new HashMap<>();
            resourceAttMatchFuncListMap.put("resource group1",resourceAttsForPolicyRule); // this is a <Resource></Resource> group in XACML

            // 2c.2. generate agent attributes with their matching functions to be used in the policy rule
            // (Map attributes to their attribute matching functions for XACML evaluation)
            Agent agent = agentBase.getAgentList().get(0);// => since I added all resources with the same set of atts, I just use the first one
            List<Attribute> agentAttributes = agent.getAgentAttributes();
            List<Map<Attribute, String>> agentAttsForPolicyRule =
                    generateAttributesForPolicyRuleForAuthorizedAgents(agentAttributes);
            Map<String, List<Map<Attribute, String>>> agentAttMatchFuncListMap=new HashMap<>();
            agentAttMatchFuncListMap.put("agent group1",agentAttsForPolicyRule);

            // 2c.3. generate action attributes with their matching functions to be used in the policy rule
            // (Map attributes to their attribute matching functions for XACML evaluation)

                // select actions based on given sod rules
            List<SODPolicyRule> sodPolicyRules=sodPolicy.getSodPolicyRules();

            for (SODPolicyRule sodRule:sodPolicyRules)
            {
                List<Permission> permissions=sodRule.getPerm();
                int ruleCounter=0;
                for (Permission perm:permissions)
                {
                    ruleCounter++;
                    List<Action> actionsOfPerm=perm.getActionList();

                    Map<String, List<Map<Attribute, String>>> actionAttMatchFuncListMap=new HashMap<>();
                    int actionGroupCounter=0;
                    for (Action action:actionsOfPerm)
                    {
                        actionGroupCounter++;
                        List<Attribute> actionAttributes=action.getActionAttributes();
                        List<Map<Attribute, String>> actionAttsForPolicyRule =
                                generateAttributesForPolicyRuleForAuthorizedAgents(actionAttributes);
                        actionAttMatchFuncListMap.put("action group "+(actionGroupCounter),actionAttsForPolicyRule);
                    }
                    String ruleEffect="Permit";
                    PolicyRule policyRule=new PolicyRule(resourceAttMatchFuncListMap,agentAttMatchFuncListMap,
                            actionAttMatchFuncListMap,null,
                            operationalMode,"sample-CPAC-policy-rule"+ruleCounter,ruleEffect,
                            resourceBase,actionBase);

                    rules.add(policyRule);
                }

            }
//        }

        Policy policy=new Policy("sample-CPAC-policy",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit",
                rules);

        return policy;
    }


    private static List<Map<Attribute,String>> generateAttributesForPolicyRule(List<Attribute> attributes) throws Exception {
        List<Map<Attribute,String>> attributeListToReturn=new LinkedList<>();
        Random random=new Random();
        // I assume that I use all the attributes I generated for resourceBase/agentBase for generating a policy rule
        for (Attribute attribute:attributes)
        {
            String attName=attribute.getAttributeName();
            // process attributes that are not defined in CPAC specifications, i.e. the ones that are synthetically generated
            // for the experiments
            if(!CPACSpecifications.resourceAttributes.contains(attName)
                    && !CPACSpecifications.agentAttributes.contains(attName)
                    &&!CPACSpecifications.actionAttributes.contains(attName))
            {
                String attributeType = attribute.getAttributeType();
                Attribute attributeToAdd = random.generateAttributeValue(attribute);
                Map<Attribute,String> attributeToMatchingFunctionMap=new HashMap<>();
                String matchingFunction=null;

                boolean isCategorical=attributeType.equalsIgnoreCase("categorical")?true:false;

                if(isCategorical)
                {
                    matchingFunction="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case";
                }
                else
                {
                    List<String> doubleMatchFunctions = XACMLSpecifications.DOUBLE_MATCH_FUNCTIONS;
                    int upperLimit = doubleMatchFunctions.size();
                    Random random2=new Random(1,upperLimit);
                    int index = random2.generateRandomIndex();
                    matchingFunction=doubleMatchFunctions.get(index-1);
//                matchingFunction="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case";
                }

                attributeToMatchingFunctionMap.put(attributeToAdd,matchingFunction);
                attributeListToReturn.add(attributeToMatchingFunctionMap);
            }
        }

        return attributeListToReturn;
    }

    /**
     * The difference of this method from generateAttributesForPolicyRule() is that it generates attributes for policy rules
     * such that existing agents in the agentBase will be authorized with privileges inside policy rules. In other words,
     * values of attributes in policy rules exactly match with the values of existing agent/resource/action attributes.
     * @param attributes
     * @return
     * @throws Exception
     */

    private static List<Map<Attribute,String>> generateAttributesForPolicyRuleForAuthorizedAgents(List<Attribute> attributes) throws Exception {
        List<Map<Attribute,String>> attributeListToReturn=new LinkedList<>();
        Random random=new Random();
        // I assume that I use all the attributes I generated for resourceBase/agentBase for generating a policy rule
        for (Attribute attribute:attributes)
        {
            String attName=attribute.getAttributeName();
            // process attributes that are not defined in CPAC specifications, i.e. the ones that are synthetically generated
            // for the experiments (except for actions, because if we do not include actionIDs, "actionAttributeMap" field
            // of a policy rule will be empty, and this will throw a null pointer exception while getting permissions from a
            // policy rule in getPermissionsFromRule() method that is called inside computeValidSetForAgent() method of
            // executeCPAC algorithm.

            if(!CPACSpecifications.resourceAttributes.contains(attName)
                    && !CPACSpecifications.agentAttributes.contains(attName))
//                    &&!CPACSpecifications.actionAttributes.contains(attName))
            {
                String attributeType = attribute.getAttributeType();
                Map<Attribute,String> attributeToMatchingFunctionMap=new HashMap<>();
                String matchingFunction=null;

                boolean isCategorical=attributeType.equalsIgnoreCase("categorical")?true:false;

                if(isCategorical)
                {
                    matchingFunction="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case";
                }
                else
                {
                matchingFunction="urn:oasis:names:tc:xacml:1.0:function:double-equal";
                }

                attributeToMatchingFunctionMap.put(attribute,matchingFunction);
                attributeListToReturn.add(attributeToMatchingFunctionMap);
            }
        }

        return attributeListToReturn;
    }

    private static SODPolicy generateSoDPolicy(int nSoDRules,
                                               int maxNPermissions,
                                               int lowerRiskLimit,
                                               int upperRiskLimit)
    {
        // 2c. Generate SoD rules and add it to an SoD policy
        /*
                sample sod from the paper:
                sod1=<{perm1,perm2},2>
                perm1=<  infuseDrug1,patientX >
                perm2=< infuseDrug2,patientX >
         */
        SODPolicy sodPolicy=new SODPolicy();
        List<Resource> resourceList = resourceBase.getResourceList();


        for(int i=0; i<nSoDRules;i++)
        {
            // each SoD rule will consist of a random number of permissions between 2 and max number of permissions parameter
                // select number of permissions for the SoD rule:
            Random random=new Random(2,maxNPermissions);
            int nPermissions = random.generateRandomIndex();

            List<Permission> permissionList=new LinkedList<>(); // permission list for the SoD rule

            for(int j=0;j<nPermissions;j++)
            {
                // select a  random resource
                int upperLimit = resourceList.size();
                Random random2=new Random(1,upperLimit);
                int resourceIdx = random2.generateRandomIndex();
                Resource resource = resourceList.get(resourceIdx-1);

                // select a random subset of actions
                    // select a random number of actions for the permission:
                List<Action> actions = actionBase.getActionList();
                int upperLimit2=actions.size();
                Random random3=new Random(1,upperLimit2);
                int nActions=random3.generateRandomIndex();

                List<Action> actionsListForPermission=new LinkedList<>();
                while (actionsListForPermission.size()<nActions)
                {
                    int index = random3.generateRandomIndex();
                    Action action = actions.get(index-1);
                    if(!actionsListForPermission.contains(action))
                    {
                        actionsListForPermission.add(action);
                    }
                }

                Permission permission=new Permission(resource,actionsListForPermission);
                permissionList.add(permission);

            }

            // generate random "k" for the sod rule
            Random random4=new Random(2,nPermissions);
            int k = random4.generateRandomIndex();

            // generate a random risk value for the SoD rule
            double risk = random4.doubleRandomInclusive(lowerRiskLimit, upperRiskLimit);
            SODPolicyRule sodPolicyRule=new SODPolicyRule(permissionList,k,risk,policyBase,agentBase);
            sodPolicy.addSoDPolicyRule(sodPolicyRule);
        }
        return sodPolicy;
    }



    /*
    The following method is for "risk experiments", where we experiment the effect of risk of sod policies and v_cur/v_next.
    Hence, we want the "var_v_cur" and its corresponding "perm_var" to return a non-empty sod policies (done by
    checkApplicableSodPolicies(perm_var) method in executeCPAC() method). For this purpose, the below method generates sod
    policy rules that correspond to a given "var_v_cur".
     */
    private static SODPolicy generateSoDPolicyForExperiment(int nSoDRules,
                                                            int lowerRiskLimit,
                                                            int upperRiskLimit,
                                                            VirtualAccessRequest var_v_cur ) throws Exception {
        // 2c. Generate SoD rules and add it to an SoD policy
        /*
                sample sod from the paper:
                sod1=<{perm1,perm2},2>
                perm1=<  infuseDrug1,patientX >
                perm2=< infuseDrug2,patientX >
         */
        SODPolicy sodPolicy=new SODPolicy();

        Permission varPermission = var_v_cur.extractPermission(resourceBase);
//        Permission varPermission = var_v_cur.getPermission();

        // use resource from the var_v_cur
        Resource varPermissionResource = varPermission.getResource();
        List<Action> varPermissionActionList = varPermission.getActionList();

        List<Action> candidateActions=new LinkedList<>();
        candidateActions.addAll(actionBase.getActionList());
        candidateActions.addAll(varPermissionActionList);
        Collections.shuffle(candidateActions);

        for(int i=0; i<nSoDRules;i++)
        {
            /*
                == Select number of permissions for the SoD rule ==
                Each SoD rule will consist of a random number of permissions between 2 and max number of permissions parameter.

                Note that max number of permissions can be equal to the size of the action set in the actionBase, because
                in case each permission has single action, each action can be a unique element of possible allowed actions in
                the system.

                Also note that permissions are mutually exclusive, meaning that action set of each permission has to have no
                overlapping action with the action set of any other permission in a given sod policy rule.
             */
            //
            int maxNActions=actionBase.getActionList().size(); // maxNActions= max number of permissions for an sod rule
            Random random=new Random(2,maxNActions);
            int nPermissions = random.generateRandomIndex();

            List<Permission> permissionList=new LinkedList<>(); // permission list for the SoD rule

            int totalActionsUsed=0;
            List<Action> actionsUsed=new ArrayList<>();
            for(int j=0;j<nPermissions;j++)
            {
                /*
                select a random subset of actions. Make sure that at least one action from the var_v_cur is included
                so that checkApplicableSodPolicies(perm_var) method in executeCPAC() method returns a non-empty set.
                 */

                // select a random number of actions:
                int upperLimit=(int) Math.ceil((maxNActions-totalActionsUsed)*1.0/(nPermissions-j));
                random=new Random(1,upperLimit);
                int nActions=random.generateRandomIndex();
                totalActionsUsed+=nActions;

                upperLimit=candidateActions.size();
                random=new Random(1,upperLimit);

                List<Action> actionsListForPermission=new LinkedList<>();
                while (actionsListForPermission.size()<nActions)
                {
                    int index = random.generateRandomIndex();
                    Action action = candidateActions.get(index-1);
                    if(!actionsUsed.contains(action))
                    {
                        actionsListForPermission.add(action);
                        actionsUsed.add(action);
                    }
                }

                Permission permission=new Permission(varPermissionResource,actionsListForPermission);
//                if(!permissionList.contains(permission))
//                {
                    permissionList.add(permission);
//                }
            }

            // generate random "k" for the sod rule
            Random random4=new Random(2,nPermissions);
            int k = random4.generateRandomIndex();

            // generate a random risk value for the SoD rule
            double risk = random4.doubleRandomInclusive(lowerRiskLimit, upperRiskLimit);
            SODPolicyRule sodPolicyRule=new SODPolicyRule(permissionList,k,risk,policyBase,agentBase);
            sodPolicy.addSoDPolicyRule(sodPolicyRule);
        }
        return sodPolicy;
    }

    private static Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> generateVarListForExperiment
            (Graph graph,int startVertexNo,int targetVertexNo)
    {
        int[] finalStates=graph.getFinalStates();
//        VARGeneration varGeneration =new VARGeneration(graph,startVertexNo,targetVertexNo,finalStates);
        VARGenerationMultipleShortestPathVersion varGenerationMultipleShortestPathVersion=
                new VARGenerationMultipleShortestPathVersion(graph,startVertexNo,targetVertexNo,finalStates);

        Set<Graph> subGraphSet=new HashSet<Graph>();
        subGraphSet.add(graph);

//        Map<String, Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>>> varSet =
//                varGeneration.generateVAR(subGraphSet);

        Map<String, Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>>> varSet =
                varGenerationMultipleShortestPathVersion.generateVAR(subGraphSet);

        Queue<Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>>> varSet_m
                = varSet.get(graph.getOperationalMode());

        Vertex startVertex=new Vertex(startVertexNo);
        Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>> varList_v_start=new LinkedList<>();

        while(!varSet_m.isEmpty())
        {
            Map<Vertex, Queue<Map<Vertex, Queue<Map<Edge, VirtualAccessRequest>>>>> vertexQueueMap = varSet_m.poll();
            Iterator<Vertex> iterator = vertexQueueMap.keySet().iterator();

            while (iterator.hasNext())
            {
                Vertex sourceVertex = iterator.next();
                if(sourceVertex.equals(startVertex))
                {
                    varList_v_start = vertexQueueMap.get(sourceVertex);
                }
            }
        }

        return varList_v_start;
    }

    /**
     *
     * @param graphType whether it is a "complete" or "sparse" graph
     * @param numberOfVertices number of vertices in a graph
     * @param riskLower The lower limit to generate the risk of a vertex. There is no limit for this parameter.
     * @param riskUpper The upper limit to generate the risk of a vertex. There is no limit for this parameter.
     * @param fraction The proportion of critical and action edges. It should be between 0.0 and 1.0
     * @param nAgentAttLower MINIMUM number of AGENT attributes to include in virtual access requests assigned to edges
     * @param nAgentAttUpper MAXIMUM number of AGENT attributes to include in virtual access requests assigned to edges
     * @param nResAttLower MINIMUM number of RESOURCE attributes to include in virtual access requests assigned to edges
     * @param nResAttUpper MAXIMUM number of RESOURCE attributes to include in virtual access requests assigned to edges
     * @param nActionAttLower MINIMUM number of ACTION attributes to include in virtual access requests assigned to edges
     * @param nActionAttUpper MAXIMUM number of ACTION attributes to include in virtual access requests assigned to edges
     * @param nActionsLower MINIMUM number of ACTIONS to include in virtual access requests assigned to edges
     * @param nActionsUpper MAXIMUM number of ACTIONS to include in virtual access requests assigned to edges
     * @param finalStates
     * @return
     * @throws Exception
     */
    private static Graph generateRandomGraph(String graphType, String operationalMode,
                                             int numberOfVertices,
                                             int riskLower, int riskUpper,
                                             double fraction,
                                             int nAgentAttLower,int nAgentAttUpper,
                                             int nResAttLower,int nResAttUpper,
                                             int nActionAttLower,int nActionAttUpper,
                                             int nActionsLower, int nActionsUpper,
                                             int[] finalStates) throws Exception {
        int[][] adjacencyMatrix = new int[0][];
        double[] riskValues= new double[0];
        String[][] edgeTypeMatrix=new String[0][];
        VirtualAccessRequest[][] varMatrix= new VirtualAccessRequest[0][];
        Sparse sparse;
        CompleteGraph complete;

        int weightLower=0, weightUpper=100;
        if(graphType.equalsIgnoreCase("complete"))
        {
            complete=new CompleteGraph(numberOfVertices,weightLower,weightUpper,riskLower,riskUpper,fraction);
            adjacencyMatrix=complete.generateAdjacencyMatrix();
            riskValues=complete.generateRiskValues();
            edgeTypeMatrix=complete.generateEdgeTypeMatrix();
//            varMatrix=complete.generateVarMatrix(nAgentAttLower,nAgentAttUpper,nResAttLower,nResAttUpper,
//                                                nActionAttLower,nActionAttUpper,nActionsLower,nActionsUpper,
//                                                actionBase,operationalMode);
            varMatrix=complete.generateVarMatrixForExperiment(resourceBase,agentBase,actionBase,
                                                                nAgentAttLower,nAgentAttUpper,
                                                                nResAttLower,nResAttUpper,
                                                                nActionAttLower,nActionAttUpper,
                                                                nActionsLower,nActionsUpper,
                                                                operationalMode);

        }
        else if (graphType.equalsIgnoreCase("sparse"))
        {
            sparse=new Sparse(numberOfVertices,weightLower,weightUpper,riskLower,riskUpper,fraction);
            adjacencyMatrix=sparse.generateAdjacencyMatrix();
            riskValues=sparse.generateRiskValues();
            edgeTypeMatrix=sparse.generateEdgeTypeMatrix();
//            varMatrix=sparse.generateVarMatrix(nAgentAttLower,nAgentAttUpper,nResAttLower,nResAttUpper,
//                    nActionAttLower,nActionAttUpper,nActionsLower,nActionsUpper,
//                    actionBase,operationalMode);

//            varMatrix=sparse.generateVarMatrixForExperiment(resourceBase,agentBase,actionBase,
//                                                            nAgentAttLower,nAgentAttUpper,
//                                                            nResAttLower,nResAttUpper,
//                                                            nActionAttLower,nActionAttUpper,
//                                                            nActionsLower,nActionsUpper,
//                                                            operationalMode);

            varMatrix=sparse.generateVarMatrixForExperiment(resourceBase,agentBase,actionBase, operationalMode);
        }

        Graph graph=new Graph(adjacencyMatrix,riskValues,edgeTypeMatrix,varMatrix,finalStates,operationalMode);

        return graph;
    }

}
