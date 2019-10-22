package com.nuray.cpacexecution.enforcementfunctions;


import com.nuray.wso2.Test;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.stub.*;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.identity.entitlement.EntitlementAdminService;
import org.wso2.carbon.identity.entitlement.EntitlementService;



import java.rmi.RemoteException;
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
        test=new Test();
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
//       Test test=new Test();

       test.getServerCert();

       /*create stubs and service clients*/

       ESadminStub=test.generateEntitlementServiceAdminStub();
       ServiceClient ESclient = ESadminStub._getServiceClient();

       EPASadminStub=test.generateEntitlementPolicyAdminServiceAdminStub();
       ServiceClient EPASclient = EPASadminStub._getServiceClient();

       RUSMSadminStub = test.generateRemoteUserStoreManagerServiceStub();
       ServiceClient RUSMSclient=RUSMSadminStub._getServiceClient();

       EASadminStub=test.generateEntitlementAdminServiceStub();
       ServiceClient EASclient=EASadminStub._getServiceClient();

       /**
        * Setting basic auth headers of the client for authentication to carbon server
        */
       test.setAuthenticationHeaders(ESclient);
       test.setAuthenticationHeaders(EPASclient);
       test.setAuthenticationHeaders(RUSMSclient);
       test.setAuthenticationHeaders(EASclient);
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
            test.EPASadminStub=this.EPASadminStub;
            test.addPolicy(policy.getPolicyContentXACML());
        }
    }

    public String parseDecision(String xmlDecision)
    {
        String decision=test.parseDecision(xmlDecision);

        return decision;
    }

    public void removePolicies(List<Policy> policyList) throws EntitlementException {
        for (Policy policy:policyList)
        {
            test.removePolicy(policy.getPolicyContentXACML());
        }

        clearDecisionCache();
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
