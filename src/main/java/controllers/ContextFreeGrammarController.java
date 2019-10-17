/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import models.NonterminalSymbol;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brian
 */
public class ContextFreeGrammarController {
    /* CFG = Context-free grammar */
    public static ArrayList<NonterminalSymbol> readCFGFromFile(File file)
        throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        ArrayList<NonterminalSymbol> cfg = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] productionLine = line.split("->");
            String grammaticalSymbol = productionLine[0];
            NonterminalSymbol production = findProduction(
                cfg, grammaticalSymbol
            );
            if (production == null) {
                production = new NonterminalSymbol(grammaticalSymbol);
                cfg.add(production);
            }
            production.getProductions().add(productionLine[1]);
        }
        return cfg;
    }
    
    private static NonterminalSymbol findProduction(
        ArrayList<NonterminalSymbol> productions, String gs
    ) {
        return productions.stream()
            .filter(production -> production.getSymbol().equals(gs))
            .findFirst()
            .orElse(null);
    }
    
    public static void cleanCFG(ArrayList<NonterminalSymbol> cfg) {
        ArrayList<NonterminalSymbol> cfgClone = new ArrayList<>();
        cfg.forEach((nonterminalSymbol) -> {
            cfgClone.add(nonterminalSymbol);
        });
        cfgClone.forEach((nonterminalSymbol) -> {
            int prodIndexWithLeftRecursivity = findLeftRecursivity(
                nonterminalSymbol
            );
            if (prodIndexWithLeftRecursivity > -1) {
                removeLeftRecursivity(
                    cfg,
                    nonterminalSymbol,
                    prodIndexWithLeftRecursivity
                );
            } else {
                findLeftFactorization(cfg, nonterminalSymbol);
            }
        });
    }
    
    private static void removeLeftRecursivity(
        ArrayList<NonterminalSymbol> cfg,
        NonterminalSymbol nonterminalSymbol,
        int productionIndex
    ) {
        ArrayList<String> productions = nonterminalSymbol.getProductions();
        String production = productions.get(productionIndex);
        ArrayList<String> prods = new ArrayList<>();
        String newNTSymbol = nonterminalSymbol.getSymbol() + "'";
        prods.add(production.substring(1) + newNTSymbol);
        prods.add("&");
        cfg.add(
            cfg.indexOf(nonterminalSymbol) + 1,
            new NonterminalSymbol(newNTSymbol, prods)
        );
        productions.remove(0);
        for (int i = 0; i < productions.size(); i++) {
            String prod = productions.get(i);
            productions.set(i, prod + newNTSymbol);
        }
    }
    
    private static int findLeftRecursivity(NonterminalSymbol nonterminalSymbol) {
        int index = 0;
        ArrayList<String> productions = nonterminalSymbol.getProductions();
        while (index < productions.size()) {
            String production = productions.get(index);
            if (hasLeftRecursivity(nonterminalSymbol.getSymbol(), production)) {
                return index;
            } else {
                index++;
            }
        }
        return -1;
    }
    
    private static boolean hasLeftRecursivity(
        String nonterminalSymbol,
        String production
    ) {
        return production.startsWith(nonterminalSymbol);
    }
    
    private static void findLeftFactorization(
        ArrayList<NonterminalSymbol> cfg,
        NonterminalSymbol nonterminalSymbol
    ) {
        ArrayList<String> productions = nonterminalSymbol.getProductions();
        Map<String,ArrayList<Integer>> matchesMap = new HashMap<>();  
        productions.forEach((production) -> {
            for (int i = 0; i < production.length(); i++) {
                String prefix = production.substring(0, i + 1);
                if (!matchesMap.containsKey(prefix)) {
                    matchesMap.put(prefix, findPrefixMatches(nonterminalSymbol, prefix));
                }
            }
        });
        String greatestPrefix = null;
        int greatestPrefixMatches = 1;
        for (Map.Entry<String,ArrayList<Integer>> m : matchesMap.entrySet()) {
            int size = m.getValue().size();
            if (size > greatestPrefixMatches) {
                greatestPrefixMatches = size;
                greatestPrefix = m.getKey();
            }
        }
        if (greatestPrefix != null) {
            ArrayList<String> prods = new ArrayList<>();
            ArrayList<Integer> prodIndices = matchesMap.get(greatestPrefix);
            prodIndices.sort(Comparator.reverseOrder());
            for (int prodIndex : prodIndices) {
                String newProd = productions.get(prodIndex)
                    .substring(greatestPrefix.length());
                if (newProd.isEmpty()) {
                    newProd = "&";
                }
                prods.add(newProd);
                productions.remove(prodIndex);
            }
            String newNTSymbol = nonterminalSymbol.getSymbol() + "'";
            productions.add(greatestPrefix + newNTSymbol);
            cfg.add(
                cfg.indexOf(nonterminalSymbol) + 1,
                new NonterminalSymbol(newNTSymbol, prods)
            );
            
        }
    }
    
    private static ArrayList<Integer> findPrefixMatches(
        NonterminalSymbol nonterminalSymbol,
        String prefix
    ) {
        ArrayList<Integer> factorizableProductions = new ArrayList<>();
        ArrayList<String> productions = nonterminalSymbol.getProductions();
        int index = 0;
        for (String production : productions) {
            if (production.startsWith(prefix)) {
                factorizableProductions.add(index);
            }
            index++;
        }
        return factorizableProductions;
    }
}
