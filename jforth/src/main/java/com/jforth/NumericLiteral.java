package com.jforth;

/**
 * NumericLiteral Word Class
 * <p/>
 * This word is not in the dictionary. When executed at run time it will
 * push a number (Integer) onto the data stack.
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class NumericLiteral extends BaseWord {

    // Class data
    private Integer number;

    public NumericLiteral(Integer number) {
        super("", false, false);

        this.number = number;
    }

    public int execute(OStack dStack, OStack vStack) {

        dStack.push(number);

        // Return a positive one index increment
        return 1;
    }
}
