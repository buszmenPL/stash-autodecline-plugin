package ut.com.pbuchman.stash.autodecline;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;
import com.pbuchman.stash.autodecline.PullRequestRescopedEventListener;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestRescopedEventListenerTest {

	private static final long PULL_REQUEST_ID = 19L;
	private static final int PULL_REQUEST_VERSION = 66;
	
	private static final int REPOSITORY_ID = 45;
	private static final String DEST_BRANCH_ID = "456";

	@Mock
	private PullRequestService pullRequestService;
	
	@Mock
	private I18nService i18nService;
	
	@Mock
	private PullRequestRescopedEvent event;
	
	@Mock 
	private PullRequest pullRequest;
	
	@Mock 
	private Repository repository;
	
	@Mock
	private PullRequestRef destinationBranch;
		
	@Mock
	private PullRequestMergeability mergeablity;
	
	private PullRequestRescopedEventListener listener;
	
	@Before
	public void setup() {
		initializeMocks();
		
		this.listener = new PullRequestRescopedEventListener(pullRequestService, i18nService);
	}
	
	@Test
	public void shouldDeclineConflictedPullRequest() {
		// given
		when(mergeablity.isConflicted()).thenReturn(true);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
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
	public void shouldNotDeclineNotConflictedPullRequest() {
		// given
		when(mergeablity.isConflicted()).thenReturn(false);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
		verify(pullRequestService, times(0)).decline(REPOSITORY_ID, PULL_REQUEST_ID, PULL_REQUEST_VERSION);
	}
	
	private void initializeMocks() {
		mockEvent();
		mockPullRequest();
		mockRepository();
		mockDestinationBranch();
		mockPullRequestService();
	}
	
	private void mockEvent() {
		when(event.getPullRequest()).thenReturn(pullRequest);
	}
	
	private void mockPullRequest() {
		when(pullRequest.getId()).thenReturn(PULL_REQUEST_ID);
		when(pullRequest.getVersion()).thenReturn(PULL_REQUEST_VERSION);
		when(pullRequest.getToRef()).thenReturn(destinationBranch);
	}
	
	private void mockRepository() {
		when(repository.getId()).thenReturn(REPOSITORY_ID);
	}

	private void mockDestinationBranch() {
		when(destinationBranch.getId()).thenReturn(DEST_BRANCH_ID);
		when(destinationBranch.getRepository()).thenReturn(repository);
	}

	private void mockPullRequestService() {
		when(pullRequestService.canMerge(REPOSITORY_ID, PULL_REQUEST_ID)).thenReturn(mergeablity);
	}
}
