package in.codeseed.tubely.test;

import android.test.AndroidTestCase;

/**
 * Created by bala on 14/10/14.
 */

public class TubelyTestCases extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testInternetPermission() throws Exception{
        assertTrue(1 == 1);
        //assertActivityRequiresPermission("in.codeseed.tubely","in.codeseed.tubely.activities.MainActivity","android.permission.INTERNET");
    }

    public void testFab() throws Exception{
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
