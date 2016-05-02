package at.se2.gruppe3.menschrgeredichnicht;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

public class BoardActivity extends Activity implements BoardView.OnFeldClickedListener{

    private BoardView BView;
    private RelativeLayout TableView;

    private Kegel[][] StartFelder;
    private Kegel[][] ZielFelder;

    private Kegel[] HauptFelder;

    Kegel KegelHighlighted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        StartFelder = new Kegel[4][4];
        ZielFelder = new Kegel[4][4];

        HauptFelder = new Kegel[40];

        TableView = (RelativeLayout)findViewById(R.id.tableView);
        TableView.setBackgroundResource(R.drawable.table);

        BView = (BoardView)findViewById(R.id.boardView);
        BView.setImageResource(R.drawable.board);

        startGame();
        moveDone();

    }

    public void startGame(){
        for(int i=0;i<=3;i++){
            for(int j=0;j<=3;j++){
                StartFelder[i][j] = new Kegel(i,0,j);
            }
        }
        Log.w("Logger","StartGame");
    }

    public void moveDone(){
        BView.setBoardState(StartFelder,ZielFelder,HauptFelder);
        BView.invalidate();
        Log.w("Logger","MoveDone");
    }

    @Override
    public void OnFeldClicked(int state,int player,int position) {
        switch(state){
            case 0:
                if(StartFelder[player][position]!=null){
                    KegelHighlighted = StartFelder[player][position];
                    BView.highlightKegel(KegelHighlighted);
                }
                break;
            case 1:
                if(KegelHighlighted!=null){
                    moveKegel(KegelHighlighted,state,position);
                }else{
                    KegelHighlighted = HauptFelder[position];
                    BView.highlightKegel(KegelHighlighted);
                }
                break;
            case 2:
                if(KegelHighlighted!=null){
                    moveKegel(KegelHighlighted,state,position);
                }else{
                    KegelHighlighted = ZielFelder[player][position];
                    BView.highlightKegel(KegelHighlighted);
                }
                break;
        }
        moveDone();
    }

    public void moveKegel(Kegel k,int state, int position){
        removeKegel(k);
        switch(state) {
            case 0:
                StartFelder[k.getPlayer()][position] = new Kegel(k.getPlayer(),state,position);
                break;
            case 1:
                if(HauptFelder[position]!=null){
                    kickKegel(HauptFelder[position]);
                }
                HauptFelder[position] = new Kegel(k.getPlayer(),state,position);
                break;
            case 2:
                ZielFelder[k.getPlayer()][position] = new Kegel(k.getPlayer(),state,position);
                break;
        }
        BView.resetHighlight();
        KegelHighlighted = null;
        moveDone();
    }

    public void kickKegel(Kegel k){
        for(int i=0;i<StartFelder[k.getPlayer()].length;i++){
            if(StartFelder[k.getPlayer()][i]==null){
                StartFelder[k.getPlayer()][i] = new Kegel(k.getPlayer(),0,i);
                break;
            }
        }
    }

    public void removeKegel(Kegel k){
        switch(k.getState()) {
            case 0:
                StartFelder[k.getPlayer()][k.getPosition()] = null;
                break;
            case 1:
                HauptFelder[k.getPosition()] = null;
                break;
            case 2:
                ZielFelder[k.getPlayer()][k.getPosition()] = null;
                break;
        }
    }
}