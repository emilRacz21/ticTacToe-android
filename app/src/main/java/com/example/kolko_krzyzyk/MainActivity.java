package com.example.kolko_krzyzyk;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import java.util.Date;
public class MainActivity extends AppCompatActivity {
    final int[][] WINNING_VARIANTS = {
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };
    String currentPlayer = "O";
    String dateCount;
    boolean winner = false;
    boolean isClicked = false;
    boolean playerVsPc = false;
    int occupiedCells = 0;
    ImageView cellButton;
    TextView titleText;
    int[] time = {0, 0};
    LinearLayout[] playerTheme = new LinearLayout[2];
    TextView[] currentTime = new TextView[2];
    boolean[] currentSymbol = {true, false};
    ImageView [] cells = new ImageView[9];
    LinearLayout hideAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //pobieranie parametrów ekranu.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        System.out.println(widthPixels);
        System.out.println(heightPixels);
        double screenInches= Math.sqrt(Math.pow(widthPixels / metrics.xdpi, 2) + Math.pow(heightPixels / metrics.ydpi, 2));
        setContentView(screenInches <= 5.5 ? R.layout.activity_main : R.layout.activity_main_hdpi);

        //deklaracja zmiennych
        playerTheme[0]= findViewById(R.id.crossTheme);
        playerTheme[1] = findViewById(R.id.circleTheme);
        currentTime[0] = findViewById(R.id.currentTimeX);
        currentTime[1] = findViewById(R.id.currentTimeO);
        titleText = findViewById(R.id.titleText);
        hideAll = findViewById(R.id.hideAll);
        //Metoda do pobrania buttonów.
        getCells();

        //Nasłuch na klikniete buttony.
        View.OnClickListener onCellClickListener = v -> clickBoard(v);

        //Rozpoczecie gry.
        selectGameMode();
        animate(Techniques.Flash, 10000,200,titleText );

        //Metoda do wyslanie nasluchu na przyciski do  view onCellClickListener.
        sendCells( onCellClickListener );
    }

    void clickBoard(View v){
        if (isClicked && !winner) {
            cellButton = findViewById(v.getId());
            animate(Techniques.Landing, 500, 0, cellButton);
            currentPlayer = (currentPlayer.equals("X")) ? "O" : "X";
            cellButton.setTag(currentPlayer);
            if (currentPlayer.equals("X")) {
                animate(Techniques.Wobble, 500, 0, playerTheme[1]);
                yourPlayer(false, true, R.drawable.player_previous, R.drawable.player_current, R.drawable.cross);
            } else {
                animate(Techniques.Flash, 500, 0, playerTheme[0]);
                yourPlayer(true, false, R.drawable.player_current, R.drawable.player_previous, R.drawable.tac);
            }
            if (cellButton.isClickable()) {
                cellButton.setClickable(false);
                occupiedCells++;
            }
            checkWin(WINNING_VARIANTS);
            // tryb gdy jest wlaczony player vs pc.
            if (!winner && currentPlayer.equals("X") && playerVsPc) {
                makeComputerMove();
            }
        } else {
            // Short notification when the player doesn't click the start button.
            makeToast("Press start to begin the game!");
        }
    }
    //Wybierz tryb gry
    void selectGameMode(){
        hideAll.setVisibility(View.GONE);
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.select_game_mode);
        RelativeLayout playerVsPlayer = dialog.findViewById(R.id.playervsplayer);
        animate(Techniques.Flash, 1000,1,playerVsPlayer );
        RelativeLayout pcPlayer = dialog.findViewById(R.id.playervspc);
        playerVsPlayer.setOnClickListener( view ->{
            if(!isClicked){
                startGame();
            }
            dialog.dismiss();
        });

        pcPlayer.setOnClickListener( view ->{
            startGame();
            currentTime[1].setText("N/A");
            dialog.dismiss();
            playerVsPc = true;
        });
        dialog.show();
    }
    //Komputer wykonuje ruch!
    void makeComputerMove() {
        int randomCell;
        if (occupiedCells < 9) {
            do {
                randomCell = (int) (Math.random() * 9);
            } while (cells[randomCell].getTag() != null);

            cellButton = cells[randomCell];
            animate(Techniques.Landing, 500, 0, cellButton);
            currentPlayer = "O";
            cellButton.setTag(currentPlayer);
            animate(Techniques.Flash, 500, 0, playerTheme[0]);
            yourPlayer(true, false, R.drawable.player_current, R.drawable.player_previous, R.drawable.tac);
            cellButton.setClickable(false);
            occupiedCells++;
            checkWin(WINNING_VARIANTS);
        }
    }

    //Metoda do rozpoczynania gry!
    void startGame(){
        hideAll.setVisibility(View.VISIBLE);
        countTime();
        animate(Techniques.Flash,500, 0, playerTheme[0]);
        playerTheme[0].setBackgroundResource( R.drawable.player_current );
        isClicked = true;
    }

    //Ustaw zachwaonie gracza gdy jest jego kolej na rozpoczecie ruchu.
    void yourPlayer( boolean first, boolean second,int crossBackground, int circleBackground, int currentPlayerNow ){
        currentSymbol[0] = first;
        currentSymbol[1] = second;
        playerTheme[0].setBackgroundResource(crossBackground);
        playerTheme[1].setBackgroundResource(circleBackground);
        cellButton.setBackgroundResource(currentPlayerNow);
    }

    //Metoda do sprawdzania wszytskich mmożliwych kombinacji wygranej.
    void checkWin( int[][] WINNING_VARIANTS ){
        Intent restart = new Intent(MainActivity.this, MainActivity.class );
        for ( int[] winningVariant : WINNING_VARIANTS ) {
            int firstWariants = winningVariant[0];
            int secondWariants = winningVariant[1];
            int thirdWariants = winningVariant[2];

            ImageView button1 = getButtonByIndex(firstWariants);
            ImageView button2 = getButtonByIndex(secondWariants);
            ImageView button3 = getButtonByIndex(thirdWariants);

            if (button1.getTag() != null && button2.getTag() != null && button3.getTag() != null && button1.getTag().equals(currentPlayer) && button2.getTag().equals(currentPlayer) && button3.getTag().equals(currentPlayer)) {
                if(playerVsPc){
                    if(currentPlayer.equals("X")) customDialog("Player " + currentPlayer + " win this game \nYour time  \n" + dateCount, restart);
                        else customDialog("Player " + currentPlayer + " win this game", restart);
                }else customDialog("Player " + currentPlayer + " win this game \nYour time  \n" + dateCount, restart);
                winner = true;
                stopTime();
            } else if (occupiedCells == 9 && !winner) {
                stopTime();
                customDialog("Game over", restart);
            }
        }
    }
    //Zatrzymaj czas gdy wygrana lub przegrana.
    void stopTime(){
        currentSymbol[0] = false;
        currentSymbol[1] = false;
    }
    //Metoda do sprawdzania czasu X i Y
    void countTime() {
        Handler timeNow = new Handler();
        timeNow.post(new Runnable() {
            @Override
            public void run() {
                if (currentSymbol[0]) {
                    updateAndDisplayTime(time[0], currentTime[0]);
                    time[0] += 1000;
                }
                if (currentSymbol[1]) {
                    updateAndDisplayTime(time[1], currentTime[1]);
                    time[1] += 1000;
                }
                timeNow.postDelayed(this, 1000);
            }
        });
    }

    void updateAndDisplayTime(int seconds, TextView textView) {
        Date date = new Date(seconds);
        dateCount = String.format("%tM:%tS", date, date);
        textView.setText(dateCount);
    }

    // Sprawdz INDEX buttonów.
    ImageView getButtonByIndex(int index) {
        if (index >= 0 && index < cells.length) {
            return cells[index];
        } else {
            return null;
        }
    }
    //Pobierz buttony z activity_main.
    void getCells() {
        int[] img = {
                R.id.cell1, R.id.cell2, R.id.cell3,
                R.id.cell4, R.id.cell5, R.id.cell6,
                R.id.cell7, R.id.cell8, R.id.cell9
        };
        for ( int i = 0 ; i < cells.length ; i ++) cells[i] = findViewById(img[i]);
    }
    //Wyslij buttony do nasluchu klikniecia do view onCellClickListener.
    void sendCells( View.OnClickListener onCellClickListener ) {
        for (ImageView cell : cells) cell.setOnClickListener(onCellClickListener);
    }
    //Custom dialog po wygranej lub przegranej grze.
    @SuppressLint("SetTextI18n")
    void customDialog( String text , Intent cutomIntent ){
        Dialog resultdialog = new Dialog(this );
        resultdialog.setContentView( R.layout.dialog );

        resultdialog.setCancelable( false );
        TextView textWinner = resultdialog.findViewById( R.id.winner );
        textWinner.setText( text + "\n\nRestart?" );
        Button restart = resultdialog.findViewById( R.id.restartBoard );
        YoYo.with(Techniques.BounceInUp).duration(1000).repeat(1).playOn(restart);
        restart.setOnClickListener(view ->{
            startActivity( cutomIntent );
            resultdialog.dismiss();
        });
        resultdialog.show();
    }
    //Zrób powiadomienie toast.
    void makeToast( String content ){
        Toast.makeText(this,content, Toast.LENGTH_SHORT ).show();
    }
    //Animacje.
    void animate(Techniques name, int duration, int repeat, View v){YoYo.with(name).duration(duration).repeat(repeat).playOn(v);}
}
