package com.nuray.cpacexecution.cpacmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CPACSpecifications {

    public static final List<String> elementTypes=new ArrayList<>(Arrays.asList(new String[]{"human","physical","cyber"}));
    public static final List<String> actionTypes=new ArrayList<>(Arrays.asList(new String[]{"object-oriented","cyber-physical"}));

    public static final List<String> attributeTypes=new ArrayList<>(Arrays.asList(new String[]{"numeric","categorical","date","time"}));


    public static final List<String> nonEmergencyModes=new ArrayList<>(Arrays.asList(new String[]{"active","passive","autonomous"}));
    public static final List<String> emergencyMode=new ArrayList<>(Arrays.asList(new String[]{"emergency"}));
    //This is MO variable defined in CPAC
    public static final List<String> MO= Stream.concat(nonEmergencyModes.stream(),emergencyMode.stream()).collect(Collectors.toList());

    public static final List<String> agentAttributes=new ArrayList<>(Arrays.asList(new String[]{"agentID","userName","email","role","age"}));
    public static final List<String> resourceAttributes=new ArrayList<>(Arrays.asList(new String[]{"resourceName","resourceID"}));
    public static final List<String> actionAttributes=new ArrayList<>(Arrays.asList(new String[]{"actionName","actionID"}));






}
