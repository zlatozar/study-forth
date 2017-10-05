package com.jforth;

/**
 * Primitive Word Class
 * <p/>
 * A primitive word is one which contains Java code.
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class PrimitiveWord extends BaseWord {

    // Class data
    private ExecuteIF eif;

    public PrimitiveWord(String name, boolean isImmediate, ExecuteIF eif) {
        super(name, isImmediate, true);

        this.eif = eif;
    }

    public int execute(OStack dStack, OStack vStack) {
        return eif.execute(dStack, vStack);
    }
}
