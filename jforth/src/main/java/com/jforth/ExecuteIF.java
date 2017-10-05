package com.jforth;

/**
 * Interface for executing primitive and non-primitive TIL words
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public interface ExecuteIF {

    int execute(OStack dataStack, OStack variableStack);

}
