package io.jenkins.plugins.sample;

public class BoardElement {     
    public String label;    
    public double value;    
				       
    public BoardElement(String label, double value) {    
	this.label = label;            
	this.value = value;            
    }

    public String getLabel() {
	return label;
    }

    public double getValue() {
	return value;
    }
}
