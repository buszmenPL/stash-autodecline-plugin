package ut.com.pbuchman.stash.autodecline.technical;

import static junitparams.JUnitParamsRunner.$;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.atlassian.stash.pull.PullRequest;
import com.pbuchman.stash.autodecline.technical.PullRequestTitleFormtter;

@RunWith(JUnitParamsRunner.class)
public class PullRequestTitleFormtterTest {
	
	@Mock
	private PullRequest pullRequest;
	
	private PullRequestTitleFormtter formatter = new PullRequestTitleFormtter(); 
	
	@Before
	public void initializeMocks() {
		initMocks(this);
	}
	
	@Test
    @Parameters( method = "pullRequestsTitlesProvider" )
	public void shouldFormatPullRequestTitle(String expectedTitle, String givenTitle) {
		when(pullRequest.getTitle()).thenReturn(givenTitle);
		
		String actualTitle = formatter.format(pullRequest);
		
		assertThat(actualTitle).isEqualTo(expectedTitle);
	}
	
	protected Object[] pullRequestsTitlesProvider() {
		return $(
            $("Title", "Title"),
            $("Ti/]tle", "Ti]tle"),
            $("Titl/[e", "Titl[e"),
            $("Titl/[e/]", "Titl[e]"),
            $("Titl/[/[/[e", "Titl[[[e"),
            $("Titl/]/]/]e", "Titl]]]e")
       );
	}
}
