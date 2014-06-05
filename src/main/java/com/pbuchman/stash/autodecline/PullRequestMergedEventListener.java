package com.pbuchman.stash.autodecline;

import static com.atlassian.stash.pull.PullRequestState.OPEN;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestSearchRequest;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.pbuchman.stash.autodecline.technical.PullRequestTitleFormtter;

public class PullRequestMergedEventListener {

	private final PullRequestService pullRequestService;
	
	private final I18nService i18nService;
	
	private final NavBuilder navBuilder;
	
	private final PullRequestTitleFormtter titleFormatter = new PullRequestTitleFormtter(); 

	public PullRequestMergedEventListener(PullRequestService pullRequestService, 
			I18nService i18nService, NavBuilder navBuilder) {
		
		this.pullRequestService = pullRequestService;
		this.i18nService = i18nService;
		this.navBuilder = navBuilder;
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
				
				String comment = createDeclineComment(event.getRepository(), event.getPullRequest());
				pullRequestService.addComment(repositoryId, pullRequest.getId(), comment);
			}
		}
	}

	private String createDeclineComment(Repository repository, PullRequest pullRequest) {
		String url = navBuilder.repo(repository).pullRequest(pullRequest.getId()).buildAbsolute();
		String title = titleFormatter.format(pullRequest);
		
		String message = i18nService.getMessage("pullRequest.automaticallyDeclined", title, url); 
		return message;
	}

}
