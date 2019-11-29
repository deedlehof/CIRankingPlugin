package io.jenkins.plugins.sample;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.lang.StringBuilder;

//ScoreBoards keep the top 'length' number of results
public class ScoreBoard {
    
    private int length;
    public final int maxLength;
    private LinkedList<BoardElement> elements;

    public ScoreBoard(int maxLength) {
	length = 0;
	this.maxLength = maxLength;
	elements = new LinkedList<BoardElement>();
    }

    public boolean insert(String label, double value) {
	//Returns true if value was added, false otherwise
	
	//Check if value is greater than any current elements
	//If so, insert it
	for (int i = 0; i < length; i += 1) {
	    if (elements.get(i).value < value) {
		elements.add(i, new BoardElement(label, value));
		length += 1;
		//If list is too long now, remove lowest element
		if (length > maxLength) {
		    elements.removeLast();
		    length -= 1;
		}
		return true;
	    }
	}
	//If there is room left in the list add the element
	if (length < maxLength) {
	    elements.add(length, new BoardElement(label, value));
	    length += 1;
	    return true;
	}
	return false;
    }

    public List<BoardElement> getBoardElements() {
	return elements;
    }

    public Map<String, Double> getElemsAsMap() {
	Map<String, Double> mapElems = new HashMap<String, Double>();
	for (BoardElement be: elements) {
	    mapElems.put(be.label, be.value);
	}
	return mapElems;
    }

    public int getLength() {
	return length;
    }

    public String toString() {
	StringBuilder result = new StringBuilder();
	for (int i = 0; i < length; i += 1) {
	    BoardElement be = elements.get(i);
	    result.append(be.label);
	    result.append("  \t");
	    result.append(be.value);
	    result.append("\n");
	}
	return result.toString();
    }

}
