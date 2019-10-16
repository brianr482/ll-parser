/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ll.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author brian
 */
public class Util {
    /* CFG = Context-free grammar */
    public static ArrayList<Production> readCFGFromFile(File file)
        throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        ArrayList<Production> cfg = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] productionLine = line.split("->");
            String grammaticalSymbol = productionLine[0];
            Production production = findProduction(
                cfg, grammaticalSymbol
            );
            if (production == null) {
                production = new Production(grammaticalSymbol);
                cfg.add(production);
            }
            production.getProduction().add(productionLine[1]);
        }
        return cfg;
    }
    
    private static Production findProduction(
        ArrayList<Production> productions, String gs
    ) {
        return productions.stream()
            .filter(production -> production.getSymbol().equals(gs))
            .findFirst()
            .orElse(null);
    }
}
