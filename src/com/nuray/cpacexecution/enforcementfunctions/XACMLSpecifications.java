package com.nuray.cpacexecution.enforcementfunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XACMLSpecifications {

        /*
        NOTE: following functions, rule combining algorithms etc. are given in XACML specification here:
        http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047239
     */

    public static final String[] STRING_MATCH_FUNCTIONS_ARRAY =new String[]{"urn:oasis:names:tc:xacml:1.0:function:string-equal",
            "urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case",
            "urn:oasis:names:tc:xacml:3.0:function:string-contains",
            "urn:oasis:names:tc:xacml:3.0:function:string-starts-with",
            "urn:oasis:names:tc:xacml:3.0:function:string-ends-with"};
    public static final List<String> STRING_MATCH_FUNCTIONS = new ArrayList<>(Arrays.asList(STRING_MATCH_FUNCTIONS_ARRAY));

    public static final String[] DOUBLE_MATCH_FUNCTIONS_ARRAY =new String[]{"urn:oasis:names:tc:xacml:1.0:function:double-equal",
            "urn:oasis:names:tc:xacml:1.0:function:double-greater-than",
            "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal",
            "urn:oasis:names:tc:xacml:1.0:function:double-less-than",
            "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal"};
    public static final List<String> DOUBLE_MATCH_FUNCTIONS = new ArrayList<>(Arrays.asList(DOUBLE_MATCH_FUNCTIONS_ARRAY));


    public static final String[] DATE_MATCH_FUNCTIONS_ARRAY =new String[]{"urn:oasis:names:tc:xacml:1.0:function:date-equal",
            "urn:oasis:names:tc:xacml:1.0:function:date-greater-than",
            "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
            "urn:oasis:names:tc:xacml:1.0:function:date-less-than",
            "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal"};
    public static final List<String> DATE_MATCH_FUNCTIONS = new ArrayList<>(Arrays.asList(DATE_MATCH_FUNCTIONS_ARRAY));


    public static final String[] TIME_MATCH_FUNCTIONS_ARRAY =new String[]{"urn:oasis:names:tc:xacml:1.0:function:time-equal",
            "urn:oasis:names:tc:xacml:1.0:function:time-greater-than",
            "urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal",
            "urn:oasis:names:tc:xacml:1.0:function:time-less-than",
            "urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal",
            "urn:oasis:names:tc:xacml:2.0:function:time-in-range"};
    public static final List<String> TIME_MATCH_FUNCTIONS = new ArrayList<>(Arrays.asList(TIME_MATCH_FUNCTIONS_ARRAY));

    public static final String[] RULE_COMBINIG_ALGORITHMS_ARRAY= new String[]{
            "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides",
            "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides",
            "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit",
            "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny"};

    public static final List<String> RULE_COMBINIG_ALGORITHMS=new ArrayList<>(Arrays.asList(RULE_COMBINIG_ALGORITHMS_ARRAY));

}
