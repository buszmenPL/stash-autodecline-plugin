package com.pbuchman.stash.autodecline;

import static com.atlassian.stash.pull.PullRequestState.OPEN;
import static java.lang.String.format;

import com.atlassian.event.api.AsynchronousPreferred;
import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestSearchRequest;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;

@AsynchronousPreferred
public class PullRequestMergedEventListener {
	
	private final PullRequestService pullRequestService;

	public PullRequestMergedEventListener(PullRequestService pullRequestService) {
		this.pullRequestService = pullRequestService;
	}
	
	@EventListener
	public void declineConflictedPullRequests(PullRequestMergedEvent event) {
		int repositoryId = event.getRepository().getId();
		
		PullRequestSearchRequest searchRequest = new PullRequestSearchRequest.Builder()
				.toBranchId(event.getPullRequest().getToRef().getId())
				.toRepositoryId(repositoryId)
				.state(OPEN)
				.build();
		PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
		
		Page<PullRequest> pullRequests = pullRequestService.search(searchRequest, pageRequest);
		
		for (PullRequest pullRequest : pullRequests.getValues()) {
			PullRequestMergeability mergeability = pullRequestService.canMerge(repositoryId, pullRequest.getId());
			
			if (mergeability.isConflicted()) {
				pullRequestService.decline(repositoryId, pullRequest.getId(), pullRequest.getVersion());
				pullRequestService.addComment(repositoryId, pullRequest.getId(), createDeclineComment(event.getPullRequest()));
			}
		}
	}

	private String createDeclineComment(PullRequest pullRequest) {
		String message = format("This pull request has been automatically declined due to conflicts after merge of PR: '%s'", pullRequest.getTitle());
		return message;
	}

}
