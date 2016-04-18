package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BoardActivity extends Activity {

    private BoardView BoardView;
    private RelativeLayout TableView;
    private Coordinate[] boardC;

    private int displayWidth, displayHeight;
    private int boardWidth, boardHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        TableView = (RelativeLayout)findViewById(R.id.tableView);
        TableView.setBackgroundResource(R.drawable.table);

        BoardView = (BoardView)findViewById(R.id.boardView);
        BoardView.setImageResource(R.drawable.board);



        BoardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                BoardView drawView = (BoardView) v;
                getDisplaySize();
                Log.w("","DS:"+displayWidth+"-"+displayHeight);
                Log.w("","BS:"+boardWidth+"-"+boardHeight);

                boardC=new Coordinate[121];

                int y,x;
                int yC,xC;
                for(int i = 0; i<121; i++){
                    y = i/11;
                    x = i-(y*11);

                    xC = (int)(((x*8.9+5.5)*boardWidth)/100);
                    yC = (int)(((y*8.9+5.5)*boardHeight)/100);

                    boardC[i]=new Coordinate(xC,yC);
                }
                drawView.setBoardC(boardC);

                drawView.invalidate();

                return true;
            }
        });
    }

    public void getDisplaySize(){
        boardWidth = BoardView.getMeasuredWidth();
        boardHeight = BoardView.getMeasuredHeight();
    }
}