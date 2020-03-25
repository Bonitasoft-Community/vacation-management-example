# Vacation management Living Application

## About
This is an official Living Application example for Bonita >7.10.3

This example demonstrates the following concepts:

* Living Application
* Forms and pages built using the UI Designer
* Process using BDM and contracts
* REST API extensions

## Installation

1. Start Bonita Studio 7.10.3 or later
1. Download the [project .bos file and REST API extension .zip file](https://github.com/Bonitasoft-Community/vacation-management-example/releases)
1. Import the .bos file in your Bonita Studio. It contains 5 diagrams: Example-NewVacationRequest, Example-ModifyPendingVacationRequest, Example-CancelVacationRequest, Example-InitiateVacationAvailable and Example-RemoveAllBusinessData. Each diagram includes a single process definition. It also includes business data model definition, default process configuration, application page and definition (i.e. application user interface definition). *Important*: importing the `.bos` file will ask you to override your existing business data model.
1. Open the Example-InitiateVacationAvailable diagram and run the process (click on **Run** button in a Bonita Studio). This process will initialize a business data that store vacation available counter for each Bonita user declared in the default test environment (ACME organization).
1. You can now deploy and run the *New Vacation Request*, *Modify Pending Vacation Request*  and *Cancel Vacation Request* processes by clicking on **Run** button in Bonita Studio. Note that vacation request approval need to be performed by requester (walter.bates) manager (helen.kelly) so you might want to logout and login back with helen.kelly/bpm user. If you want to create (or cancel) several vacation requests, do not click again on **Run** button. Instead click on the **Portal** button and in **User** view go to to **Processes** tab select the process and click on **Start** button.
1. You can now deploy the living application that give user an overview of all created vacation request.
   1. First you need to deploy a REST API extension:
      1. In the Portal, switch to **Administrator** view. Use drop down list on top right corner to switch from **User** to **Administrator**.
      1. Go to **Resources**.
      1. Click on **Add** button.
      1. Select the `vacationRestApiExtension.zip` file.
      1. Click on **Next** button.
      1. Click on **Confirm** button.
      1. Read more about [REST API extension](https://documentation.bonitasoft.com/bonita//rest-api-extensions) in Bonita [official documentation](https://documentation.bonitasoft.com/).
   1. Next you need to deploy the application page:
      1. In Bonita Studio project explorer unfold **Pages/Forms/Layout** node.
      1. Select *ExampleVacationManagement* application page.
      1. Do a right click and select **Deploy**.
      1. Read more about [pages](https://documentation.bonitasoft.com/bonita//pages) on the Bonita [official documentation](https://documentation.bonitasoft.com/).
1. You are now ready to deploy in the Portal the application definition that will use the previously deployed REST API extension and page:
   1. In Bonita Studio project explorer unfold **Application descriptors** node.
   1. Select *Application_Data.xml* application descriptor.
   1. Do a right click and select **Deploy**.
   1. For more information refer to [applications](https://documentation.bonitasoft.com/bonita//applications) in Bonita official documentation.
1. Run the Vacation Management application by accessing to this URL (the port number may vary): [http://localhost:8080/bonita/apps/tahiti](http://localhost:8080/bonita/apps/tahiti). You can find the URL of the application in the Portal **Administrator view** -> **Applications**.

## Screenshots
### Vacation Management Living Application
![Vacation Management Application](./screenshots/livingAppsMyVacationRequest.png?raw=true)

#### Vacation Management Living Application - Vacation request to cancel
![Vacation Management Living Application - Vacation request to cancel](./screenshots/formCancelVacationRequestInstantiation.png?raw=true)

### New Vacation Request process
![New Vacation Request process](./screenshots/newVacationRequest.png?raw=true)

#### New Vacation Request process - Vacation request creation form
![New Vacation Request process - Vacation request creation form](./screenshots/formNewVacationRequestInstantiation.png?raw=true)

#### New Vacation Request process - Vacation request review form
![New Vacation Request process - Vacation request review form](./screenshots/formReviewVacationRequest.png?raw=true)

### Modify Pending Vacation Request process
![Modify Pending Vacation Request process](./screenshots/modifyPendingVacationRequest.png?raw=true)

### Cancel Vacation Request process
![Cancel Vacation Request process](./screenshots/cancelVacationRequest.png?raw=true)

#### Cancel Vacation Request process - Vacation request canceled review form
![Cancel Vacation Request process - Vacation request canceled review form](./screenshots/formReviewVacationRequestCancellation.png?raw=true)

### Initiate Vacation Available process
![Initiate Vacation Available process](./screenshots/initiateVacationRequest.png?raw=true)

### Remove all business data process
![Remove all business data process](./screenshots/removeAllBusinessData.png?raw=true)

## Compatibility
This example has been created and built with Bonita 7.10.3 Community Edition.

It should be compatible with any newer version as well as Enterprise Edition.

## Known limitations
None so far.

## Issues
Reports issues and improvement requests on GitHub tracker.
