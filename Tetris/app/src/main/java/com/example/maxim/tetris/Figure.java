package com.example.maxim.tetris;

import android.content.Context;

/**
 * Created by Maxim on 8/6/2016.
 */
public class Figure {

    public enum TYPE {SQUARE, T, LINE, L, REVERSED_L, Z, REVERSED_Z}
    public enum COLOR {RED, GREEN, PURPLE, ORANGE, BLUE}

    public final static boolean[][] square = {{true, true}, {true, true}};
    public final static boolean[][] tShaped = {{true, false}, {true, true}, {true, false}};
    public final static boolean[][] line = {{true}, {true}, {true}, {true}};
    public final static boolean[][] lShaped = {{false, true}, {false, true}, {true, true}};
    public final static boolean[][] reverseLShaped = {{true, true}, {false, true}, {false, true}};
    public final static boolean[][] zShaped = {{true, false}, {true, true}, {false, true}};
    public final static boolean[][] reverseZShaped = {{false, true}, {true, true}, {true, false}};

    private int  x;
    private int y;
    public int centerX;
    public int centerY;
    private Context c;

    public boolean[][] space;
    public int blockID;
    public TYPE type;
    public COLOR color;

    public Figure(int givenX, int givenY, TYPE givenType, COLOR givenColor, Context c) {
        this.x = givenX;
        this.y = givenY;
        this.type = givenType;
        this.color = givenColor;

        switch (givenType) {
            case SQUARE:
                space = square;
                break;
            case T:
                space = tShaped;
                break;
            case LINE:
                space = line;
                break;
            case L:
                space = lShaped;
                break;
            case REVERSED_L:
                space = reverseLShaped;
                break;
            case Z:
                space = zShaped;
                break;
            case REVERSED_Z:
                space = reverseZShaped;
                break;
        }

        blockID = c.getResources().getIdentifier(color.name().toLowerCase() + "_block" , "drawable", c.getPackageName());

        centerX = x + space.length / 2;
        centerY = y + space[0].length / 2;
        this.c = c;
    }

    public Figure(int givenX, int givenY, boolean[][] givenSpace,  Context c) {
        this.x = givenX;
        this.y = givenY;
        this.space = givenSpace;
        this.centerX = x + space.length / 2;
        this.centerY = y + space[0].length / 2;
        this.c = c;
    }

    public void rotateRight() {
        boolean[][] result = new boolean[space[0].length][space.length];
        for (int i = 0; i < result.length; i ++) {
            for (int j = 0; j < result[0].length; j++) {
                result [i][j] = space[j][space[0].length - 1 - i];
            }
        }
        space = result;
        x = centerX - space.length / 2;
        y = centerY - space[0].length / 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void changeXby(int offset) {
        x += offset;
        centerX +=offset;
    }

    public void changeYby(int offset) {
        y += offset;
        centerY += offset;
    }

    public Figure getRotationResult() {
        boolean[][] result = new boolean[space[0].length][space.length];
        for (int i = 0; i < result.length; i ++) {
            for (int j = 0; j < result[0].length; j++) {
                result [i][j] = space[j][space[0].length - 1 - i];
            }
        }
        int newX = centerX - result.length / 2;
        int newY = centerY - result[0].length / 2;
        return new Figure(newX, newY, result, c);
    }

    public int blockCount () {
        int  count = 0;
        for (int  i = 0; i  < space.length; i ++) {
            for (int j = 0; j < space[0].length; j ++) {
                if (space[i][j])
                    count++;
            }
        }
        return count;
    }

}
