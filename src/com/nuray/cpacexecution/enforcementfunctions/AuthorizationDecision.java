package com.nuray.cpacexecution.enforcementfunctions;


import com.nuray.wso2.Test;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.stub.*;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.identity.entitlement.EntitlementAdminService;
import org.wso2.carbon.identity.entitlement.EntitlementService;


import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.nuray.wso2.Test.*;


public class AuthorizationDecision {

    EntitlementServiceStub ESadminStub;
    EntitlementPolicyAdminServiceStub EPASadminStub;
    RemoteUserStoreManagerServiceStub RUSMSadminStub;
    EntitlementAdminServiceStub EASadminStub;

    EntitlementAdminService entitlementAdminService;
    EntitlementService entitlementService;



    static Test test;

    public AuthorizationDecision()
    {
//        test=new Test();
        connectServerForAuth();
    }


    /**
     * This function corresponds to auth(PS,aar) predicate in our CPAC enforcement definition.
     * @return
     */
   public String auth(AgentInitiatedAccessRequest aar) throws EntitlementServiceException, RemoteException, EntitlementException {

       clearDecisionCache();
       String requestContentXACML = aar.getRequestContentXACML();
       String decision = ESadminStub.getDecision(requestContentXACML);
//       String decision = EASadminStub.doTestRequest(requestContentXACML);
       clearDecisionCache();

       return decision;
   }

   private void connectServerForAuth()
   {
//       test.getServerCert();
       getServerCert();

       /*create stubs and service clients*/

//       ESadminStub=test.generateEntitlementServiceAdminStub();
       ESadminStub=generateEntitlementServiceAdminStub();
       ServiceClient ESclient = ESadminStub._getServiceClient();

//       EPASadminStub=test.generateEntitlementPolicyAdminServiceAdminStub();
       EPASadminStub=generateEntitlementPolicyAdminServiceAdminStub();
       ServiceClient EPASclient = EPASadminStub._getServiceClient();

//       RUSMSadminStub = test.generateRemoteUserStoreManagerServiceStub();
       RUSMSadminStub = generateRemoteUserStoreManagerServiceStub();
       ServiceClient RUSMSclient=RUSMSadminStub._getServiceClient();

//       EASadminStub=test.generateEntitlementAdminServiceStub();
       EASadminStub=generateEntitlementAdminServiceStub();
       ServiceClient EASclient=EASadminStub._getServiceClient();

       /**
        * Setting basic auth headers of the client for authentication to carbon server
        */
//       test.setAuthenticationHeaders(ESclient);
//       test.setAuthenticationHeaders(EPASclient);
//       test.setAuthenticationHeaders(RUSMSclient);
//       test.setAuthenticationHeaders(EASclient);

       setAuthenticationHeaders(ESclient);
       setAuthenticationHeaders(EPASclient);
       setAuthenticationHeaders(RUSMSclient);
       setAuthenticationHeaders(EASclient);
   }

   private void getServerCert()
   {
       /**
        * trust store path.  this must contain server's  certificate or Server's CA chain
        */

       String trustStore = System.getProperty("user.dir") + File.separator +
               "src" + File.separator + "main" + File.separator +
               "resources" + File.separator + "wso2carbon.jks";

       trustStore="/Users/nrybltc/Desktop/wso2/wso2is-5.4.0/repository/resources/security/wso2carbon.jks";

        /*
        10/16/2019: I noticed that trustStore for server certificate has been changed in later versions
         of the WSO2 identity server (from here: https://docs.wso2.com/display/IS580/Calling+Admin+Services).
         So, I needed to change the value of "trustStore" variable below.
         */
//       trustStore="/Users/nrybltc/Desktop/wso2/5.9.0/repository/resources/security/wso2carbon.jks";

       System.setProperty("javax.net.ssl.trustStore", trustStore);

       System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
   }
    public EntitlementServiceStub generateEntitlementServiceAdminStub()
    {
        ConfigurationContext configContext=generateAxis2ConfigContext();

        EntitlementServiceStub adminStub=null;
        try {
            SERVICE_ENDPOINT=SERVER_URL + "EntitlementService";
            adminStub=new EntitlementServiceStub(configContext,SERVICE_ENDPOINT);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return adminStub;
    }

    public EntitlementPolicyAdminServiceStub generateEntitlementPolicyAdminServiceAdminStub()
    {
        ConfigurationContext configContext=generateAxis2ConfigContext();

        EntitlementPolicyAdminServiceStub adminStub=null;
        try {
            SERVICE_ENDPOINT=SERVER_URL + "EntitlementPolicyAdminService";
            adminStub=new EntitlementPolicyAdminServiceStub(configContext,SERVICE_ENDPOINT);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return adminStub;
    }

    public RemoteUserStoreManagerServiceStub generateRemoteUserStoreManagerServiceStub()
    {

        ConfigurationContext configContext=generateAxis2ConfigContext();

        RemoteUserStoreManagerServiceStub adminStub=null;
        try {
            SERVICE_ENDPOINT=SERVER_URL + "RemoteUserStoreManagerService";
            adminStub=new RemoteUserStoreManagerServiceStub(configContext,SERVICE_ENDPOINT);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return adminStub;
    }

    public EntitlementAdminServiceStub generateEntitlementAdminServiceStub()
    {

        ConfigurationContext configContext=generateAxis2ConfigContext();

        EntitlementAdminServiceStub adminStub=null;
        try {
            SERVICE_ENDPOINT=SERVER_URL + "EntitlementAdminService";
            adminStub=new EntitlementAdminServiceStub(configContext,SERVICE_ENDPOINT);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return adminStub;
    }

    public void setAuthenticationHeaders(ServiceClient client)
    {
        Options option = client.getOptions();

        /**
         * Setting an authenticated cookie that is received from Carbon server.
         * If you have authenticated with Carbon server earlier, you can use that cookie, if
         * it has not been expired
         */
        option.setProperty(HTTPConstants.COOKIE_STRING, null);

        /**
         * Setting basic auth headers for authentication for carbon server
         */
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(SERVER_USER_NAME);
        auth.setPassword(SERVER_PASSWORD);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);
    }

    public ConfigurationContext generateAxis2ConfigContext()
    {
        /*
            Axis2 configuration context
        */

        ConfigurationContext configContext = null;

        /**
         * Create a configuration context. A configuration context contains information for
         * axis2 environment. This is needed to create an axis2 service client
         */
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return configContext;
    }




    public EntitlementPolicyAdminServiceStub getEPASadminStub()
    {
        return EPASadminStub;
    }

    public EntitlementAdminServiceStub getEASadminStub() {
        return EASadminStub;
    }

    public void publishPolicies(List<Policy> policyList) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException, EntitlementServiceException {
        //publish these policies to the PAP

        for (Policy policy:policyList)
        {
//            test.EPASadminStub=this.EPASadminStub;
//            test.addPolicy(policy.getPolicyContentXACML());
            addPolicy(policy.getPolicyContentXACML());
        }
    }

    private void addPolicy(String policy) throws EntitlementServiceException, RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        String[] allPolicyIds = EPASadminStub.getAllPolicyIds("*");
        List<String> policyIdList = Arrays.asList(allPolicyIds);
        String realPolicyId = getRealPolicyIdFromPolicyContent(policy);
        PolicyDTO policyToPublish = new PolicyDTO();
        policyToPublish.setPolicyId(realPolicyId);
        policyToPublish.setPolicy(policy);
        policyToPublish.setActive(true);
        policyToPublish.setPromote(true);
        if (!containsPolicy(policyIdList, realPolicyId)) {
            try {
                EPASadminStub.addPolicy(policyToPublish);

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException var6) {
                    var6.printStackTrace();
                }
            } catch (EntitlementPolicyAdminServiceEntitlementException var7) {
                var7.printStackTrace();
            }
        }

    }

    private String getRealPolicyIdFromPolicyContent(String policy) {
        String policyIDSearchString = "PolicyId=\"";
        int idx_realPolicyIdStart = policy.indexOf(policyIDSearchString) + policyIDSearchString.length();
        policyIDSearchString = "\" RuleCombiningAlgId";
        int idx_realPolicyIdEnd = policy.indexOf(policyIDSearchString);
        String realPolicyId = policy.substring(idx_realPolicyIdStart, idx_realPolicyIdEnd);
        return realPolicyId;
    }

    private boolean containsPolicy(List<String> list, String searchString) {
        Iterator var2 = list.iterator();

        String str;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            str = (String)var2.next();
        } while(!str.contains(searchString));

        return true;
    }

    public String parseDecision(String xmlDecision)
    {
//        String decision=test.parseDecision(xmlDecision);
        String decision=parseDecisionUtility(xmlDecision);

        return decision;
    }

    public String parseDecisionUtility(String xmlDecision) {
        int indexStart = xmlDecision.indexOf("<Decision>") + "<Decision>".length();
        int indexEnd = xmlDecision.indexOf("</Decision>");
        String decision = xmlDecision.substring(indexStart, indexEnd);
        return decision;
    }

    public void removePolicies(List<Policy> policyList) throws EntitlementException, RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        for (Policy policy:policyList)
        {
//            test.removePolicy(policy.getPolicyContentXACML());
            removePolicy(policy.getPolicyContentXACML());

        }

        clearDecisionCache();
    }

    public void removePolicy(String policy) throws RemoteException, EntitlementPolicyAdminServiceEntitlementException {
        String policyId = getRealPolicyIdFromPolicyContent(policy);

            EPASadminStub.removePolicy(policyId, true);
    }


    public void clearDecisionCache() {
        try {
            EASadminStub.clearDecisionCache();

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (EntitlementAdminServiceIdentityException e) {
            e.printStackTrace();
        }
    }


}
