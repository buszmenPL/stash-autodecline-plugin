package ut.com.pbuchman.stash.autodecline;

import org.junit.Test;
import com.pbuchman.stash.autodecline.MyPluginComponent;
import com.pbuchman.stash.autodecline.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}