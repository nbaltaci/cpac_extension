package com.nuray.cpacexecution;//package com.nuray.cpacexecution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static final String[] STRING_MATCH_FUNCTIONS_ARRAY =new String[]{"urn:oasis:names:tc:xacml:1.0:function:string-equal",
            "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case"};
    public static final List<String> STRING_MATCH_FUNCTIONS = new ArrayList<>(Arrays.asList(STRING_MATCH_FUNCTIONS_ARRAY));

    public static void main(String [] args)
    {
//        StringBuilder stringBuilder=new StringBuilder();
//
//        final StringBuilder chars = new StringBuilder();
//
////        STRING_MATCH_FUNCTIONS.forEach(l -> chars.append(l.charAt(0)));
//        STRING_MATCH_FUNCTIONS.forEach(l -> chars.append(l+"\n "));
//
//
//        STRING_MATCH_FUNCTIONS.stream().forEach(stringBuilder::append);
//
//
//
//        System.out.println(chars.toString());

//        int nTrues=0;
//        int nFalses=0;
//
//        for(int i=0;i<1000;i++)
//        {
//            if(getRandomBoolean()==true)
//            {
//                nTrues++;
//
//            }
//            else
//            {
//                nFalses++;
//            }
//        }
//
//        System.out.println("Number of trues: "+nTrues);
//        System.out.println("Number of falses: "+nFalses);

        int randNum= (int) Math.round(Math.random()); 
        System.out.println(randNum);
        if(randNum==1)
        {
            System.out.println("true");
        }
        else
        {
            System.out.println("false");
        }


    }



    public static boolean getRandomBoolean() {
        return Math.random() < 0.9;
        //I tried another approaches here, still the same result
    }


}
