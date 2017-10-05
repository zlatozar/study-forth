package com.jforth;

/**
 * End Loop Control Word Class
 * <p/>
 * Runtime word for terminating a begin/end loop
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class EndLoopControlWord extends BaseWord {

    // Class data
    private int indexIncrement;

    public EndLoopControlWord(int indexIncrement) {
        super("", false, false);

        this.indexIncrement = indexIncrement;
    }

    public int execute(OStack dStack, OStack vStack) {

        if (dStack.empty()) {
            return 0;
        }

        Object o = dStack.pop();

        if (!(o instanceof Integer)) {
            System.out.println("EndWord requires numeric stack entry");
            return 0;
        }

        if (((Integer) o) == JForth.TRUE) {

            return 1;

        } else {
            return indexIncrement;
        }
    }
}
