package ut.com.pbuchman.stash.autodecline;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.pull.PullRequestSearchRequest;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.pbuchman.stash.autodecline.PullRequestMergedEventListener;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestMergedEventListenerTest {

	private static final long PULL_REQUEST_ID = 19L;
	private static final int PULL_REQUEST_VERSION = 66;
	private static final int REPOSITORY_ID = 45;
	private static final String DEST_BRANCH_ID = "456";

	@Mock
	private PullRequestService pullRequestService;
	
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
	
	private PullRequestMergedEventListener listener;
	
	@Before
	public void setup() {
		initializeMocks();
		
		this.listener = new PullRequestMergedEventListener(pullRequestService);
	}
	
	@Test
	public void shouldDeclineConflictedPullRequest() {
		// given
		when(mergeablity.isConflicted()).thenReturn(true);
		
		// when
		listener.declineConflictedPullRequests(event);
		
		// then
		verify(pullRequestService).search(any(PullRequestSearchRequest.class), any(PageRequest.class));
		verify(pullRequestService).canMerge(REPOSITORY_ID, PULL_REQUEST_ID);
		verify(pullRequestService).decline(REPOSITORY_ID, PULL_REQUEST_ID, PULL_REQUEST_VERSION);
		verify(pullRequestService).addComment(eq(REPOSITORY_ID), eq(PULL_REQUEST_ID), anyString());
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
		verify(pullRequestService).search(any(PullRequestSearchRequest.class), any(PageRequest.class));
		verify(pullRequestService).canMerge(REPOSITORY_ID, PULL_REQUEST_ID);
		verifyNoMoreInteractions(pullRequestService);
	}
	
	private void initializeMocks() {
		when(event.getPullRequest()).thenReturn(pullRequest);
		when(event.getRepository()).thenReturn(repository);
		
		when(pullRequest.getId()).thenReturn(PULL_REQUEST_ID);
		when(pullRequest.getVersion()).thenReturn(PULL_REQUEST_VERSION);
		when(pullRequest.getToRef()).thenReturn(destinationBranch);
		
		when(repository.getId()).thenReturn(REPOSITORY_ID);

		when(destinationBranch.getId()).thenReturn(DEST_BRANCH_ID);
		
		when(page.getValues()).thenReturn(singletonList(pullRequest));
		
		when(pullRequestService.search(any(PullRequestSearchRequest.class), any(PageRequest.class))).thenReturn(page);
		when(pullRequestService.canMerge(REPOSITORY_ID, PULL_REQUEST_ID)).thenReturn(mergeablity);
	}
}
