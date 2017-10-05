package com.jforth;

/**
 * Loop Control Word Class
 * <p/>
 * Runtime word for a do loop
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class LoopControlWord extends BaseWord {

    // Class data
    private int indexIncrement;

    public LoopControlWord(int indexIncrement) {
        super("", false, false);

        // Save the index increment. Offset back to do
        this.indexIncrement = indexIncrement;
    }

    public int execute(OStack dStack, OStack vStack) {

        if (vStack.size() < 2) {
            return 0;
        }

        Object o1 = vStack.pop();     // Pop index
        Object o2 = vStack.peek();    // Peek at limit

        // Index and limit must be integer values
        if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
            int index = ((Integer) o1).intValue();
            int limit = ((Integer) o2).intValue();

            index += 1;

            // Is the loop limit reached ?
            if (index >= limit) {

                // Yes we're done. Pop limit off of variable stack and
                // return a positive one instruction increment.
                vStack.pop();

                return 1;

            } else {

                // Loop index has not been reached. Push new index value
                // and return negative instruction increment to cause
                // control to return to immediately following the do clause.
                vStack.push(new Integer(index));

                return indexIncrement;
            }

        } else {
            System.out.println("Loop requires 2 numeric stack entries");
            return 0;
        }
    }
}
