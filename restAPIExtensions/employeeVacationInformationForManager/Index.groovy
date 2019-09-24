import com.company.model.VacationAvailableDAO
import com.company.model.VacationRequestDAO
import com.company.model.VacationRequest
import groovy.json.JsonBuilder
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest

public class Index implements RestApiController {

	final REVIEW_REQUEST_TASK_NAME = 'Review request'

    private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft")

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {

        // User session to get the user Bonita id to search for managed employee vacation requests
        def apiClient = context.apiClient

        // Get a reference to IdentityAPI to search for users managed by current user
        def identityAPI = apiClient.identityAPI

        // Get a reference to ProcessAPI to search for task id based on process instance id
        def processAPI = apiClient.processAPI

        // DAO to access vacation request and vacation available
        def vacationRequestDAO = context.apiClient.getDAO(VacationRequestDAO.class)
        def vacationAvailableDAO = context.apiClient.getDAO(VacationAvailableDAO.class)

        // Current user id
        def managerUserId = context.apiSession.userId
        LOGGER.debug("Manager id is: {}", managerUserId)

        // Search for users managed by current user and build users id list
        def searchBuilder = new SearchOptionsBuilder(0, 100)
        searchBuilder.filter(UserSearchDescriptor.MANAGER_USER_ID, managerUserId)
        def users = identityAPI.searchUsers(searchBuilder.done()).result
        LOGGER.debug("Users managed by: {}  are: {}", managerUserId, users)

        // If current user manage at least one user he/she is a manager
        def isManager = !users.empty

        // Initialize the list of pending vacation requests for all employees
        def employeesVacationRequests = []

        // Initialize the list of vacation available for all employees
        def employeesVacationAvailable = []

        // For each employee
        for(user in users) {
            // Current user id
            def employeeId = user.id

            // Get pending vacation request
            def currentUserVacationRequests = vacationRequestDAO.findByRequesterBonitaUserId(employeeId, 0, 100)

            // For each vacation request of current employee
            for(currentUserVacationRequest in currentUserVacationRequests) {
                // Id of the task to approve/refuse pending vacation request
                // null for vacation request that are not pending
                def reviewRequestTaskId= null

                // If vacation request is pending
                if(currentUserVacationRequest.status == "pending") {
                    // Search for "Review request" task id using "New Vacation Request" process instance id store in business variable "VacationRequest"
                    reviewRequestTaskId = processAPI.getHumanTaskInstances(currentUserVacationRequest.newRequestProcessInstanceId, REVIEW_REQUEST_TASK_NAME, 0, 1).get(0).id
                }

                // Map with current user single pending vacation request
                def vacationRequestInfo = [firstName: user.firstName, lastName: user.lastName, startDate: currentUserVacationRequest.startDate.toString(), returnDate: currentUserVacationRequest.returnDate.toString(), numberOfDays: currentUserVacationRequest.numberOfDays, status: currentUserVacationRequest.status, taskId: reviewRequestTaskId]

                // Add current employee vacation request to the global list
                employeesVacationRequests << vacationRequestInfo
            }

            // Get vacation available
            def vacationAvailable = vacationAvailableDAO.findByBonitaUserId(employeeId)

            def vacationAvailableInfo = [firstName: user.firstName, lastName: user.lastName, daysAvailableCounter: vacationAvailable.daysAvailableCounter]

            // Add current employee vacation available to the global list
            employeesVacationAvailable << vacationAvailableInfo
        }
                  
        //
        def response = [isManager: isManager, employeesVacationRequests: employeesVacationRequests, employeesVacationAvailable: employeesVacationAvailable]
        
		// Use error log level to avoid the need of logging configuration when executed from test environment embedded in Bonita Studio
        LOGGER.error("Response: " + response)

        responseBuilder.with {
            withResponse new JsonBuilder(response).toPrettyString()
            build()
        }
    }

}
