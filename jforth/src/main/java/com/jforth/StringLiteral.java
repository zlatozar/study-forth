package com.jforth;

/**
 * StringLiteral Word Class
 * <p/>
 * This word is not in the dictionary. When executed at run time it will
 * push a reference to the string it contains onto the data stack.
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class StringLiteral extends BaseWord {

    // Class data
    private String str;

    public StringLiteral(String str) {
        super("", false, false);

        this.str = str;
    }

    public int execute(OStack dStack, OStack vStack) {

        dStack.push(str);

        // Return a positive one index increment
        return 1;
    }
}
