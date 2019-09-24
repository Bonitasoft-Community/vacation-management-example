package org.bonitasoft.tahiti;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.session.APISession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.ut.tooling.BonitaBPMAssert;
import com.bonitasoft.ut.tooling.ProcessExecutionDriver;
import com.bonitasoft.ut.tooling.Server;
import com.company.model.VacationAvailable;
import com.company.model.VacationAvailableAssert;
import com.company.model.VacationRequest;
import com.company.model.VacationRequestAssert;

public class IntegrationTest {

	private static final String REJECT_COMMENTS = "Critical project milestone";

	private static final String NEW_VACATION_REQUEST = "New Vacation Request";

	private static final String INIT_VACATION_AVAILABLE = "Initiate Vacation Available";

	private static final String CLEAN_BDM_PROCESS = "Remove All Business Data";

	private static final String CANCEL_VACATION_REQUEST = "Cancel Vacation Request";

	private static final String MODIFY_VACATION_REQUEST = "Modify Pending Vacation Request";

	private static final String PROCESSES_VERSION = "1.5.0";

	private static APISession session;

	private static ProcessAPI processAPI;

	@BeforeClass
	public static void setUpClass() throws Exception {
		session = Server.httpConnect();
		processAPI = TenantAPIAccessor.getProcessAPI(session);

		BonitaBPMAssert.setUp(session, processAPI);
		ProcessExecutionDriver.setUp(processAPI);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		// Run a process to remove all business data
		ProcessExecutionDriver.createProcessInstance(CLEAN_BDM_PROCESS, PROCESSES_VERSION);

		BonitaBPMAssert.tearDown();
		Server.logout(session);
	}

	@Before
	public void setUp() throws Exception {
		ProcessExecutionDriver.prepareServer();
	}

	// @After
	// public void tearDown() throws Exception {
	//
	// }

	@Test
	public void testHappyPath() throws Exception {
		// Create process instance to initialize vacation available
		ProcessExecutionDriver.createProcessInstance(INIT_VACATION_AVAILABLE, PROCESSES_VERSION);

		// Create process instance
		long newVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(NEW_VACATION_REQUEST,
				PROCESSES_VERSION, newVacationRequestInputs());

		// Step reviewRequest
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(newVacationRequestProcessInstanceId, "Review request",
				reviewRequestApprovedInputs(), session.getUserId());

		// Check process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(newVacationRequestProcessInstanceId);

		// Check the vacation available counter
		VacationAvailable vacationAvailable = BonitaBPMAssert.assertBusinessDataNotNull(VacationAvailable.class,
				newVacationRequestProcessInstanceId, "requesterVacationAvailable");
		VacationAvailableAssert.assertThat(vacationAvailable).hasDaysAvailableCounter(9);
	}

	private Map<String, Serializable> newVacationRequestInputs() {
		Map<String, Serializable> submitLeaveRequestInputs = new HashMap<String, Serializable>();

		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		submitLeaveRequestInputs.put("startDateContract", today);
		submitLeaveRequestInputs.put("returnDateContract", tomorrow);
		submitLeaveRequestInputs.put("numberOfDaysContract", Integer.valueOf(1));

		return submitLeaveRequestInputs;
	}

	private Map<String, Serializable> reviewRequestApprovedInputs() {
		Map<String, Serializable> reviewRequestInputs = new HashMap<String, Serializable>();

		reviewRequestInputs.put("statusContract", VacationRequestStatus.APPROVED.getStatus());
		reviewRequestInputs.put("commentsContract", "");

		return reviewRequestInputs;
	}

	@Test
	public void testRejectPath() throws Exception {

		// Create process instance to initialize vacation available
		ProcessExecutionDriver.createProcessInstance(INIT_VACATION_AVAILABLE, PROCESSES_VERSION);

		// Create process instance
		Map<String, Serializable> newVacationRequestInputs = newVacationRequestInputs();
		long newVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(NEW_VACATION_REQUEST,
				PROCESSES_VERSION, newVacationRequestInputs);

		// Check reviewRequest step is pending
		ActivityInstance pendingHumanTask = BonitaBPMAssert.assertHumanTaskIsPending(
				newVacationRequestProcessInstanceId, "Review request");

		// Check that vacation request business data has the expected value
		VacationRequest vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.PENDING.getStatus())
				.hasNumberOfDays((Integer) newVacationRequestInputs.get("numberOfDaysContract"))
				.hasNewRequestProcessInstanceId(newVacationRequestProcessInstanceId)
				.hasRequesterBonitaUserId(session.getUserId())
				.hasReturnDate((LocalDate)newVacationRequestInputs.get("returnDateContract"))
				.hasStartDate((LocalDate)newVacationRequestInputs.get("startDateContract"));

		Assert.assertNull(vacationRequest.getReviewerBonitaUserId());
		Assert.assertNull(vacationRequest.getComments());

		// Execute the reviewRequest step
		ProcessExecutionDriver.executePendingHumanTask(pendingHumanTask, session.getUserId(),
				reviewRequestRefusedInputs());

		// Check process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(newVacationRequestProcessInstanceId);

		// Check that vacation request business data as the expected value
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.REFUSED.getStatus());

		// Check the vacation available counter
		VacationAvailable vacationAvailable = BonitaBPMAssert.assertBusinessDataNotNull(VacationAvailable.class,
				newVacationRequestProcessInstanceId, "requesterVacationAvailable");
		VacationAvailableAssert.assertThat(vacationAvailable).hasDaysAvailableCounter(10);

	}

	private Map<String, Serializable> reviewRequestRefusedInputs() {
		Map<String, Serializable> reviewRequestInputs = new HashMap<String, Serializable>();

		reviewRequestInputs.put("statusContract", VacationRequestStatus.REFUSED.getStatus());
		reviewRequestInputs.put("commentsContract", REJECT_COMMENTS);

		return reviewRequestInputs;
	}

	@Test
	public void testModify() throws Exception {
		// Create process instance to initialize vacation available
		ProcessExecutionDriver.createProcessInstance(INIT_VACATION_AVAILABLE, PROCESSES_VERSION);

		// Create a process instance of new vacation request process
		Map<String, Serializable> newVacationRequestInputs = newVacationRequestInputs();
		long newVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(NEW_VACATION_REQUEST,
				PROCESSES_VERSION, newVacationRequestInputs);

		// Check reviewRequest step is pending
		ActivityInstance pendingHumanTask = BonitaBPMAssert.assertHumanTaskIsPending(
				newVacationRequestProcessInstanceId, "Review request");

		// Check that vacation request business data has been created
		VacationRequest vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		// Run the modify process
		Map<String, Serializable> modifyVacationRequestInputs = modifyVacationRequestInputs(vacationRequest
				.getPersistenceId().toString());
		long modifyVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(
				MODIFY_VACATION_REQUEST, PROCESSES_VERSION, modifyVacationRequestInputs);

		// Check modify process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(modifyVacationRequestProcessInstanceId);

		// Check that vacation request business data as the expected value
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.PENDING.getStatus())
				.hasNumberOfDays((Integer) modifyVacationRequestInputs.get("numberOfDaysContract"))
				.hasNewRequestProcessInstanceId(newVacationRequestProcessInstanceId)
				.hasRequesterBonitaUserId(session.getUserId())
				.hasReturnDate((LocalDate)modifyVacationRequestInputs.get("returnDateContract"))
				.hasStartDate((LocalDate)modifyVacationRequestInputs.get("startDateContract"));

		Assert.assertNull(vacationRequest.getReviewerBonitaUserId());
		Assert.assertNull(vacationRequest.getComments());

		// Execute the reviewRequest step
		ProcessExecutionDriver.executePendingHumanTask(pendingHumanTask, session.getUserId(),
				reviewRequestApprovedInputs());

		// Check process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(newVacationRequestProcessInstanceId);

		// Check that vacation request business data as the expected value
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.APPROVED.getStatus());
		Assert.assertNotNull(vacationRequest.getReviewerBonitaUserId());

		// Check the vacation available counter
		VacationAvailable vacationAvailable = BonitaBPMAssert.assertBusinessDataNotNull(VacationAvailable.class,
				newVacationRequestProcessInstanceId, "requesterVacationAvailable");
		VacationAvailableAssert.assertThat(vacationAvailable).hasDaysAvailableCounter(8);
	}

	private Map<String, Serializable> modifyVacationRequestInputs(String vacationRequestId) {
		Map<String, Serializable> modifyLeaveRequestInputs = new HashMap<String, Serializable>();

		LocalDate today = LocalDate.now();
		
		LocalDate in2Days = today.plusDays(2);
		LocalDate in4Days = today.plusDays(4);

		modifyLeaveRequestInputs.put("vacationRequestIdContract", vacationRequestId);
		modifyLeaveRequestInputs.put("startDateContract", in2Days);
		modifyLeaveRequestInputs.put("returnDateContract", in4Days);
		modifyLeaveRequestInputs.put("numberOfDaysContract", Integer.valueOf(2));

		return modifyLeaveRequestInputs;
	}

	@Test
	public void testCancel() throws Exception {
		// Create process instance to initialize vacation available
		ProcessExecutionDriver.createProcessInstance(INIT_VACATION_AVAILABLE, PROCESSES_VERSION);

		// Create a process instance of new vacation request process
		Map<String, Serializable> newVacationRequestInputs = newVacationRequestInputs();
		long newVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(NEW_VACATION_REQUEST,
				PROCESSES_VERSION, newVacationRequestInputs);

		// Check reviewRequest step is pending
		BonitaBPMAssert.assertHumanTaskIsPending(newVacationRequestProcessInstanceId, "Review request");

		// Check that vacation request business data has been created
		VacationRequest vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		// Run the cancel process
		Map<String, Serializable> cancelVacationRequestInputs = cancelVacationRequestInputs(vacationRequest
				.getPersistenceId().toString());
		long cancelVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(
				CANCEL_VACATION_REQUEST, PROCESSES_VERSION, cancelVacationRequestInputs);

		// Check cancel process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(cancelVacationRequestProcessInstanceId);

		// Check that vacation request business data as the expected value
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.CANCELLED.getStatus())
				.hasNumberOfDays((Integer) newVacationRequestInputs.get("numberOfDaysContract"))
				.hasNewRequestProcessInstanceId(newVacationRequestProcessInstanceId)
				.hasRequesterBonitaUserId(session.getUserId())
				.hasReturnDate((LocalDate)newVacationRequestInputs.get("returnDateContract"))
				.hasStartDate((LocalDate)newVacationRequestInputs.get("startDateContract"));

		Assert.assertNull(vacationRequest.getReviewerBonitaUserId());
		Assert.assertNull(vacationRequest.getComments());

		// Check process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(newVacationRequestProcessInstanceId);

		// Check that vacation request business data as the expected value
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		Assert.assertNull(vacationRequest.getReviewerBonitaUserId());

		// Check the vacation available counter
		VacationAvailable vacationAvailable = BonitaBPMAssert.assertBusinessDataNotNull(VacationAvailable.class,
				newVacationRequestProcessInstanceId, "requesterVacationAvailable");
		VacationAvailableAssert.assertThat(vacationAvailable).hasDaysAvailableCounter(10);
	}

	private Map<String, Serializable> cancelVacationRequestInputs(String vacationRequestId) {
		Map<String, Serializable> cancelLeaveRequestInputs = new HashMap<String, Serializable>();

		cancelLeaveRequestInputs.put("vacationRequestIdContract", vacationRequestId);

		return cancelLeaveRequestInputs;
	}

	@Test
	public void testCancelAcceptedApproved() throws Exception {
		// Create process instance to initialize vacation available
		ProcessExecutionDriver.createProcessInstance(INIT_VACATION_AVAILABLE, PROCESSES_VERSION);

		// Create process instance
		long newVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(NEW_VACATION_REQUEST,
				PROCESSES_VERSION, newVacationRequestInputs());

		// Step reviewRequest
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(newVacationRequestProcessInstanceId, "Review request",
				reviewRequestApprovedInputs(), session.getUserId());

		// Check process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(newVacationRequestProcessInstanceId);

		// Check that vacation request business data exist
		VacationRequest vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		// Start cancel vacation request process instance
		long cancelVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(CANCEL_VACATION_REQUEST,
				PROCESSES_VERSION, cancelVacationRequestInputs(vacationRequest.getPersistenceId().toString()));

		// Step Review cancellation
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(cancelVacationRequestProcessInstanceId, "Review cancellation",
				reviewCancellationApprovedInputs(), session.getUserId());
		
		// Check cancel process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(cancelVacationRequestProcessInstanceId);

		// Check that vacation request business data is cancelled
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.CANCELLED.status);

		// Check the vacation available counter
		VacationAvailable vacationAvailable = BonitaBPMAssert.assertBusinessDataNotNull(VacationAvailable.class,
				newVacationRequestProcessInstanceId, "requesterVacationAvailable");
		VacationAvailableAssert.assertThat(vacationAvailable).hasDaysAvailableCounter(10);
	}

	private Map<String, Serializable> reviewCancellationApprovedInputs() {
		Map<String, Serializable> reviewCancellationApprovedInputs = new HashMap<String, Serializable>();

		reviewCancellationApprovedInputs.put("cancellationApprovedContract", Boolean.TRUE);

		return reviewCancellationApprovedInputs;
	}
	
	@Test
	public void testCancelAcceptedRefused() throws Exception {
		// Create process instance to initialize vacation available
		ProcessExecutionDriver.createProcessInstance(INIT_VACATION_AVAILABLE, PROCESSES_VERSION);

		// Create process instance
		long newVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(NEW_VACATION_REQUEST,
				PROCESSES_VERSION, newVacationRequestInputs());

		// Step reviewRequest
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(newVacationRequestProcessInstanceId, "Review request",
				reviewRequestApprovedInputs(), session.getUserId());

		// Check process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(newVacationRequestProcessInstanceId);

		// Check that vacation request business data exist
		VacationRequest vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		// Start cancel vacation request process instance
		long cancelVacationRequestProcessInstanceId = ProcessExecutionDriver.createProcessInstance(CANCEL_VACATION_REQUEST,
				PROCESSES_VERSION, cancelVacationRequestInputs(vacationRequest.getPersistenceId().toString()));

		// Step Review cancellation
		BonitaBPMAssert.assertHumanTaskIsPendingAndExecute(cancelVacationRequestProcessInstanceId, "Review cancellation",
				reviewCancellationRefusedInputs(), session.getUserId());
		
		// Check cancel process is finished
		BonitaBPMAssert.assertProcessInstanceIsFinished(cancelVacationRequestProcessInstanceId);

		// Check that vacation request business data is cancelled
		vacationRequest = BonitaBPMAssert.assertBusinessDataNotNull(VacationRequest.class,
				newVacationRequestProcessInstanceId, "vacationRequest");

		VacationRequestAssert.assertThat(vacationRequest).hasStatus(VacationRequestStatus.APPROVED.status);

		// Check the vacation available counter
		VacationAvailable vacationAvailable = BonitaBPMAssert.assertBusinessDataNotNull(VacationAvailable.class,
				newVacationRequestProcessInstanceId, "requesterVacationAvailable");
		VacationAvailableAssert.assertThat(vacationAvailable).hasDaysAvailableCounter(9);
	}

	private Map<String, Serializable> reviewCancellationRefusedInputs() {
		Map<String, Serializable> reviewCancellationRefusedInputs = new HashMap<String, Serializable>();

		reviewCancellationRefusedInputs.put("cancellationApprovedContract", Boolean.FALSE);

		return reviewCancellationRefusedInputs;
	}

	enum VacationRequestStatus {
		PENDING("pending"), APPROVED("approved"), REFUSED("refused"), PROCESSING_CANCELLATION("processing cancellation"), CANCELLED(
				"cancelled");

		private String status;

		private VacationRequestStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

	}
}
