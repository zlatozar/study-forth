package com.jforth;

/**
 * Leave Loop Control Word Class
 * <p/>
 * Runtime word for a do loop
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class LeaveLoopControlWord extends BaseWord {

    public LeaveLoopControlWord() {

        super("", false, false);
    }

    public int execute(OStack dStack, OStack vStack) {

        if (vStack.size() < 2) {
            return 0;
        }

        // When leave is executed it makes index equal to the limit
        vStack.pop();                 // Pop index
        Object o2 = vStack.peek();    // Peek at limit
        vStack.push(o2);              // Push new index == limit

        return 1;
    }
}
