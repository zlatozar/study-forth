package com.jforth;

/**
 * Do Loop Control Word Class
 * <p/>
 * Runtime word for a do loop
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class DoLoopControlWord extends BaseWord {

    public DoLoopControlWord() {

        super("", false, false);
    }

    public int execute(OStack dStack, OStack vStack) {

        if (dStack.size() < 2) {
            return 0;
        }

        // Pop index and limit off of the stack
        Object o1 = dStack.pop();    // Pop start index
        Object o2 = dStack.pop();    // Pop end index + 1

        // Index and limit must be integer values
        if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
            vStack.push(o2);         // Push limit onto the variable stack
            vStack.push(o1);         // Push index onto the variable stack

            return 1;                // Return a positive one index increment

        } else {
            System.out.println("Do requires 2 numeric stack entries");
            return 0;
        }
    }
}
