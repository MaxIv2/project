package com.example.maxim.tetris;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.os.Handler;
import android.widget.TextView;

import java.util.Random;

public class Game extends AppCompatActivity {

    int comboCount;

    RelativeLayout gameHolder;
    ImageView[][] grid;
    ImageView[] nextPresentation = new ImageView[3];;
    Button rotateRightButton;
    Button dropButton;
    TextView scoreHolder;
    Button pauseButton;

    int score;
    int cellWidth;
    int cellHeight;
    int holderWidth;
    int holderHeight;
    int topOffSet;
    final int rowCount = 20;
    final int columnCount = 10;

    final int regularTimeDelay = 500;
    final int dropTimeDelay = 50;
    boolean dropMode;

    boolean pausedOnce;
    boolean menuIsOpened;

    Figure fallingFigure;
    Figure[] nextFigures = new Figure[3];

    final Figure.TYPE[] typeValues = Figure.TYPE.values();
    final Figure.COLOR[] colorValues = Figure.COLOR.values();
    Random random;

    final Handler handler = new Handler();

    final Runnable fall = new Runnable() {
        @Override
        public void run() {
            if (canMoveDown()) {
                moveDown();
                handler.postDelayed(this, actualTimeDelay());
            } else
                handler.postDelayed(lastMoment, regularTimeDelay);
        }
    };

    final Runnable lastMoment = new Runnable() {
        @Override
        public void run() {
            if (canMoveDown())
                handler.postDelayed(fall, 0);
            else {
                checkRows();
                play();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        dropMode = false;
        nextPresentation[0] = (ImageView) findViewById(R.id.next0);
        nextPresentation[1] = (ImageView) findViewById(R.id.next1);
        nextPresentation[2] = (ImageView) findViewById(R.id.next2);

        pausedOnce = false;
        menuIsOpened = false;

        comboCount = 0;

        scoreHolder = (TextView) findViewById(R.id.score_holder);

        random = new Random();

        rotateRightButton = (Button) findViewById(R.id.rotate_right_btn);
        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateRight();
            }
        });


        pauseButton = (Button) findViewById(R.id.pause_btn);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause();
            }
        });

        dropButton = (Button) findViewById(R.id.drop_btn);
        dropButton.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    dropMode = true;
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
                    dropMode = false;
                return false;
            }
        });

        gameHolder = (RelativeLayout) findViewById(R.id.game_block);
        gameHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if (motionEvent.getX() >= holderWidth / 2) {
                        if (canMoveRight())
                            moveRight();
                    } else {
                        if (canMoveLeft())
                            moveLeft();
                    }
                }
                return false;
            }
        });

        ViewTreeObserver viewTreeObserver = gameHolder.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    gameHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    cellWidth = gameHolder.getWidth() / columnCount;
                    cellHeight = gameHolder.getHeight() / rowCount;
                    holderWidth = gameHolder.getWidth();
                    holderHeight = gameHolder.getHeight();
                    System.out.println();
                    grid = new ImageView[columnCount][rowCount];
                    topOffSet = holderHeight % cellWidth;
                    intializeGrid();
                    intializeNextFigures();
                    play();
                }
            });
        }

        score = 0;

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(fall);
        handler.removeCallbacks(lastMoment);
        pausedOnce = true;
    }

    public void pause() {
        Intent intent = new Intent(this, Pause.class);
        this.startActivityForResult(intent, Pause.REQ_CODE);
        menuIsOpened = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!menuIsOpened && pausedOnce)
            pause();
        else
            menuIsOpened = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Pause.REQ_CODE) {
            if(resultCode == RESULT_OK){
                int chosenAction = data.getIntExtra(Pause.EXTRA_CHOICE, Pause.RESUME);
                switch (chosenAction) {
                    case Pause.RESUME:
                        handler.postDelayed(fall, regularTimeDelay);
                        break;
                    case Pause.RESTART:
                        restart();
                        break;
                    case Pause.MENU:
                        finish();
                }
            }
        }
    }

    public void intializeGrid() {
        for (int i = 0; i < grid.length; i++) {
            for (int  j = 0 ; j < grid[0].length; j ++) {
                grid[i][j] = new ImageView(this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(cellWidth, cellHeight);
                params.topMargin = topOffSet + j * cellHeight;
                params.leftMargin = i * cellWidth;
                params.rightMargin = holderWidth - cellWidth * (i + 1);
                params.bottomMargin = holderHeight - cellWidth - topOffSet - params.topMargin;
                grid[i][j].setLayoutParams(params);
                grid[i][j].setVisibility(View.INVISIBLE);
                grid[i][j].setScaleType(ImageView.ScaleType.FIT_XY);
                grid[i][j].setAdjustViewBounds(true);
                gameHolder.addView(grid[i][j], params);
            }
        }
    }

    public void play() {
        if (isGameOver()) {
            gameOver();
        } else {
            spawnNextFigure();
            generateNextFigure();
            handler.postDelayed(fall, actualTimeDelay());
        }
    }

    public void generateNextFigure() {
        String resourceName;
        for(int i = 0; i < nextFigures.length - 1; i ++) {
            nextFigures[i] = nextFigures[i + 1];
            resourceName = nextFigures[i].color.name().toLowerCase() + "_";
            resourceName += nextFigures[i].type.name().toLowerCase();
            nextPresentation[i].setImageResource(getResources().getIdentifier(resourceName , "drawable", getPackageName()));
        }
        nextFigures[nextFigures.length - 1] = new Figure(columnCount / 2, 0, typeValues[random.nextInt(typeValues.length)], colorValues[random.nextInt(colorValues.length)], this);
        resourceName = nextFigures[nextFigures.length - 1].color.name().toLowerCase() + "_";
        resourceName += nextFigures[nextFigures.length - 1].type.name().toLowerCase();
        nextPresentation[nextFigures.length - 1].setImageResource(getResources().getIdentifier(resourceName , "drawable", getPackageName()));
    }

    public void intializeNextFigures() {
        String resourceName;
        for(int i = 0; i < nextFigures.length; i++) {
            nextFigures[i] = new Figure(columnCount / 2, 0, typeValues[random.nextInt(typeValues.length)], colorValues[random.nextInt(colorValues.length)], this);
            resourceName = nextFigures[i].color.name().toLowerCase() + "_";
            resourceName += nextFigures[i].type.name().toLowerCase();
            nextPresentation[i].setImageResource(getResources().getIdentifier(resourceName , "drawable", getPackageName()));
        }
    }

    public void spawnNextFigure() {
        fallingFigure = nextFigures[0];
        for (int i = 0; i < fallingFigure.space.length; i++) {
            for (int j = 0; j < fallingFigure.space[0].length; j++) {
                if(fallingFigure.space[i][j]) {
                    grid[i + fallingFigure.getX()][j + fallingFigure.getY()].setVisibility(View.VISIBLE);
                    grid[i + fallingFigure.getX()][j + fallingFigure.getY()].setImageResource(fallingFigure.blockID);
                }
            }
        }
    }

    public void moveDown() {
        hide();
        fallingFigure.changeYby(1);
        reappear();
    }

    public void moveRight() {
        hide();
        fallingFigure.changeXby(1);
        reappear();
    }

    public void moveLeft() {
        hide();
        fallingFigure.changeXby(-1);
        reappear();
    }

    public void hide() {
        for(int i = 0; i < fallingFigure.space.length; i++) {
            for (int j = 0; j < fallingFigure.space[0].length; j++) {
                if(fallingFigure.space[i][j])
                    grid[i + fallingFigure.getX()][j + fallingFigure.getY()].setVisibility(View.INVISIBLE);
            }
        }
    }

    public void reappear() {
        for(int i = 0; i < fallingFigure.space.length; i++) {
            for (int j = 0; j < fallingFigure.space[0].length; j++) {
                if (fallingFigure.space[i][j]) {
                    grid[i + fallingFigure.getX()][j + fallingFigure.getY()].setVisibility(View.VISIBLE);
                    grid[i + fallingFigure.getX()][j + fallingFigure.getY()].setImageResource(fallingFigure.blockID);
                }
            }
        }
    }

    public boolean canMoveDown() {
        int [] bottomCoordinates = new int[fallingFigure.space.length];//y coordinates of bottom most part of figure on its 'space' matrix
        for (int i = 0; i < fallingFigure.space.length; i++) {
            for (int j = 0; j < fallingFigure.space[0].length; j++) {
                if (fallingFigure.space[i][j])
                    bottomCoordinates[i] = j;
            }
        }
        for(int i = 0; i < bottomCoordinates.length; i ++){
            if (fallingFigure.getY() + bottomCoordinates[i] + 1 >= grid[0].length)
                return false;
            if (grid[fallingFigure.getX() + i][fallingFigure.getY() + bottomCoordinates[i] + 1].getVisibility() == View.VISIBLE)
                return false;
        }
        return true;
    }

    public boolean canMoveRight() {
        int [] rightCoordinates = new int[fallingFigure.space[0].length];//x coordinates of right most parts of the figure on its 'space' matrix
        for (int i = 0; i < fallingFigure.space[0].length; i++) {
            for (int j = 0; j < fallingFigure.space.length; j++) {
                if (fallingFigure.space[j][i])
                    rightCoordinates[i] = j;
            }
        }
        for(int i = 0; i < rightCoordinates.length; i ++){
            if (fallingFigure.getX() + rightCoordinates[i] + 1 >= grid.length)
                return false;
            if (grid[fallingFigure.getX() + rightCoordinates[i] + 1][fallingFigure.getY() + i].getVisibility() == View.VISIBLE)
                return false;
        }
        return true;

    }

    public boolean canMoveLeft() {
        int [] leftCoordinates = new int[fallingFigure.space[0].length];//x coordinates of left most parts of the figure on its 'space' matrix
        for (int i = 0; i < fallingFigure.space[0].length; i++) {
            for (int j = fallingFigure.space.length - 1; j >= 0; j--) {
                if (fallingFigure.space[j][i])
                    leftCoordinates[i] = j;
            }
        }
        for(int i = 0; i < leftCoordinates.length; i ++){
            if (fallingFigure.getX() + leftCoordinates[i]  <= 0)
                return false;
            if (grid[fallingFigure.getX() + leftCoordinates[i] - 1][fallingFigure.getY() + i].getVisibility() == View.VISIBLE)
                return false;
        }
        return true;

    }

    public void checkRows() {
        int lineCleared = 0;
        boolean lineIsFull = true;
        for (int i = 0; i < grid[0].length; i ++) {
            for(int j = 0; j < grid.length && lineIsFull; j ++) {
                if(grid[j][i].getVisibility() == View.INVISIBLE) {
                    lineIsFull = false;
                }
            }
            if (lineIsFull) {
                lineCleared++;
                removeRow(i);
                pushDownUpperRows(i);
            }
            else
                lineIsFull = true;
        }
        if (lineCleared > 0)
            comboCount += 1;
        else
            comboCount = 0;
        score = ScoringSystem.newScore(comboCount, lineCleared, false, fallingFigure.blockCount(), score, 0);
        scoreHolder.setText(Integer.toString(score));
    }

    public void removeRow(int row) {
        for (int i = 0; i < grid.length; i++) {
            grid[i][row].setVisibility(View.INVISIBLE);
        }
    }

    public void pushDownUpperRows(int row) {
        for (int j = 0; j < grid.length; j ++) {
            for (int i = row; i > 0; i--) {
                if (grid[j][i - 1].getVisibility() == View.VISIBLE) {
                    grid[j][i].setVisibility(View.VISIBLE);
                    grid[j][i].setImageBitmap(((BitmapDrawable)grid[j][i - 1].getDrawable()).getBitmap());
                    grid[j][i - 1].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public boolean isGameOver() {
        int [] bottomCoordinates = new int[nextFigures[0].space.length];//y coordinates of bottom most part of next figure on its 'space' matrix
        for (int i = 0; i < nextFigures[0].space.length; i++) {
            for (int j = 0; j < nextFigures[0].space[0].length; j++) {
                if (nextFigures[0].space[i][j])
                    bottomCoordinates[i] = j;
            }
        }
        for(int i = 0; i < bottomCoordinates.length; i ++){
            if (nextFigures[0].getY() + bottomCoordinates[i] + 1 >= grid[0].length)
                return true;
            if (grid[nextFigures[0].getX() + i][nextFigures[0].getY() + bottomCoordinates[i]].getVisibility() == View.VISIBLE)
                return true;
        }
        return false;
    }

    public void gameOver() {
        final GameOverDialog dialog = new GameOverDialog(this);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (dialog.choice == GameOverDialog.RESTART){
                    restart();
                } else if (dialog.choice == GameOverDialog.MENU)
                    finish();
            }
        });
        dialog.show();
    }

    public int actualTimeDelay() {
        if (dropMode)
            return dropTimeDelay;
        else
            return regularTimeDelay;
    }

    public void rotateRight() {
        Figure rotationRes = fallingFigure.getRotationResult();
        int maxOffset = 2;
        if (isFittable(rotationRes)) {
            hide();
            fallingFigure.rotateRight();
            reappear();
        } else {
            for (int i = 0; i <= maxOffset; i++) {
                for (int j = 1; j <= maxOffset; j++) {
                    rotationRes.changeXby(i);
                    rotationRes.changeYby(j);
                    if (isFittable(rotationRes)) {
                        hide();
                        fallingFigure.rotateRight();
                        fallingFigure.changeXby(i);
                        fallingFigure.changeYby(j);
                        reappear();
                        return;
                    }
                    rotationRes.changeXby(-2 * i);
                    if (isFittable(rotationRes)) {
                        hide();
                        fallingFigure.rotateRight();
                        fallingFigure.changeXby(-i);
                        fallingFigure.changeYby(j);
                        reappear();
                        return;
                    }
                    rotationRes.changeYby(-2 * j);
                    if (isFittable(rotationRes)) {
                        hide();
                        fallingFigure.rotateRight();
                        fallingFigure.changeXby(-i);
                        fallingFigure.changeYby(-j);
                        reappear();
                        return;
                    }
                    rotationRes.changeXby(2 * i);
                    if (isFittable(rotationRes)) {
                        hide();
                        fallingFigure.rotateRight();
                        fallingFigure.changeXby(i);
                        fallingFigure.changeYby(-j);
                        reappear();
                        return;
                    }
                    rotationRes.changeXby(-i);
                    rotationRes.changeYby(+j);

                }
            }
        }
    }

    public boolean isFittable(Figure toCheck) {
        try {
            int x, y;
            for(int i = 0; i < toCheck.space.length; i++) {
                for (int j = 0; j < toCheck.space[0].length; j++) {
                    x = i + toCheck.getX();
                    y = j + toCheck.getY();
                    if (toCheck.space[i][j])
                        if (grid[x][y].getVisibility() == View.VISIBLE && !isFallingFigure(x, y))
                            return false;
                }
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

    }

    public boolean isFallingFigure(int x, int y) {
        for (int i = 0; i < fallingFigure.space.length; i ++) {
            for (int j = 0; j < fallingFigure.space[0].length; j ++) {
                if (fallingFigure.space[i][j] && x == (fallingFigure.getX() + i) && y == (fallingFigure.getY() + j))
                    return true;
            }
        }
        return false;
    }

    private void restart() {
        Intent myIntent = getIntent();
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(myIntent);
    }

    private static class ScoringSystem {
        public static int newScore(int combo, int numOfRows, boolean previousIsTetris, int blockCount, int originalScore, int level) {
            int lineClearancePoints = 0;
            switch (numOfRows) {
                case 1:
                    lineClearancePoints = 40 * (1 + level);
                    break;
                case 2:
                    lineClearancePoints = 100 * (1 + level);
                    break;
                case 3:
                    lineClearancePoints = 300 * (1 + level);
                    break;
                case 4:
                    lineClearancePoints = 1200 * (1 + level);
                    break;
            }

            if (combo > 1)
                return originalScore + lineClearancePoints + 50 * combo * level + blockCount;
            else
                return originalScore + lineClearancePoints + blockCount;
        }
    }


}
