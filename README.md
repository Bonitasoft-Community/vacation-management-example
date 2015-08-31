# Vacation Management Living Application

## About
This is an official Living Application example for Bonita BPM 7.0

This example demonstrates the following concepts:
- Living Application
- Forms, pages and custom widgets built the UI Designer
- Process using BDM and contracts

## Installation

1. Start Bonita BPM Studio 7.0.0 or later
2. Go to `Development` -> `Manage jars...` Studio menu. Click on `Import...` button. Go to `BonitaBPMCommunity-7.0.0/workspace/tomcat/lib/bonita` folder and select the `slf4j-api-1.6.1.jar` library.
3. Download the <a href="https://github.com/Bonitasoft-Community/vacation-management-example/releases">application bundle</a>
4. Extract the bundle to a temporary folder
5. Import the .bos file in your Bonita BPM Studio. It contains 4 processes: NewVacationRequest, CancelVacationRequest, InitiateVacationAvailable and RemoveAllBusinessData. It also includes business data model definition, default process configuration and custom widgets.
6. Open and run the InitiateVacationAvailable process (click on "Run" button in a Bonita BPM Studio). This process will initialize a business data that store vacation available counter for each Bonita BPM user declared in the default test environment (ACME organization).
7. You can now deploy and run the NewVacationRequest and CancelVacationRequest processes by clicking on "Run" button in Bonita BPM Studio. Note that vacation request approval need to be performed by requester (walter.bates) manager (helen.kelly) so you might want to logout and login back with helen.kelly/bpm user. If you want to create (or cancel) several vacation requests, do not click again on "Run" button. Instead click on the "Portal" button and in "User" view go to to "Processes" tab select the process and click on "Start" button.
8. You can now deploy the living application that give user an overview of all created vacation request. In the Portal, switch to "administrator" view (use drop down list on top right corner to switch from "user" to "Administrator"), go to "Resources" and click on "Add" button to upload the `page-myVacationRequests.zip` file (click on "Next" and "Confirm" button). Read more about custom pages on the Bonitasoft <a href="http://documentation.bonitasoft.com/pages">official documentation</a>.
9. In the Portal, go to 'Applications' and click on "Import" button. Select the `Application_Data.xml` file. For more information refer to <a href="http://documentation.bonitasoft.com/applications-0">applications in Bonitasoft official documentation</a>.
10. Run the Vacation Management application by accessing to this url (the port number may vary):<br/><a href="http://localhost:8080/bonita/apps/tahiti">http://localhost:8080/bonita/apps/tahiti</a> 

## Screenshots
#### Vacation Management Living Application
<img src="/screenshots/livingAppsMyVacationRequest.png?raw=true" alt="Vacation Management Living Application"/>

#### New Vacation Request process - Diagram
<img src="/screenshots/newVacationRequest.png?raw=true" alt="New Vacation Request process - Diagram"/>

#### New Vacation Request process - Vacation request creation form
<img src="/screenshots/formNewVacationRequestInstantiation.png?raw=true" alt="New Vacation Request process - Vacation request creation form"/>

#### New Vacation Request process - Vacation request review form
<img src="/screenshots/formReviewVacationRequest.png?raw=true" alt="New Vacation Request process - Vacation request review form"/>

#### Cancel Vacation Request process - Diagram
<img src="/screenshots/cancelVacationRequest.png?raw=true" alt="Cancel Vacation Request process - Diagram"/>

#### Cancel Vacation Request process - Vacation request to cancel selection form
<img src="/screenshots/formCancelVacationRequestInstantiation.png?raw=true" alt="Cancel Vacation Request process - Vacation request to cancel selection form"/>

#### Cancel Vacation Request process - Vacation request canceled review form
<img src="/screenshots/formReviewVacationRequestCancellation.png?raw=true" alt="Cancel Vacation Request process - Vacation request canceled review form"/>

#### Initiate Vacation Available process - Diagram
<img src="/screenshots/initiateVacationRequest.png?raw=true" alt="Initiate Vacation Available process - Diagram"/>

#### Remove all business data process - Diagram
<img src="/screenshots/initiateVacationRequest.png?raw=true" alt="Remove all business data process - Diagram"/>

## Notes

### SLF4J
Processes' Groovy script are using SLF4J to produce log messages. SLF4J is not embedded by default in Studio classloader. This explains why you need to import it manually. Note that the library is provided by Bonita BPM web application and so does not need to be embedded as process dependency.
