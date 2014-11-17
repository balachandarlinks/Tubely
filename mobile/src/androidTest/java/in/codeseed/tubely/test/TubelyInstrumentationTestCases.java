package in.codeseed.tubely.test;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import in.codeseed.tubely.activities.MainActivity;

/**
 * Created by bala on 15/10/14.
 */
public class TubelyInstrumentationTestCases extends ActivityInstrumentationTestCase2<MainActivity>{
    private MainActivity mainActivity;
    private Button fabButton;
    public TubelyInstrumentationTestCases() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        mainActivity = getActivity();
        //fabButton = (Button)mainActivity.findViewById(R.id.fab);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFabButton(){
        assertTrue(fabButton.getVisibility() == View.VISIBLE);
    }

    public void testTubeRefresh(){
        this.sendKeys(KeyEvent.KEYCODE_BACK);

    }

}
