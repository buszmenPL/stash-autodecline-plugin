package ut.com.pbuchman.stash.autodecline;

import static com.atlassian.stash.pull.PullRequestState.OPEN;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.i18n.KeyedMessage;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestDirection;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestOrder;
import com.atlassian.stash.pull.PullRequestOutOfDateException;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.pbuchman.stash.autodecline.PullRequestMergedEventListener;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestMergedEventListenerTest {

	private static final long PULL_REQUEST_ID = 19L;
	private static final int PULL_REQUEST_VERSION = 66;
	private static final String PULL_REQUEST_TITLE = "Pull Request Title";
	private static final String PULL_REQUEST_URL = "https:\\\\domain.com\\projects\\PROJECT_1\\repos\\rep_1\\pull-requests\\1\\overview";
	
	private static final int REPOSITORY_ID = 45;
	private static final String DEST_BRANCH_ID = "456";

	@Mock
	private PullRequestService pullRequestService;
	
	@Mock
	private I18nService i18nService;
	
	@Mock
	private PullRequestMergedEvent event;
	
	@Mock 
	private PullRequest pullRequest;
	
	@Mock 
	private Repository repository;
	
	@Mock
	private PullRequestRef destinationBranch;
	
	@Mock
	private Page<PullRequest> page;
	
	@Mock
	private PullRequestMergeability mergeablity;

	@Mock
	private NavBuilder navBuilder;
	
	@Mock
	private NavBuilder.Repo navRepo;
	
	@Mock
	private NavBuilder.PullRequest navPullRequest;
	
	private PullRequestMergedEventListener listener;
	
	@Before
	public void setup() {
		initializeMocks();
		
		this.listener = new PullRequestMergedEventListener(pullRequestService, i18nService, navBuilder);
	}
	
	@Test
	public void shouldDeclineConflictedPullRequest() {
		// given
		when(mergeablity.isConflicted()).thenReturn(true);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
		verify(pullRequestService).findInDirection(any(PullRequestDirection.class), eq(REPOSITORY_ID), 
				eq(DEST_BRANCH_ID), eq(OPEN), any(PullRequestOrder.class), any(PageRequest.class));
		verify(pullRequestService).canMerge(REPOSITORY_ID, PULL_REQUEST_ID);
		verify(pullRequestService).decline(REPOSITORY_ID, PULL_REQUEST_ID, PULL_REQUEST_VERSION);
	}
	
	@Test
	public void shouldAddCommentWhenDecliningPullRequest() {
		// given
		when(mergeablity.isConflicted()).thenReturn(true);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
		verify(pullRequestService).addComment(eq(REPOSITORY_ID), eq(PULL_REQUEST_ID), anyString());
	}
	
	@Test
	public void shouldFetchAgainOutOfDatedPullRequest() {
		// given
		KeyedMessage message = new KeyedMessage("key", "localisedMessage", "rootMessage");
		PullRequest updatedPullRequest = mock(PullRequest.class);
		int updatedVersion = 23;
		
		when(mergeablity.isConflicted()).thenReturn(true);
		when(pullRequestService.decline(REPOSITORY_ID, PULL_REQUEST_ID, PULL_REQUEST_VERSION))
			.thenThrow(new PullRequestOutOfDateException(message, new RuntimeException()));
		when(pullRequestService.findById(REPOSITORY_ID, PULL_REQUEST_ID)).thenReturn(updatedPullRequest);
		when(updatedPullRequest.getVersion()).thenReturn(updatedVersion);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
//		verify(pullRequestService).decline(REPOSITORY_ID, PULL_REQUEST_ID, PULL_REQUEST_VERSION);
		verify(pullRequestService).findById(REPOSITORY_ID, PULL_REQUEST_ID);
		verify(pullRequestService).decline(REPOSITORY_ID, PULL_REQUEST_ID, updatedVersion);
	}
	
	@Test
	public void shouldNotDeclineNotConflictedPullRequest() {
		// given
		when(mergeablity.isConflicted()).thenReturn(false);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
		verify(pullRequestService).findInDirection(any(PullRequestDirection.class), eq(REPOSITORY_ID), 
				eq(DEST_BRANCH_ID), eq(OPEN), any(PullRequestOrder.class), any(PageRequest.class));
		verify(pullRequestService).canMerge(REPOSITORY_ID, PULL_REQUEST_ID);
		verifyNoMoreInteractions(pullRequestService);
	}
	
	private void initializeMocks() {
		mockEvent();
		mockPullRequest();
		
		when(repository.getId()).thenReturn(REPOSITORY_ID);

		when(destinationBranch.getId()).thenReturn(DEST_BRANCH_ID);
		
		when(page.getValues()).thenReturn(singletonList(pullRequest));

		when(pullRequestService.findInDirection(any(PullRequestDirection.class), eq(REPOSITORY_ID), 
				eq(DEST_BRANCH_ID), eq(OPEN), any(PullRequestOrder.class), any(PageRequest.class))).thenReturn(page);
		when(pullRequestService.canMerge(REPOSITORY_ID, PULL_REQUEST_ID)).thenReturn(mergeablity);
		
		when(navBuilder.repo(repository)).thenReturn(navRepo);
		when(navRepo.pullRequest(PULL_REQUEST_ID)).thenReturn(navPullRequest);
		when(navPullRequest.buildAbsolute()).thenReturn(PULL_REQUEST_URL);
	}

	private void mockEvent() {
		when(event.getPullRequest()).thenReturn(pullRequest);
		when(event.getRepository()).thenReturn(repository);
	}

	private void mockPullRequest() {
		when(pullRequest.getId()).thenReturn(PULL_REQUEST_ID);
		when(pullRequest.getTitle()).thenReturn(PULL_REQUEST_TITLE);
		when(pullRequest.getVersion()).thenReturn(PULL_REQUEST_VERSION);
		when(pullRequest.getToRef()).thenReturn(destinationBranch);
	}
}
