package com.pbuchman.stash.autodecline.technical;

import com.atlassian.stash.pull.PullRequest;

/**
 * Class responsible for formatting title of {@link PullRequest}.
 * 
 * @author Piotr Buchman
 */
public class PullRequestTitleFormtter {
	
	/**
	 * Formats title of pull request.
	 * 
	 * @param pullRequest whose title need to be formatted. 
	 * @return formatted title.
	 */
	public String format(PullRequest pullRequest) {
		String title = pullRequest.getTitle()
				.replace("[", "/[")
				.replace("]", "/]");
		
		return title;
	}
}
