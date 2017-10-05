package com.jforth;

/**
 * Else Control Word Class
 * <p/>
 * Runtime word for ELSE in an IF/ELSE/THEN
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class ElseControlWord extends BaseWord {

    // Class data
    private int indexFollowingElse;
    private int thenIndexIncrement;

    public ElseControlWord(int indexFollowingElse) {
        super("", false, false);

        this.indexFollowingElse = indexFollowingElse;
    }

    public void setThenIndexIncrement(int thenIndexIncrement) {
        this.thenIndexIncrement = thenIndexIncrement;
    }

    public int execute(OStack dStack, OStack vStack) {
        return thenIndexIncrement - indexFollowingElse + 1;
    }
}
