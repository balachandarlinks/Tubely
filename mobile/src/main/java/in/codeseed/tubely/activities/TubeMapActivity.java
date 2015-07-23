package in.codeseed.tubely.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.codeseed.tubely.R;


public class TubeMapActivity extends BaseActivity {

    @Bind(R.id.tube_map)
    SubsamplingScaleImageView tubeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tube_map);
        ButterKnife.bind(this);

        tubeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getSupportActionBar().isShowing()){
                    getSupportActionBar().hide();

                }else{
                    getSupportActionBar().show();
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
