package in.codeseed.tubely.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.codeseed.tubely.R;

public class TubeMapActivity extends BaseActivity {

    @Bind(R.id.tube_map) SubsamplingScaleImageView tubeMap;

    private static int MAP_MODE = 0;

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
            switch (MAP_MODE){
                case 0:
                    MAP_MODE = 1;
                    tubeMap.setImage(ImageSource.asset("london_tube_night_map.png").tilingDisabled());
                    item.setIcon(getResources().getDrawable(R.drawable.map));
                    getSupportActionBar().setTitle("London Night Tube Map");
                    break;
                case 1:
                    MAP_MODE = 0;
                    tubeMap.setImage(ImageSource.asset("london_tube_map.png").tilingDisabled());
                    item.setIcon(getResources().getDrawable(R.drawable.night_map));
                    getSupportActionBar().setTitle("London Tube Map");
                    break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_tube_map, menu);
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
}