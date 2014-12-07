package in.codeseed.tubely.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by bala on 25/11/14.
 */
public class ObservableScrollView extends ScrollView {

    private CallBack mCallBack;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mCallBack.onScrollChanged(l, oldl, t, oldt);
    }

    public void setCallBack(CallBack listener) {
        if(mCallBack == null)
            this.mCallBack = listener;
    }

    public static interface CallBack{
        public void onScrollChanged(int l, int oldl, int t, int oldt);
    }
}
