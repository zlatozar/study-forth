package com.jforth;

/**
 * +Loop Control Word Class
 * <p/>
 * Runtime word for a do loop
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class PlusLoopControlWord extends BaseWord {

    // Class data
    private int indexIncrement;

    public PlusLoopControlWord(int indexIncrement) {
        super("", false, false);

        // Save the index increment which is the offset back to "do"
        this.indexIncrement = indexIncrement;
    }

    public int execute(OStack dStack, OStack vStack) {

        if ((vStack.size() < 2) || dStack.empty()) {
            return 0;
        }

        Object o1 = vStack.pop();     // Pop index
        Object o2 = vStack.peek();    // Peek at limit
        Object o3 = dStack.pop();     // Loop increment

        // Index and limit must be integer values
        if ((o1 instanceof Integer) && (o2 instanceof Integer) && (o3 instanceof Integer)) {
            int index = ((Integer) o1).intValue();
            int limit = ((Integer) o2).intValue();
            int inc = ((Integer) o3).intValue();

            index += inc;
            boolean condition;

            if (inc >= 0) {
                condition = index >= limit;
            } else {
                condition = index <= limit;
            }

            // Is the loop limit reached ?
            if (condition) {
                // Yes we're done. Pop limit off of variable stack and
                // return a positive one instruction increment.
                vStack.pop();

                return 1;

            } else {

                // Loop index has not been reached. Push new index value
                // and return negative instruction increment to  cause
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
