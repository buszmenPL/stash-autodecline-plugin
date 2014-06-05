package com.pbuchman.stash.autodecline.technical;

import com.atlassian.stash.pull.PullRequest;

public class PullRequestTitleFormtter {
	
	public String format(PullRequest pullRequest) {
		String title = pullRequest.getTitle()
				.replace("[", "/[")
				.replace("]", "/]");
		
		return title;
	}
}
