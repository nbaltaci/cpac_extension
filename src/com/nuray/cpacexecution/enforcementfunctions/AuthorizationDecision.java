package com.nuray.cpacexecution.enforcementfunctions;


import com.nuray.wso2.Test;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.identity.entitlement.stub.*;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;

import java.rmi.RemoteException;

import static com.nuray.wso2.Test.generateEntitlementServiceAdminStub;


public class AuthorizationDecision {

    EntitlementServiceStub ESadminStub;
    EntitlementPolicyAdminServiceStub EPASadminStub;
    RemoteUserStoreManagerServiceStub RUSMSadminStub;
    EntitlementAdminServiceStub EASadminStub;


    /**
     * This function corresponds to auth(PS,aar) predicate in our CPAC enforcement definition.
     * @return
     */
   public String auth(AgentInitiatedAccessRequest aar) throws EntitlementServiceException, RemoteException
   {
       connectServerForAuth();

       String requestContentXACML = aar.getRequestContentXACML();
       String decision = ESadminStub.getDecision(requestContentXACML);

       return decision;
   }

   private void connectServerForAuth()
   {
       Test test=new Test();
       test.getServerCert();

       /*create stubs and service clients*/

       ESadminStub=generateEntitlementServiceAdminStub();
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

}
