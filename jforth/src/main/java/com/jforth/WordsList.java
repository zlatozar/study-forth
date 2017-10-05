package com.jforth;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * WordsList Class for mini Forth Implementation
 * <p/>
 * This is a simple linked list class for linking BaseWord objects
 * together. The list can be added to, can be searched and can be truncated
 * at a specified position.
 * <p/>
 * Written by: Craig A. Lindley
 * Last Update: 03/03/2008
 */
public class WordsList {

    // Class data
    private LinkedList<BaseWord> wordsList;

    /**
     * Words List class constructor.
     * <p/>
     * Sets the content of the list to empty
     */
    public WordsList() {
        wordsList = new LinkedList<BaseWord>();
    }

    /**
     * Determine if list is empty
     *
     * @return boolean true if list is empty, false if not empty
     */
    public boolean isEmpty() {
        return wordsList.size() == 0;
    }

    /**
     * Remove all elements from the list
     */
    public void clear() {
        wordsList.clear();
    }

    /**
     * Add a BaseWord onto the list at the end
     *
     * @param bw is the element to add to the list
     */
    public void add(BaseWord bw) {
        wordsList.add(bw);
    }

    /**
     * Converts the list into a string for display
     *
     * @return String containing the elements on the list from back
     * to front.
     */
    public String toString(boolean showDetail) {

        if (isEmpty()) {
            return "WordsList is empty\n";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("Words:\n");

        Iterator<BaseWord> i1 = wordsList.descendingIterator();

        while (i1.hasNext()) {
            BaseWord bw = i1.next();
            sb.append(bw.toString(showDetail));

            if (showDetail) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Search the list for the specified element
     *
     * @param wordName is the word being searched for
     * @return BaseWord reference if word is found or null if it wasn't
     */
    public BaseWord search(String wordName) throws Exception {

        if (isEmpty()) {
            throw new Exception("WordsList is empty");
        }

        Iterator<BaseWord> i1 = wordsList.descendingIterator();

        while (i1.hasNext()) {
            BaseWord bw = i1.next();

            if (bw.name.equals(wordName)) {
                return bw;
            }
        }

        return null;
    }

    /**
     * Truncate list at specified element. Assumes the BaseWord passed in
     * is contained in the list. It had better be.
     *
     * @param bw is the first word in the list to forget.
     *           All words from this word to the end of the list are forgotten.
     *           bw is usually found using the search method above.
     */
    public void truncateList(BaseWord bw) {

        int size = wordsList.size();
        int index = wordsList.lastIndexOf(bw);

        if (index != -1) {

            for (int i = index; i < size; i++) {
                wordsList.remove(index);
            }

        }
    }
}






