/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brian
 */
public class NonterminalSymbol {
    private String symbol;
    private ArrayList<String> productions;
    private Set<String> firstList;
    private Set<String> followingList;
    private Map<String, String> mTableAssociation;
    
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
    
    public Set<String> getFirstList() {
        return firstList;
    }

    public void setFirstList(Set<String> firstList) {
        this.firstList = firstList;
    }
    
    public Map<String, String> getmTableAssociation() {
        return mTableAssociation;
    }

    public void setmTableAssociation(Map<String, String> mTableAssociation) {
        this.mTableAssociation = mTableAssociation;
    }
    
    public Set<String> getFollowingList() {
        return followingList;
    }

    public void setFollowingList(Set<String> followingList) {
        this.followingList = followingList;
    }
}
