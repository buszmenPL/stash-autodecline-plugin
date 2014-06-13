package com.pbuchman.stash.autodecline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestService;

/**
 * Class responsible for handling {@link PullRequestRescopedEvent}.
 * 
 * @author Piotr Buchman
 */
public class PullRequestRescopedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PullRequestRescopedEventListener.class);

	private final PullRequestService pullRequestService;
	
	private final I18nService i18nService;
	
	public PullRequestRescopedEventListener(PullRequestService pullRequestService, I18nService i18nService) {
		this.pullRequestService = pullRequestService;
		this.i18nService = i18nService;
	}
	
	/**
	 * Performs validation of open pull requests from repository and branch to which pull request associated with passed 
	 * event was merged. Then declines all of them which are in conflicted state.
	 *  
	 * @param event that was raised by pull request's scope change.
	 */
	@EventListener
	public void declineConflictedPullRequests(PullRequestRescopedEvent event) {
		PullRequest pullRequest = event.getPullRequest();
		Integer repositoryId = pullRequest.getToRef().getRepository().getId();
		String branchId = pullRequest.getToRef().getId();
		
		PullRequestMergeability mergeability = pullRequestService.canMerge(repositoryId, pullRequest.getId());
		
		if (mergeability.isConflicted()) {
			logger.info("PR {} into repository ID: {}, branch ID: {} is conflicted, declining.", pullRequest.getId(), repositoryId, branchId);
			
			pullRequestService.decline(repositoryId, pullRequest.getId(), pullRequest.getVersion());
			
			explainWhyDeclined(repositoryId, pullRequest.getId());
		}
	}

	private void explainWhyDeclined(int repositoryId, long pullRequestId) {
		String fallbackMessage = "This pull request has been automatically declined due to conflicts.";
		String comment = i18nService.getText("pullRequest.automaticallyDeclined", fallbackMessage); 
		
		pullRequestService.addComment(repositoryId, pullRequestId, comment);
	}

}
