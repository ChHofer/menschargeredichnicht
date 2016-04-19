package at.se2.gruppe3.menschrgeredichnicht;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by chris on 17.04.2016.
 */
public class BoardView extends ImageView {

    private Coordinate[] boardC;

    public BoardView(Context context) {
        super(context);
        boardC = new Coordinate[121];
    }
    public BoardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        boardC = new Coordinate[121];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFF000000);
        p.setStrokeWidth(5);

        Log.w("","onDraw!");

        for(int i = 0; i<121; i++) {
            if(boardC[i]!=null) {
                Log.w("", "Draw:" + boardC[i].getX() + " - " + boardC[i].getY());
                canvas.drawPoint(boardC[i].getX(), boardC[i].getY(), p);
            }
        }

    }

    public void setBoardC(Coordinate[] boardC) {
        this.boardC = boardC;
    }

}
