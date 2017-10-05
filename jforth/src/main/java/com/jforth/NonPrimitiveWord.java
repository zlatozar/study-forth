package com.jforth;

import java.util.ArrayList;

/**
 * NonPrimitive Word Class
 * <p/>
 * A non primitive word is one which is made up of primitive words and other
 * non primitive words.
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class NonPrimitiveWord extends BaseWord {

    // Class data
    private ArrayList<ExecuteIF> words = new ArrayList<ExecuteIF>();

    public NonPrimitiveWord(String name) {
        super(name, false, false);
    }

    // Add a primitive or non-primitive word to this word's definition
    public void addWord(ExecuteIF eif) {
        words.add(eif);
    }

    // Return what will be the index into the words array of the next entry.
    public int getNextWordIndex() {
        return words.size() + 1;
    }

    // Execution of a non primitive means executing each word that makes up its definition.
    public int execute(OStack dStack, OStack vStack) {

        int index = 0;
        int size = words.size();

        while (index < size) {
            ExecuteIF eif = words.get(index);

            // Execute word and return an index increment
            int increment = eif.execute(dStack, vStack);

            // An index of 0 is an error
            if (increment == 0) {
                return 0;
            }

            // Change instruction index by the increment amount
            index += increment;
        }

        return 1;
    }

    public void setImmediate() {
        immediate = true;
    }
}
