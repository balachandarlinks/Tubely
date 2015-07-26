package in.codeseed.tubely.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.List;

import in.codeseed.tubely.R;
import in.codeseed.tubely.util.Util;

/**
 * Created by bala on 3/12/14.
 */
public class BatchListLayout extends LinearLayout{

    private static final int SPACE_BETWEEN_LINE_BATCHES = 15;

    private String[] lines;
    private TextView mBatchTextView;
    private Space mSpaceView;
    private Util util;

    public BatchListLayout(Context context) {
        super(context);
        util = Util.getInstance(getContext().getApplicationContext());
    }

    public BatchListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        util = Util.getInstance(getContext().getApplicationContext());
    }

    public BatchListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        util = Util.getInstance(getContext().getApplicationContext());
    }

    public String[] getLines() {
        return lines;
    }

    public void setLines(String[] lines) {

        this.lines = lines;
        removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        if(isLinesDataAvailable(lines)){

            for(String line: lines){
                mBatchTextView = (TextView) inflater.inflate(R.layout.line_batch_textview, null);
                mSpaceView = new Space(getContext());

                mSpaceView.setLayoutParams(new ViewGroup.LayoutParams(SPACE_BETWEEN_LINE_BATCHES, ViewGroup.LayoutParams.WRAP_CONTENT));

                mBatchTextView.setText(line);
                mBatchTextView.setBackgroundResource(util.getLineColorResource(line));

                addView(mBatchTextView);
                addView(mSpaceView);
            }

        }else {
            mBatchTextView = (TextView) inflater.inflate(R.layout.line_batch_textview, null);
            mBatchTextView.setText("Lines Data Not Available!");
            mBatchTextView.setBackgroundResource(R.color.colorAccent);
            addView(mBatchTextView);
        }
    }

    public void setTrainTimes(String line, List<String> trainTimes) {
        removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        if(trainTimes != null && trainTimes.size() > 0){
            for(String time: trainTimes){
                mBatchTextView = (TextView) inflater.inflate(R.layout.line_batch_textview, null);
                mSpaceView = new Space(getContext());

                mSpaceView.setLayoutParams(new ViewGroup.LayoutParams(SPACE_BETWEEN_LINE_BATCHES, ViewGroup.LayoutParams.WRAP_CONTENT));

                mBatchTextView.setText(time);
                mBatchTextView.setBackgroundResource(util.getLineColorResource(line));

                addView(mBatchTextView);
                addView(mSpaceView);
            }

        }else {
            setVisibility(View.GONE);
        }
    }

    private boolean isLinesDataAvailable(String[] lines){
        return (lines.length >= 1 && !lines[0].isEmpty());
    }

}