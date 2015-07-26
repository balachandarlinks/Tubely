package in.codeseed.tubely.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.codeseed.tubely.R;

public class TubeMapActivity extends BaseActivity {

    @Bind(R.id.map_layout) FrameLayout mMapLayout;
    @Bind(R.id.tube_map) SubsamplingScaleImageView mTubeMap;

    private static int MAP_MODE = 0;
    private Snackbar mSnackbar;
    private MenuItem mMapMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tube_map);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }else if(id == R.id.toggle_map){
            setMap(MAP_MODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_tube_map, menu);
        mMapMenuItem = menu.findItem(R.id.toggle_map);
        setMap(MAP_MODE);
        return true;
    }

    @OnClick(R.id.tube_map)
    public void tapOnMap(View view){
        if(getSupportActionBar().isShowing()){
            getSupportActionBar().hide();
        }else{
            getSupportActionBar().show();
        }
    }

    private void setMap(int mapMode){
        switch (mapMode){
            case 0:
                MAP_MODE = 1;
                mMapMenuItem.setIcon(getResources().getDrawable(R.drawable.night_map));
                mTubeMap.setImage(ImageSource.asset("london_tube_map.png").tilingDisabled());
                if(mSnackbar != null)
                    mSnackbar.dismiss();
                mSnackbar = Snackbar.make(mMapLayout, "Try London Night Tube Map", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OPEN", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setMap(MAP_MODE);
                            }
                        });
                mSnackbar.show();
                getSupportActionBar().setTitle("London Day Tube Map");
                break;
            case 1:
                MAP_MODE = 0;
                mMapMenuItem.setIcon(getResources().getDrawable(R.drawable.map));
                mTubeMap.setImage(ImageSource.asset("london_tube_night_map.png").tilingDisabled());
                if(mSnackbar != null)
                    mSnackbar.dismiss();
                mSnackbar = Snackbar.make(mMapLayout, "Try London Day Tube Map", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OPEN", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setMap(MAP_MODE);
                            }
                        });
                mSnackbar.show();
                getSupportActionBar().setTitle("London Night Tube Map");
                break;
        }
    }
}