package com.jforth;

/**
 * Storage Word Class
 * <p/>
 * A class for defining variables and arrays
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public final class StorageWord extends BaseWord {

    // Class data
    private int size;
    private Object[] array = null;

    public StorageWord(String name, int size) {
        super(name, false, false);

        // Save incoming size which is the number of storage elements
        // This is used to check incoming offsets for valid range.
        this.size = size;

        // Allocate storage for size number of items
        array = new Object[size];
    }

    public int execute(OStack dStack, OStack vStack) {

        // Push a reference to this object onto the variable stack
        vStack.push(this);

        // Return a positive one index increment
        return 1;
    }

    public boolean isArray() {

        return (size > 1);
    }

    public Object fetch(OStack vStack, int offset) {

        offset = Math.abs(offset);

        if (size == 1) {
            offset = 0;
        }

        Object o = null;

        if (offset <= size - 1) {
            // Offset is within range, fetch the data
            o = array[offset];

        } else {
            System.out.println("@ Error - \'" + name + "\' size: " + size + " index: " + offset);
        }

        return o;
    }

    public void display(OStack vStack, int offset) {

        offset = Math.abs(offset);

        if (size == 1) {
            offset = 0;
        }

        if (offset <= size - 1) {

            // Offset is within range, fetch the data
            Object o = array[offset];

            String s;

            if (o instanceof Integer) {
                s = ((Integer) o).toString();

            } else {
                s = (String) o;
            }

            System.out.print(s);

        } else {
            System.out.println("? Error - \'" + name + "\' size: " + size + " index: " + offset);
        }
    }

    public void store(OStack vStack, Object data, int offset) {

        offset = Math.abs(offset);

        if (size == 1) {
            offset = 0;
        }

        if (offset <= size - 1) {
            // Offset is within range, store the data
            array[offset] = data;

        } else {
            System.out.println("! Error - \'" + name + "\' size: " + size + " index: " + offset);
        }
    }

    public void plusStore(OStack vStack, Object data, int offset) {

        offset = Math.abs(offset);

        if (size == 1) {
            offset = 0;
        }

        if (offset <= size - 1) {

            // Offset is within range, fetch the data
            Object o1 = array[offset];
            Object o2 = data;

            if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                int i1 = ((Integer) o1).intValue();
                int i2 = ((Integer) o2).intValue();

                array[offset] = new Integer(i1 + i2);

            } else if ((o1 instanceof String) && (o2 instanceof String)) {
                String s1 = (String) o1;
                String s2 = (String) o2;
                array[offset] = s1 + s2;

            } else {
                System.out.println("+! - Type mismatch in arguments");
            }

        } else {
            System.out.println("+! Error - \'" + name + "\' size: " + size + " index: " + offset);
        }
    }
}
