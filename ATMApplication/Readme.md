# General Developer Information for API Project
Please update this documentation as you see fit as you are altering the code.

## Prerequisites

* Java JDK 11+
* Apache Maven 3
* Docker
* Mysql (with initialised database called db)

## Running the application.
Requests are submitted using Base64 encoded parameters of the accountid and PIN. These represent the typical username and password combination that utilises the authorization header in conjunction with SSL configuration (see below)

###Docker Container. 
Running ./run-app-inside-docker.sh (testing within WSL2 on windows with dockerForWindows using WSL2) will autoamtically build the application and copy the jar into its own docker container. 
The docker setup instantiates mysql database on the same network, accessible on port 3308 (preventing local conflict). 

###Local setup. 
for setup locally you can have your own database instance setup (update the application.properties accordingly with local user and password)
Run the application with the Spring-boot:run maven command. 


###Sample Requests
Note for Docker urls below its mapped to port 8081. It will run on 8080 locally. 
GET https://localhost:8081/atm/account/1234  user(1234) password(1234)
GET https://localhost:8081/heartbeat NO Auth
PATCH https://localhost:8081/atm/account/1234/withdraw?amount=20 user(1234) password(1234)

##Security

###Security SSL Configuration
The application is configured to run on an environment setup with **HTTPS certification** using a PKCS12 certificate. A certificate has been generated to enable the application to work locally agasint the API. 
For windows (suggested development environment) You can install the local atmapplication.crt on your local machine by right clicking it, and selecting "install certificate" and when given the option choose "trusted Root Certification Authorities" as the Certificate Store. 
You should to restart browser after installing your certificate. 

For certificate installation on postman you can use https://learning.postman.com/docs/sending-requests/certificates/ 
Note that CSRF security is disabled in ATMAppSecurityConfig for local testing. 
###

The code demonstrates using an authentication mechanism to authorize customers accountIds and pin codes to automatically reject invalid requests. 
The API uses Basic Authentication. This is a base64 encoded username and password (AccountId and Pin). The pin is passed in this way so as to be encrypted with the HTTPS security that has been setup. 
The database is stored in the database as a hash of the Pin Code. (currently All Pins entered for testing represent Pin "1234")
Note: The storing of 4 digit PIN codes in the database as hashcode is not considered as secure in the case of dataleaks. Further work should be implemented before release to encrypt the pin with e.g PKCS12 certificate. 


## Testing the application
Tests are split up into two categories. Basic unit tests to cover functionality of each modle. And an integration test to test the complete API. 

##TODO
* add rateLimiting of requests to prevent brute force attacks. 
* Disable account if there are 3 invalid access attempts. 





