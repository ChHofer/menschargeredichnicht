package at.se2.gruppe3.menschrgeredichnicht;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by chris on 17.04.2016.
 */
public class BoardView extends ImageView {

    private Coordinate[][] boardCoordStart;
    private Coordinate[][] boardCoordZiel;
    private Coordinate[] boardCoordHaupt;

    private int[] index;

    private int boardWidth, boardHeight;
    private boolean boardInitialized;

    private Kegel[][] StartFelder;
    private Kegel[][] ZielFelder;
    private Kegel[] HauptFelder;

    public BoardView(Context context) {
        super(context);
    }
    public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    private void initBoard(){
        getDisplaySize();
        index = new int[9];
        boardCoordStart = new Coordinate[4][4];
        boardCoordZiel = new Coordinate[4][4];
        boardCoordHaupt = new Coordinate[40];

        StartFelder = new Kegel[4][4];
        ZielFelder = new Kegel[4][4];
        HauptFelder = new Kegel[40];

        int y,x;
        int yC,xC;
        for(int i = 0; i<121; i++){
            y = i/11;
            x = i-(y*11);

            xC = (int)(((x*8.9+5.5)*boardWidth)/100);
            yC = (int)(((y*8.9+5.5)*boardHeight)/100);

            //Koordinaten zuordnen
            // Start Felder
            if(x<2 && y<2){ // Player 1 Start Feld
                boardCoordStart[0][index[0]] = new Coordinate(xC,yC);
                Log.w("Logged","boardCoordStart["+0+"]["+index[0]+"] = new Coordinate("+xC+","+yC+");");
                index[0]++;
            }else if(x>8 && y<2){ // Player 2 Start Feld
                boardCoordStart[1][index[1]] = new Coordinate(xC,yC);
                index[1]++;
            }else if(x<2 && y>8){ // Player 3 Start Feld
                boardCoordStart[2][index[2]] = new Coordinate(xC,yC);
                index[2]++;
            }else if(x>8 && y>8){ // Player 4 Start Feld
                boardCoordStart[3][index[3]] = new Coordinate(xC,yC);
                index[3]++;
            }
            // Ziel Felder
            if(y==5 && x>0 && x<5){ // Player 1 Ziel Feld
                boardCoordZiel[0][index[4]] = new Coordinate(xC,yC);
                Log.w("Logged","boardCoordZiel["+0+"]["+index[0]+"] = new Coordinate("+xC+","+yC+");");
                index[4]++;
            }else if(x==5 && y>0 && y<5){ // Player 2 Ziel Feld
                boardCoordZiel[1][index[5]] = new Coordinate(xC,yC);
                index[5]++;
            }else if(y==5 && x>5 && x<10){ // Player 3 Ziel Feld
                boardCoordZiel[2][3-index[6]] = new Coordinate(xC,yC);
                index[6]++;
            }else if(x==5 && y>5 && y<10){ // Player 4 Ziel Feld
                boardCoordZiel[3][3-index[7]] = new Coordinate(xC,yC);
                index[7]++;
            }
            // Haupt Felder
            if(x==0 && y==4) boardCoordHaupt[0]=new Coordinate(xC,yC);
            if(x==1 && y==4) boardCoordHaupt[1]=new Coordinate(xC,yC);
            if(x==2 && y==4) boardCoordHaupt[2]=new Coordinate(xC,yC);
            if(x==3 && y==4) boardCoordHaupt[3]=new Coordinate(xC,yC);
            if(x==4 && y==4) boardCoordHaupt[4]=new Coordinate(xC,yC);
            if(x==4 && y==3) boardCoordHaupt[5]=new Coordinate(xC,yC);
            if(x==4 && y==2) boardCoordHaupt[6]=new Coordinate(xC,yC);
            if(x==4 && y==1) boardCoordHaupt[7]=new Coordinate(xC,yC);
            if(x==4 && y==0) boardCoordHaupt[8]=new Coordinate(xC,yC);
            if(x==5 && y==0) boardCoordHaupt[9]=new Coordinate(xC,yC);
            if(x==6 && y==0) boardCoordHaupt[10]=new Coordinate(xC,yC);
            if(x==6 && y==1) boardCoordHaupt[11]=new Coordinate(xC,yC);
            if(x==6 && y==2) boardCoordHaupt[12]=new Coordinate(xC,yC);
            if(x==6 && y==3) boardCoordHaupt[13]=new Coordinate(xC,yC);
            if(x==6 && y==4) boardCoordHaupt[14]=new Coordinate(xC,yC);
            if(x==7 && y==4) boardCoordHaupt[15]=new Coordinate(xC,yC);
            if(x==8 && y==4) boardCoordHaupt[16]=new Coordinate(xC,yC);
            if(x==9 && y==4) boardCoordHaupt[17]=new Coordinate(xC,yC);
            if(x==10 && y==4) boardCoordHaupt[18]=new Coordinate(xC,yC);
            if(x==10 && y==5) boardCoordHaupt[19]=new Coordinate(xC,yC);
            if(x==10 && y==6) boardCoordHaupt[20]=new Coordinate(xC,yC);
            if(x==9 && y==6) boardCoordHaupt[21]=new Coordinate(xC,yC);
            if(x==8 && y==6) boardCoordHaupt[22]=new Coordinate(xC,yC);
            if(x==7 && y==6) boardCoordHaupt[23]=new Coordinate(xC,yC);
            if(x==6 && y==6) boardCoordHaupt[24]=new Coordinate(xC,yC);
            if(x==6 && y==7) boardCoordHaupt[25]=new Coordinate(xC,yC);
            if(x==6 && y==8) boardCoordHaupt[26]=new Coordinate(xC,yC);
            if(x==6 && y==9) boardCoordHaupt[27]=new Coordinate(xC,yC);
            if(x==6 && y==10) boardCoordHaupt[28]=new Coordinate(xC,yC);
            if(x==5 && y==10) boardCoordHaupt[29]=new Coordinate(xC,yC);
            if(x==4 && y==10) boardCoordHaupt[30]=new Coordinate(xC,yC);
            if(x==4 && y==9) boardCoordHaupt[31]=new Coordinate(xC,yC);
            if(x==4 && y==8) boardCoordHaupt[32]=new Coordinate(xC,yC);
            if(x==4 && y==7) boardCoordHaupt[33]=new Coordinate(xC,yC);
            if(x==4 && y==6) boardCoordHaupt[34]=new Coordinate(xC,yC);
            if(x==3 && y==6) boardCoordHaupt[35]=new Coordinate(xC,yC);
            if(x==2 && y==6) boardCoordHaupt[36]=new Coordinate(xC,yC);
            if(x==1 && y==6) boardCoordHaupt[37]=new Coordinate(xC,yC);
            if(x==0 && y==6) boardCoordHaupt[38]=new Coordinate(xC,yC);
            if(x==0 && y==5) boardCoordHaupt[39]=new Coordinate(xC,yC);
        }

        boardInitialized = true;
    }

    public void getDisplaySize(){
        boardWidth = this.getMeasuredWidth();
        boardHeight = this.getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(!boardInitialized){
            initBoard();
        }

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setFilterBitmap(true);

        int w=65;
        int h=90;
        Bitmap KegelRot = BitmapFactory.decodeResource(getResources(), R.drawable.game_piece_red);
        Bitmap KegelGrün = BitmapFactory.decodeResource(getResources(), R.drawable.game_piece_green);
        Bitmap KegelGelb = BitmapFactory.decodeResource(getResources(), R.drawable.game_piece_yellow);
        Bitmap KegelBlau = BitmapFactory.decodeResource(getResources(), R.drawable.game_piece_blue);
        KegelRot = Bitmap.createScaledBitmap(KegelRot, w, h, false);
        KegelGrün = Bitmap.createScaledBitmap(KegelGrün, w, h, false);
        KegelGelb = Bitmap.createScaledBitmap(KegelGelb, w, h, false);
        KegelBlau = Bitmap.createScaledBitmap(KegelBlau, w, h, false);

        Log.w("","onDraw!");

        // Kegel auf Start Feldern zeichnen
        for(int i=0;i< boardCoordStart.length;i++){
            for(int j=0;j<boardCoordStart[i].length;j++){
                if(boardCoordStart[i][j]!=null && StartFelder[i][j]==null) {
                    if(i==0){
                        canvas.drawBitmap(KegelRot, boardCoordStart[i][j].getX()-(w/2), boardCoordStart[i][j].getY()-(h/2)-15, p);
                    }else if(i==1){
                        canvas.drawBitmap(KegelBlau, boardCoordStart[i][j].getX()-(w/2), boardCoordStart[i][j].getY()-(h/2)-15, p);
                    }else if(i==2){
                        canvas.drawBitmap(KegelGelb, boardCoordStart[i][j].getX()-(w/2), boardCoordStart[i][j].getY()-(h/2)-15, p);
                    }else if(i==3){
                        canvas.drawBitmap(KegelGrün, boardCoordStart[i][j].getX()-(w/2), boardCoordStart[i][j].getY()-(h/2)-15, p);
                    }

                }
            }
        }
        // Kegel auf Ziel Feldern zeichnen
        for(int i=0;i< boardCoordZiel.length;i++){
            for(int j=0;j<boardCoordZiel[i].length;j++){
                if(boardCoordZiel[i][j]!=null && ZielFelder[i][j]!=null) {
                    canvas.drawBitmap(KegelRot, boardCoordZiel[i][j].getX()-(w/2), boardCoordZiel[i][j].getY()-(h/2)-7, p);
                }
            }
        }
        // Kegel auf Haupt Feldern zeichnen
        for(int i=0;i<boardCoordHaupt.length;i++){
            if(boardCoordHaupt[i]!=null && HauptFelder[i]!=null) {
                canvas.drawBitmap(KegelRot, boardCoordHaupt[i].getX()-(w/2), boardCoordHaupt[i].getY()-(h/2)-7, p);
            }
        }

    }

}
