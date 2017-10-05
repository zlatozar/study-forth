package com.jforth;

/**
 * If Control Word Class
 * <p/>
 * Runtime word for IF part of IF/ELSE/THEN
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class IfControlWord extends BaseWord {

    // Class data
    private int currentIndex;
    private int thenIndexIncrement;
    private int elseIndexIncrement;

    public IfControlWord(int currentIndex) {
        super("", false, false);

        this.currentIndex = currentIndex;
        thenIndexIncrement = 0;
        elseIndexIncrement = 0;
    }

    // Called by then to set its index
    public void setThenIndex(int thenIndex) {
        thenIndexIncrement = thenIndex - currentIndex;
    }

    // Called by else to set its index
    public void setElseIndex(int elseIndex) {
        elseIndexIncrement = elseIndex - currentIndex;
    }

    public int execute(OStack dStack, OStack vStack) {

        if (dStack.empty()) {
            return 0;
        }

        Object o = dStack.pop();    // Pop flag

        if (o instanceof Integer) {

            if (((Integer) o) == JForth.TRUE) {

                // Flag is true, advance instruction index by one to true portion of if
                return 1;

            } else {

                // Flag is false, advance to else if present or to then if not.
                if (elseIndexIncrement != 0) {
                    return elseIndexIncrement;

                } else {
                    return thenIndexIncrement;
                }
            }

        } else {
            System.out.println("if - requires numeric flag on stack");
            return 0;
        }
    }
}
