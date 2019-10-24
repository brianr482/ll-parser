/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 *
 * @author brian
 */
public class RecognitionRecord {
    private String stack;
    private String input;
    private String output;
    public RecognitionRecord(String stack, String input, String output) {
        this.stack = stack;
        this.input = input;
        this.output = output;
    }
    
    public RecognitionRecord(String stack, String input) {
        this.stack = stack;
        this.input = input;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
