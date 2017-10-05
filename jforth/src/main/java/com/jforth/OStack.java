package com.jforth;

import java.util.Stack;

/**
 * This class was created to override the generic behavior of the Stack class.
 * I had to create this class to prevent warnings when I used Stack directly.
 * OStack stands for Object Stack because Objects are what are stored there.
 * Integers, Strings or BaseWords are the only objects permitted.
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public class OStack extends Stack<Object> {

}
