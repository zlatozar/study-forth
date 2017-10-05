package com.jforth;

import java.io.*;
import java.util.Random;

/**
 * A Forth like language implemented in Java
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public class JForth {

    public static final Integer TRUE = new Integer(1);
    public static final Integer FALSE = new Integer(0);
    private static final String PROMPT = "\n> ";
    private static final String OK = " OK";

    // Class data

    private OStack dStack = new OStack();            // Data stack
    private OStack vStack = new OStack();            // Variable stack
    private WordsList dictionary = new WordsList();

    private boolean compiling;
    private int base;

    private StreamTokenizer st = null;
    private NonPrimitiveWord wordBeingDefined = null;
    private Random random;

    // List of Words
    private BaseWord[] forthWords = {

            new PrimitiveWord("(", true, new ExecuteIF() {

                public int execute(OStack dStack, OStack vStack) {

                    // Comment token found. Consume all tokens up to and including closing )

                    // Get the next input token
                    String token = getNextToken();

                    while ((token != null) && (!token.equals(")")))
                        token = getNextToken();

                    if (token != null) {
                        return 1;

                    } else {
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("'", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    // Ignore if compiling as next word will already be
                    // placed in the word being defined definition
                    if (compiling) {
                        return 1;
                    }

                    // Get the name of the next word
                    String name = getNextToken();
                    if (name == null) {
                        return 0;
                    }

                    BaseWord bw = null;
                    try {
                        bw = dictionary.search(name);

                    } catch (Exception ignore) {
                        // NOP
                    }

                    if (bw != null) {
                        // Found the word push address on stack
                        dStack.push(bw);
                        return 1;

                    } else {
                        System.out.println("\' - word after tick not found");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("execute", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o = dStack.pop();

                    if (o instanceof BaseWord) {
                        BaseWord bw = (BaseWord) o;

                        return bw.execute(dStack, vStack);

                    } else {
                        System.out.println("execute - requires word on stack");
                        return 0;
                    }
                }
            }),

            // Flow control words

            new PrimitiveWord("if", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    int currentIndex = wordBeingDefined.getNextWordIndex();

                    IfControlWord ifcw = new IfControlWord(currentIndex);

                    wordBeingDefined.addWord(ifcw);

                    // Push execute address of if word onto variable stack
                    vStack.push(ifcw);

                    return 1;
                }
            }),

            new PrimitiveWord("then", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    if (vStack.empty()) {
                        return 0;
                    }

                    // Pop object off of the variable stack
                    Object o = vStack.pop();

                    // Get the index of the next free slot in the non-primitive being defined.
                    int thenIndex = wordBeingDefined.getNextWordIndex();

                    // What type of control word does o represent ?
                    if (o instanceof ElseControlWord) {
                        // We had a previous else
                        ((ElseControlWord) o).setThenIndexIncrement(thenIndex);

                        // Pop variable stack again to find if
                        o = vStack.pop();
                    }

                    if (o instanceof IfControlWord) {
                        // We had a previous if. Set the then index into the if control word.
                        ((IfControlWord) o).setThenIndex(thenIndex);

                    } else {
                        System.out.println("then - requires previous if or else");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("else", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    if (vStack.empty()) {
                        return 0;
                    }

                    Object o = vStack.peek();

                    if (o instanceof IfControlWord) {

                        // Get the index past where else will be
                        int elseIndex = wordBeingDefined.getNextWordIndex() + 1;

                        // Instantiate the else runtime code passing the  index following else
                        ElseControlWord ecw = new ElseControlWord(elseIndex);

                        wordBeingDefined.addWord(ecw);
                        vStack.push(ecw);

                        // Inform the if control word of this index as well
                        ((IfControlWord) o).setElseIndex(elseIndex);

                    } else {
                        System.out.println("else - requires previous if");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("do", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    DoLoopControlWord dlcw = new DoLoopControlWord();
                    wordBeingDefined.addWord(dlcw);

                    // Push index of the next non-primitive word onto variable stack
                    int index = wordBeingDefined.getNextWordIndex();
                    vStack.push(new Integer(index));

                    return 1;
                }
            }),

            new PrimitiveWord("i", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.empty()) {
                        return 0;
                    }

                    Object o = vStack.peek();
                    dStack.push(o);

                    return 1;
                }
            }),

            new PrimitiveWord("j", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.size() < 4) {
                        return 0;
                    }

                    Object o1 = vStack.pop();
                    Object o2 = vStack.pop();
                    Object o3 = vStack.peek();

                    dStack.push(o3);

                    vStack.push(o2);
                    vStack.push(o1);

                    return 1;
                }
            }),

            new PrimitiveWord("leave", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    if (vStack.size() < 2) {
                        return 0;
                    }

                    LeaveLoopControlWord llcw = new LeaveLoopControlWord();
                    wordBeingDefined.addWord(llcw);

                    return 1;
                }
            }),

            new PrimitiveWord("loop", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    if (vStack.empty()) {
                        return 0;
                    }

                    // Pop the index entry off of the variable stack
                    Object o = vStack.pop();

                    if (!(o instanceof Integer)) {
                        System.out.println("loop - requires numeric stack entry");
                        return 0;
                    }

                    int beginIndex = ((Integer) o).intValue();
                    int endIndex = wordBeingDefined.getNextWordIndex();
                    int increment = beginIndex - endIndex;

                    LoopControlWord lcw = new LoopControlWord(increment);
                    wordBeingDefined.addWord(lcw);

                    return 1;
                }
            }),

            new PrimitiveWord("+loop", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    if (vStack.empty()) {
                        return 0;
                    }

                    // Pop the index entry off of the variable stack
                    Object o = vStack.pop();

                    if (!(o instanceof Integer)) {
                        System.out.println("loop - requires numeric stack entry");
                        return 0;
                    }

                    int beginIndex = ((Integer) o).intValue();
                    int endIndex = wordBeingDefined.getNextWordIndex();
                    int increment = beginIndex - endIndex;

                    PlusLoopControlWord plcw = new PlusLoopControlWord(increment);
                    wordBeingDefined.addWord(plcw);

                    return 1;
                }
            }),

            new PrimitiveWord("begin", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    // Begin word doesn't have runtime behavior
                    if (!compiling) {
                        return 1;
                    }

                    // Push index of next non-primitive word onto variable stack
                    int index = wordBeingDefined.getNextWordIndex();
                    vStack.push(new Integer(index));

                    return 1;
                }
            }),

            new PrimitiveWord("end", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (!compiling) {
                        return 1;
                    }

                    if (vStack.empty()) {
                        return 0;
                    }

                    // Pop the index entry off of the variable stack
                    Object o = vStack.pop();

                    if (!(o instanceof Integer)) {
                        System.out.println("end - requires numeric stack entry");
                        return 0;
                    }

                    int beginIndex = ((Integer) o).intValue();
                    int endIndex = wordBeingDefined.getNextWordIndex();
                    int increment = beginIndex - endIndex;

                    EndLoopControlWord ecw = new EndLoopControlWord(increment);
                    wordBeingDefined.addWord(ecw);

                    return 1;
                }
            }),

            // Do the stack manipulation words

            new PrimitiveWord("dup", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o = dStack.peek();
                    dStack.push(o);

                    return 1;
                }
            }),

            new PrimitiveWord("drop", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    dStack.pop();

                    return 1;
                }
            }),

            new PrimitiveWord("swap", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    dStack.push(o1);
                    dStack.push(o2);

                    return 1;
                }
            }),

            new PrimitiveWord("over", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    dStack.push(o2);
                    dStack.push(o1);
                    dStack.push(o2);

                    return 1;
                }
            }),

            new PrimitiveWord("rot", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 3) {
                        return 0;
                    }

                    Object o3 = dStack.pop();
                    Object o2 = dStack.pop();
                    Object o1 = dStack.pop();

                    dStack.push(o2);
                    dStack.push(o3);
                    dStack.push(o1);

                    return 1;
                }
            }),

            new PrimitiveWord("depth", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    Integer i = new Integer(dStack.size());
                    dStack.push(i);

                    return 1;
                }
            }),

            // Comparison Words

            new PrimitiveWord("<", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o2 = dStack.pop();
                    Object o1 = dStack.pop();

                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {

                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        if (i1 < i2) {
                            dStack.push(TRUE);

                        } else {
                            dStack.push(FALSE);
                        }

                    } else if ((o1 instanceof String) && (o2 instanceof String)) {

                        String s1 = (String) o1;
                        String s2 = (String) o2;

                        int result = s1.compareTo(s2);

                        if (result < 0) {
                            dStack.push(TRUE);

                        } else {
                            dStack.push(FALSE);
                        }

                    } else {
                        System.out.println("< - cannot compare items of different types");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("=", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o2 = dStack.pop();
                    Object o1 = dStack.pop();

                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {

                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        if (i1 == i2) {
                            dStack.push(TRUE);
                        } else {
                            dStack.push(FALSE);
                        }

                    } else if ((o1 instanceof String) && (o2 instanceof String)) {

                        String s1 = (String) o1;
                        String s2 = (String) o2;

                        int result = s1.compareTo(s2);

                        if (result == 0) {
                            dStack.push(TRUE);

                        } else {
                            dStack.push(FALSE);
                        }

                    } else {
                        System.out.println("< - cannot compare items of different types");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord(">", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o2 = dStack.pop();
                    Object o1 = dStack.pop();

                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {

                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        if (i1 > i2) {
                            dStack.push(TRUE);
                        } else {
                            dStack.push(FALSE);
                        }

                    } else if ((o1 instanceof String) && (o2 instanceof String)) {

                        String s1 = (String) o1;
                        String s2 = (String) o2;

                        int result = s1.compareTo(s2);

                        if (result > 0)
                            dStack.push(TRUE);
                        else
                            dStack.push(FALSE);

                    } else {
                        System.out.println("< - cannot compare items of different types");
                        return 0;
                    }
                    return 1;
                }
            }),

            new PrimitiveWord("0<", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();

                        dStack.push((i1 < 0) ? TRUE : FALSE);

                        return 1;

                    } else {
                        System.out.println("0< - tos must be numeric");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("0=", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();
                        dStack.push((i1 == 0) ? TRUE : FALSE);

                        return 1;

                    } else {
                        System.out.println("0< - tos must be numeric");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("0>", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();
                        dStack.push((i1 > 0) ? TRUE : FALSE);

                        return 1;

                    } else {
                        System.out.println("0< - tos must be numeric");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("not", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();
                        dStack.push((i1 == 0) ? TRUE : FALSE);

                        return 1;

                    } else {
                        System.out.println("0< - tos must be numeric");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("true", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    dStack.push(TRUE);
                    return 1;
                }
            }),

            new PrimitiveWord("false", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    dStack.push(FALSE);
                    return 1;
                }
            }),

            // Arithmetic Words

            new PrimitiveWord("+", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        i1 += i2;
                        dStack.push(new Integer(i1));

                    } else if ((o1 instanceof String) && (o2 instanceof String)) {
                        String s = (String) o2 + (String) o1;
                        dStack.push(s);

                    } else {
                        System.out.println("+ - cannot add items of different types");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("-", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        i2 -= i1;
                        dStack.push(new Integer(i2));

                        return 1;

                    } else {
                        System.out.println("- - cannot subtract strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("1+", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    // Determine if both are of the same type
                    if (o1 instanceof Integer) {

                        int i1 = ((Integer) o1).intValue();
                        dStack.push(new Integer(i1 + 1));

                        return 1;

                    } else {
                        System.out.println("1+ - cannot increment strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("1-", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    // Determine if both are of the same type
                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();
                        dStack.push(new Integer(i1 - 1));

                        return 1;

                    } else {
                        System.out.println("1- - cannot decrement strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("2+", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    // Determine if both are of the same type
                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();
                        dStack.push(new Integer(i1 + 2));

                        return 1;

                    } else {
                        System.out.println("2+ - cannot increment strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("2-", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    // Determine if both are of the same type
                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();
                        dStack.push(new Integer(i1 - 2));

                        return 1;

                    } else {
                        System.out.println("2- - cannot decrement strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("*", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        i2 *= i1;
                        dStack.push(new Integer(i2));

                        return 1;

                    } else {
                        System.out.println("* - cannot multiply strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("/", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        i2 /= i1;
                        dStack.push(new Integer(i2));

                        return 1;

                    } else {
                        System.out.println("/ - cannot divide strings");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("mod", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        int i3 = i2 % i1;
                        dStack.push(new Integer(i3));

                    } else {
                        System.out.println("mod - only works with numeric items");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("max", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        i2 = Math.max(i1, i2);
                        dStack.push(new Integer(i2));

                    } else if ((o1 instanceof String) && (o2 instanceof String)) {

                        String s1 = (String) o1;
                        String s2 = (String) o2;

                        s2 = (s1.compareTo(s2) > 0) ? s1 : s2;
                        dStack.push(s2);

                    } else {
                        System.out.println("max - only works items of the same type");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("min", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        i2 = Math.min(i1, i2);
                        dStack.push(new Integer(i2));

                    } else if ((o1 instanceof String) && (o2 instanceof String)) {

                        String s1 = (String) o1;
                        String s2 = (String) o2;

                        s2 = (s1.compareTo(s2) < 0) ? s1 : s2;
                        dStack.push(s2);

                    } else {
                        System.out.println("min - only works items of the same type");
                        return 0;
                    }
                    return 1;
                }
            }),

            new PrimitiveWord("abs", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    // Numeric argument ?
                    Object o1 = dStack.pop();

                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();

                        i1 = Math.abs(i1);
                        dStack.push(new Integer(i1));

                    } else {
                        System.out.println("abs - only works on numeric items");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("and", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        int i3 = i2 & i1;
                        dStack.push(new Integer(i3));

                    } else {
                        System.out.println("and - only works with numeric items");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("or", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        int i3 = i2 | i1;
                        dStack.push(new Integer(i3));

                    } else {
                        System.out.println("or - only works with numeric items");
                        return 0;
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("xor", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.size() < 2) {
                        return 0;
                    }

                    Object o1 = dStack.pop();
                    Object o2 = dStack.pop();

                    // Determine if both are of the same type
                    if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                        int i1 = ((Integer) o1).intValue();
                        int i2 = ((Integer) o2).intValue();

                        int i3 = i2 ^ i1;
                        dStack.push(new Integer(i3));

                    } else {
                        System.out.println("xor - only works with numeric items");
                        return 0;
                    }

                    return 1;
                }
            }),

            // Terminal Input and Output Words

            new PrimitiveWord(".", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    String outStr = "";
                    Object o = dStack.pop();

                    if (o instanceof Integer) {
                        outStr = Integer.toString(((Integer) o).intValue(), base).toUpperCase();

                    } else if (o instanceof String) {
                        outStr = (String) o;

                    } else if (o instanceof BaseWord) {
                        outStr = "BaseWord address on stack";
                    }

                    System.out.print(outStr);
                    return 1;
                }
            }),

            new PrimitiveWord("cr", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    System.out.println();
                    return 1;
                }
            }),

            new PrimitiveWord("spaces", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o1 = dStack.pop();

                    if (o1 instanceof Integer) {
                        int i1 = ((Integer) o1).intValue();

                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < i1; i++) {
                            sb.append(" ");
                        }

                        System.out.print(sb.toString());
                        return 1;

                    } else {
                        System.out.println("spaces - requires number on stack");
                        return 0;
                    }
                }
            }),

            // Numeric Conversion Words

            new PrimitiveWord("binary", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    base = 2;
                    return 1;
                }
            }),

            new PrimitiveWord("decimal", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    base = 10;
                    return 1;
                }
            }),

            new PrimitiveWord("hex", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    base = 16;
                    return 1;
                }
            }),

            new PrimitiveWord(":", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    compiling = true;

                    // Get the name of the new word
                    String name = getNextToken();
                    if (name == null) {
                        return 0;
                    }

                    // Start its definition
                    wordBeingDefined = new NonPrimitiveWord(name);

                    return 1;
                }
            }),

            new PrimitiveWord(";", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    compiling = false;

                    // Now add the new word to the dictionary
                    dictionary.add(wordBeingDefined);

                    return 1;
                }
            }),

            new PrimitiveWord("words", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    System.out.println(dictionary.toString(false));

                    return 1;
                }
            }),

            new PrimitiveWord("wordsd", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    System.out.println(dictionary.toString(true));

                    return 1;
                }
            }),

            new PrimitiveWord("forget", true, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    // Get the name of the word to forget
                    String name = getNextToken();

                    if (name == null) {
                        return 0;
                    }

                    // Look up the word in the dictionary
                    BaseWord bw = null;

                    try {
                        bw = dictionary.search(name);

                    } catch (Exception ignore) {
                        // NOP
                    }

                    if (bw != null) {

                        // Found the word to forget. Can only forget non-primitive words.
                        if (!bw.isPrimitive) {
                            // Truncate the dictionary
                            dictionary.truncateList(bw);

                        } else {
                            System.out.println("Forget - cannot forget primitives");
                        }

                    } else
                        System.out.println("Forget - didn't find word \"" + name + "\" to forget");

                    return 1;
                }
            }),

            new PrimitiveWord("constant", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    // Get the name of the new constant
                    String name = getNextToken();

                    if (name == null) {
                        return 0;
                    }

                    // Start the definition of the new constant
                    NonPrimitiveWord constant = new NonPrimitiveWord(name);

                    // Add this word to the dictionary
                    dictionary.add(constant);

                    // Now determine what type of constant is required.
                    // Pop item off of stack. This is the value of the constant.
                    Object o1 = dStack.pop();

                    if (o1 instanceof String) {
                        String stringConstant = (String) o1;
                        constant.addWord(new StringLiteral(stringConstant));

                    } else {
                        Integer numericConstant = (Integer) o1;
                        constant.addWord(new NumericLiteral(numericConstant));
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("variable", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    // Get the name of the new constant
                    String name = getNextToken();

                    if (name == null) {
                        return 0;
                    }

                    // Start the definition of the new constant
                    StorageWord sw = new StorageWord(name, 1);

                    // Add this storage word to the dictionary
                    dictionary.add(sw);

                    return 1;
                }
            }),

            new PrimitiveWord(">r", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o = dStack.pop();
                    vStack.push(o);

                    return 1;
                }
            }),

            new PrimitiveWord("r>", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.empty()) {
                        return 0;
                    }

                    Object o = vStack.pop();
                    dStack.push(o);

                    return 1;
                }
            }),

            new PrimitiveWord("r@", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.empty()) {
                        return 0;
                    }

                    Object o = vStack.peek();
                    dStack.push(o);

                    return 1;
                }
            }),

            new PrimitiveWord("!", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.empty()) {
                        System.out.println("Return stack empty");
                        return 0;
                    }

                    // Pop the top item off of the variable stack and see
                    // if it is a reference to a StorageWord which it must be.
                    Object o = vStack.pop();

                    if (!(o instanceof StorageWord)) {
                        System.out.println("Data found on return stack");
                        return 0;
                    }

                    StorageWord sw = (StorageWord) o;

                    // Are we dealing with an array here
                    if (!sw.isArray()) {

                        // We have a simple variable. Make sure we have at least one element on the data stack
                        if (dStack.empty()) {
                            return 0;
                        }

                        Object data = dStack.pop();
                        sw.store(vStack, data, 0);

                    } else {

                        // We have an array. Make sure there is data and an offset on the stack.
                        if (dStack.size() < 2) {
                            return 0;
                        }

                        // Offset should be on the top of the stack
                        Object off = dStack.pop();

                        if (!(off instanceof Integer)) {
                            System.out.println("Offset number be an integer");
                            return 0;
                        }

                        int offset = ((Integer) off).intValue();

                        // Now pop the data item from the stack
                        Object data = dStack.pop();

                        sw.store(vStack, data, offset);
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("+!", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.empty()) {
                        System.out.println("Return stack empty");
                        return 0;
                    }

                    // Pop the top item off of the variable stack and see
                    // if it is a reference to a StorageWord which it must be.
                    Object o = vStack.pop();
                    if (!(o instanceof StorageWord)) {
                        System.out.println("Data found on return stack");
                        return 0;
                    }

                    StorageWord sw = (StorageWord) o;

                    // Are we dealing with an array here
                    if (!sw.isArray()) {

                        // We have a simple variable. Make sure we have at least one element on the data stack
                        if (dStack.empty()) {
                            return 0;
                        }

                        Object data = dStack.pop();
                        sw.plusStore(vStack, data, 0);

                    } else {

                        // We have an array. Make sure there is data and an offset on the stack.
                        if (dStack.size() < 2) {
                            return 0;
                        }

                        // Offset should be on the top of the stack
                        Object off = dStack.pop();
                        if (!(off instanceof Integer)) {
                            System.out.println("Offset number be an integer");
                            return 0;
                        }

                        int offset = ((Integer) off).intValue();

                        // Now pop the data item from the stack
                        Object data = dStack.pop();

                        sw.plusStore(vStack, data, offset);
                    }

                    return 1;
                }
            }),

            new PrimitiveWord("@", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (vStack.empty()) {
                        System.out.println("Variable stack empty");
                        return 0;
                    }

                    // Pop the top item off of the variable stack and see
                    // if it is a reference to a StorageWord which it must be.
                    Object o = vStack.pop();

                    if (!(o instanceof StorageWord)) {
                        System.out.println("Data found on variable stack");
                        return 0;
                    }

                    StorageWord sw = (StorageWord) o;
                    Object data;

                    // Are we dealing with an array here
                    if (!sw.isArray()) {

                        // We have a simple variable. Fetch its value
                        data = sw.fetch(vStack, 0);

                    } else {

                        // We have an array. Make sure there is an offset on the stack.
                        if (dStack.empty()) {
                            return 0;
                        }

                        // Offset should be on the top of the stack
                        Object off = dStack.pop();

                        if (!(off instanceof Integer)) {
                            System.out.println("Offset number be an integer");
                            return 0;
                        }

                        int offset = ((Integer) off).intValue();

                        // Fetch the data at the specified offset
                        data = sw.fetch(vStack, offset);
                    }

                    dStack.push(data);
                    return 1;
                }
            }),

            new PrimitiveWord("array", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o = dStack.pop();

                    if (!(o instanceof Integer)) {
                        System.out.println("array - required int size");
                        return 0;
                    }

                    int size = ((Integer) o).intValue();

                    // Get the name of the new constant
                    String name = getNextToken();

                    if (name == null) {
                        return 0;
                    }

                    // Start the definition of the new constant
                    StorageWord sw = new StorageWord(name, size);

                    // Add this storage word to the dictionary
                    dictionary.add(sw);

                    return 1;
                }
            }),

            new PrimitiveWord("load", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    // Pop filename off of stack
                    Object o1 = dStack.pop();

                    if (o1 instanceof String) {
                        String fileName = (String) o1;

                        // Attempt to load the text in the specified file
                        return fileLoad(fileName);

                    } else {
                        System.out.println("load - requires filename string on stack");
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("random", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    if (dStack.empty()) {
                        return 0;
                    }

                    Object o = dStack.pop();

                    if (o instanceof Integer) {
                        int mult = ((Integer) o).intValue();

                        double number = random.nextGaussian() * mult;
                        dStack.push(new Integer((int) number));

                        return 1;

                    } else {
                        System.out.println("random - requires numeric value on stack");
                        return 0;
                    }
                }
            }),

            // This isn't correct
            new PrimitiveWord("key", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    try {
                        int inChar = System.in.read();
                        dStack.push(new Integer(inChar));

                        return 1;

                    } catch (Exception e) {
                        return 0;
                    }
                }
            }),

            new PrimitiveWord("bye", false, new ExecuteIF() {
                public int execute(OStack dStack, OStack vStack) {

                    System.exit(1);
                    return 1;
                }
            }),
    };
    public JForth() {

        // Add all of the defined words to the dictionary
        for (int i = 0; i < forthWords.length; i++) {
            dictionary.add(forthWords[i]);
        }

        // Initially in run mode not compile mode
        compiling = false;

        // Number base is 10 until changed
        base = 10;

        // Seed the random number generator
        random = new Random();
    }

    public static void main(String[] args) {
        JForth forth = new JForth();
        forth.outerInterpreter();
    }

    // Helper methods

    private String getNextToken() {

        // Attempt to retrieve the next token from the input
        try {
            if (st.nextToken() != StreamTokenizer.TT_EOF)
                return st.sval;
            else
                return null;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private Integer parseNumber(String word) {

        // Attempt to convert word to a number using the current base
        boolean isNumber = false;
        int number = 0;
        try {
            number = Integer.parseInt(word, base);
            isNumber = true;

        } catch (NumberFormatException nfe) {
            // NOP
        }

        if (isNumber) {
            return new Integer(number);

        } else {
            return null;
        }
    }

    private boolean interpretLine(String text) {

        try {
            // Create a tokenizer to look at each word individually
            StringReader sr = new StringReader(text);
            st = new StreamTokenizer(sr);

            // Configure the tokenizer carefully
            st.resetSyntax();
            st.wordChars('!', 'z');
            st.quoteChar('"');
            st.whitespaceChars('\u0000', '\u0020');

            // Parse first token
            int tokenType = st.nextToken();

            // For each word typed in do ...
            while (tokenType != StreamTokenizer.TT_EOF) {

                // Get a word to examine
                String word = st.sval;

                // Which mode are we in ?
                if (!compiling) {

                    // In interactive mode
                    if (tokenType == '"') {

                        // We have a string constant
                        dStack.push(word);
                        tokenType = st.nextToken();

                        continue;
                    }

                    // Word was not a string constant
                    // Attempt to look up the specified word in the dictionary
                    BaseWord bw = dictionary.search(word);

                    if (bw != null) {

                        // Found the word, execute it
                        if (bw.execute(dStack, vStack) == 0) {

                            // An error occurred in its execution
                            System.out.println(word + " - word execution or stack error");

                            return false;
                        }

                    } else {

                        // Word was not found. See if it is a number.
                        Integer num = parseNumber(word);

                        if (num != null) {
                            // Yes it was a number. Push it onto the stack
                            dStack.push(num);

                        } else {
                            // If word was not a string constant, a word or a
                            // number it must be an error
                            System.out.println(word + " ?");

                            return false;
                        }
                    }

                } else {
                    // In compile mode

                    if (tokenType == '"') {

                        // We have a string constant
                        wordBeingDefined.addWord(new StringLiteral(word));
                        tokenType = st.nextToken();

                        continue;
                    }

                    // Word was not a string constant.
                    // Attempt to look up the specified word in the dictionary
                    BaseWord bw = dictionary.search(word);

                    if (bw != null) {

                        // Found the word
                        if (bw.immediate) {
                            // Word was immediate, execute it now
                            bw.execute(dStack, vStack);

                        } else {
                            // Word wasn't immediate, add it to the word being defined.
                            wordBeingDefined.addWord(bw);
                        }

                    } else {

                        // Word was not found. See if it is a number.
                        Integer num = parseNumber(word);

                        if (num != null) {
                            // Yes it was a number. Add it as literal
                            wordBeingDefined.addWord(new NumericLiteral(num));

                        } else {

                            // If word was not a string constant, a word or a number it must be an error
                            System.out.println(word + " ?");
                            compiling = false;

                            return false;
                        }
                    }
                }

                tokenType = st.nextToken();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int fileLoad(String fileName) {

        // See if the specified file exists
        File f = new File(fileName);

        if (!f.exists()) {
            // File not found
            System.out.println("File: \"" + fileName + "\" not found");
            return 0;
        }

        // If we get here, a file does exist
        BufferedReader file = null;

        try {
            // Attempt to open the specified file
            FileReader fr = new FileReader(fileName);

            // Wrap in buffer so readline can be used
            file = new BufferedReader(fr);

            // Read first line of the text
            String text = file.readLine();

            // Interpret each line of the text
            while (text != null) {

                // Attempt to interpret the text input
                if (!interpretLine(text)) {
                    return 0;
                }

                // Read next line of the text
                text = file.readLine();
            }

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;

        } finally {

            try {
                file.close();

            } catch (Exception ex) {
                // NOP
            }
        }
    }

    private void outerInterpreter() {

        // Clear the data stack
        dStack.removeAllElements();

        // Wrap in stream so readline can be used
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader term = new BufferedReader(isr);

        // Do forever
        while (true) {

            System.out.print(PROMPT);

            try {
                // Read a line of input
                String input = term.readLine();

                // Attempt to interpret the line input
                if (!interpretLine(input)) {
                    // An error occurred. Clear the data stack
                    dStack.removeAllElements();

                } else {
                    System.out.println(OK);
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}


