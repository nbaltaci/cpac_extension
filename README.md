# cpac_extension
This repository is for the implementation of the GAGM module and CPAC Execution algorithm for the following paper:

Baltaci Akhuseyinoglu N, Joshi J.  Separation of Duty Extension to Attribute Based Access Control Approach for Cyber-Physical Systems. (to be submitted to Elsevier Computer & Security journal) (tentative title)

Note:
1) Before building the project, "WSO2APICall.jar" should be added as a library to the project as "ExecutionOfCPAC" class makes use of the methods implemented in it
for authorization decision.
2) This jar should be added after files with ".RSA" and ".SF" extensions are removed from the jar. Otherwise, "invalid signature file" error will be thrown since the jar itself makes use of some signed jars. In order to remove signed files, the following command should be executed within the directory where "WSO2APICall.jar"
resides in:
        zip -d WSO2APICall.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*SF'
