/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ll.parser;

import java.util.ArrayList;

/**
 *
 * @author brian
 */
public class Production {
    private String symbol;
    private ArrayList<String> production;

    public Production(String symbol, ArrayList<String> production) {
        this.symbol = symbol;
        this.production = production;
    }
    
    public Production(String symbol) {
        this.symbol = symbol;
        this.production = new ArrayList<>();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public ArrayList<String> getProduction() {
        return production;
    }

    public void setProduction(ArrayList<String> production) {
        this.production = production;
    }
}
