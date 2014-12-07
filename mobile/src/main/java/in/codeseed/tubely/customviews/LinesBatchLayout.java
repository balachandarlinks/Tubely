package in.codeseed.tubely.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import in.codeseed.tubely.R;
import in.codeseed.tubely.util.Util;

/**
 * Created by bala on 3/12/14.
 */
public class LinesBatchLayout extends LinearLayout{

    private static final int SPACE_BETWEEN_LINE_BATCHES = 15;

    private String[] lines;
    private TextView mLineTextView;
    private Space mSpaceView;

    public LinesBatchLayout(Context context) {
        super(context);
    }

    public LinesBatchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinesBatchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
                mLineTextView = (TextView) inflater.inflate(R.layout.line_batch_textview, null);
                mSpaceView = new Space(getContext());

                mSpaceView.setLayoutParams(new ViewGroup.LayoutParams(SPACE_BETWEEN_LINE_BATCHES, ViewGroup.LayoutParams.WRAP_CONTENT));

                mLineTextView.setText(line);
                mLineTextView.setBackgroundResource(Util.getLineColorResource(line));

                addView(mLineTextView);
                addView(mSpaceView);
            }

        }else {

            mLineTextView = (TextView) inflater.inflate(R.layout.line_batch_textview, null);
            mLineTextView.setText("Lines Data Not Available!");
            mLineTextView.setBackgroundResource(R.color.colorAccent);
            addView(mLineTextView);
        }


    }

    private boolean isLinesDataAvailable(String[] lines){
        return (lines.length >= 1 && !lines[0].isEmpty());
    }

}
