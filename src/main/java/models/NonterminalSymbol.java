/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;

/**
 *
 * @author brian
 */
public class NonterminalSymbol {
    private String symbol;
    private ArrayList<String> productions;

    public NonterminalSymbol(String symbol, ArrayList<String> productions) {
        this.symbol = symbol;
        this.productions = productions;
    }
    
    public NonterminalSymbol(String symbol) {
        this.symbol = symbol;
        this.productions = new ArrayList<>();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public ArrayList<String> getProductions() {
        return productions;
    }

    public void setProduction(ArrayList<String> productions) {
        this.productions = productions;
    }
}
