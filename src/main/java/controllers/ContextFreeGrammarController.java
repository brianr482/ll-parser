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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
    
    public static void cleanCFG(ArrayList<NonterminalSymbol> cfg) {
        ArrayList<NonterminalSymbol> cfgClone = new ArrayList<>();
        cfg.forEach((nonterminalSymbol) -> {
            cfgClone.add(nonterminalSymbol);
        });
        cfgClone.forEach((nonterminalSymbol) -> {
            if (findLeftRecursion(nonterminalSymbol)) {
                removeLeftRecursion(
                    cfg,
                    nonterminalSymbol
                );
            } else {
                findLeftFactorization(cfg, nonterminalSymbol);
            }
        });
    }
    
    public static Map<String, Set<String>> getFollowingList(
        ArrayList<NonterminalSymbol> cfg
    ) {
        Map<String, Set<String>> followingList = new LinkedHashMap<>();
        Map<String, Set<String>> pending = new LinkedHashMap<>();
        cfg.forEach(nonterminal -> {
            Set<String> nonterminalFollowingList = new LinkedHashSet<>();
            followingList.put(
                nonterminal.getSymbol(),
                nonterminalFollowingList
            );
            nonterminal.setFollowingList(nonterminalFollowingList);
            String nonterminalSymbol = nonterminal.getSymbol();
            cfg.stream()
                .filter(nt -> !nt.getSymbol().equals(nonterminalSymbol))
                .forEach(nt -> {
                nt.getProductions().forEach(production -> {
                    if (production.endsWith(nonterminalSymbol)) {
                        pending.putIfAbsent(
                            nonterminalSymbol, new LinkedHashSet<>()
                        );
                        pending.get(nonterminalSymbol).add(nt.getSymbol());
                    } else if (production.contains(nonterminalSymbol)) {
                        int followingIndex = production
                            .indexOf(nonterminalSymbol) + 1;
                        int followingSymbolIndexLimit = followingIndex + 1;
                        if (
                            followingIndex + 1 != production.length() 
                            && production.substring(
                                followingIndex + 1, followingIndex + 2
                            ).equals("'")
                        ) {
                            followingSymbolIndexLimit++;
                        }
                        String followingGSymbol = production.substring(
                            followingIndex,
                            followingSymbolIndexLimit
                        );
                        if (isNonterminal(cfg, followingGSymbol)) {
                            Set<String> foreignFirstList = cfg.stream().filter(
                                nte -> nte.getSymbol().equals(followingGSymbol)
                            ).findAny().orElse(null).getFirstList();
                            foreignFirstList.forEach(terminal -> {
                                if (terminal.equals("&")) {
                                    pending.putIfAbsent(
                                        nonterminalSymbol, new LinkedHashSet<>()
                                    );
                                    pending.get(nonterminalSymbol)
                                        .add(nt.getSymbol());
                                } else {
                                    followingList.get(nonterminalSymbol).add(
                                        terminal
                                   );
                                }
                            });
                            pending.putIfAbsent(
                                nonterminalSymbol, new LinkedHashSet<>()
                            );
                            pending.get(nonterminalSymbol).add(followingGSymbol);
                        } else {
                            followingList.get(nonterminalSymbol).add(
                                followingGSymbol
                            );
                        }
                    }
                });
            });
        });
        followingList.get(cfg.get(0).getSymbol()).add("$");
        Map<String, Set<String>> sortedPending = new LinkedHashMap<>();
        Map<String, Set<String>> dependentPending = new LinkedHashMap<>();
        pending.entrySet().forEach((pend) -> {
            Set<String> followingSet = pend.getValue();
            followingSet.forEach((nonterminal) -> {
                String key = pend.getKey();
                Map<String, Set<String>> map = pending.containsKey(nonterminal)
                        ? dependentPending : sortedPending;
                map.put(key, pend.getValue());
            });
        });
        sortedPending.putAll(dependentPending);
        sortedPending.entrySet().forEach((pend) -> {
            Set<String> nonterminalFollowingList =  followingList
                .get(pend.getKey());
            pend.getValue().forEach(pendingFollowing -> {
                Set<String> pendingFollowingList = followingList
                    .get(pendingFollowing);
                pendingFollowingList.forEach(terminal -> {
                    nonterminalFollowingList.add(terminal);
                });
            });
        });
        completeCFGMAssociation(cfg);
       return followingList;
    }
    
    public static Map<String, Set<String>> getFirstPositionList(
        ArrayList<NonterminalSymbol> cfg
    ) {
        Map<String, Set<String>> firstPositionList = new LinkedHashMap<>();
        Map<String, Set<String>> pending = new LinkedHashMap<>();
        cfg.forEach((nonterminalSymbol) -> {
            Set<String> firstSet = new LinkedHashSet<>();
            Map<String, String> mAssociationList = new LinkedHashMap<>();
            nonterminalSymbol.setFirstList(firstSet);
            nonterminalSymbol.setmTableAssociation(mAssociationList);
            firstPositionList.put(nonterminalSymbol.getSymbol(),firstSet);
            nonterminalSymbol.getProductions().forEach((production) -> {
                String firstChar = production.substring(0, 1);
                boolean isNonterminal = isNonterminal(cfg, firstChar);
                Set<String> firstPositionL = isNonterminal 
                    ? pending.get(nonterminalSymbol.getSymbol())
                    : firstPositionList.get(nonterminalSymbol.getSymbol());
                if (isNonterminal && firstPositionL == null) {
                    pending.put(
                        nonterminalSymbol.getSymbol(),
                        new LinkedHashSet<>(Arrays.asList(firstChar))
                    );
                } else {
                    firstPositionL.add(firstChar);
                }
                mAssociationList.put(firstChar, production);
            });
        });
        Map<String, Set<String>> sortedPending = new LinkedHashMap<>();
        Map<String, Set<String>> dependentPending = new LinkedHashMap<>();
        pending.entrySet().forEach((pend) -> {
            Set<String> firstPositions = pend.getValue();
            firstPositions.forEach((nonterminal) -> {
                String key = pend.getKey();
                Map<String, Set<String>> map = pending.containsKey(nonterminal)
                        ? dependentPending : sortedPending;
                map.put(key, pend.getValue());
            });
        });
        sortedPending.putAll(dependentPending);
        sortedPending.entrySet().forEach((pend) -> {
            Set<String> nonterminalFirstPosList =  firstPositionList
                .get(pend.getKey());
            pend.getValue().forEach(pendingFirstPos -> {
                Set<String> pendingFirstPosList = firstPositionList
                    .get(pendingFirstPos);
                NonterminalSymbol ns = cfg.stream()
                    .filter(nte -> nte.getSymbol().equals(pend.getKey()))
                    .findAny()
                    .orElse(null);
                String prodForMTable = ns.getmTableAssociation()
                    .get(pendingFirstPos);
                ns.getmTableAssociation().remove(pendingFirstPos);
                pendingFirstPosList.forEach(terminal -> {
                    nonterminalFirstPosList.add(terminal);
                    ns.getmTableAssociation().put(terminal, prodForMTable);
                });
            });
        });
        return firstPositionList;
    }
    
    public static Set<String> getTerminalList(
        ArrayList<NonterminalSymbol> cfg
    ) {
        Set<String> terminalList = new LinkedHashSet<>();
        cfg.forEach(nonterminal -> {
            nonterminal.getProductions().forEach(production -> {
                boolean isPrevANonterminal = false;
                for(char c : production.toCharArray()) {
                    String parsedChar = Character.toString(c);
                    if (!isNonterminal(cfg, parsedChar)) {
                        if (!parsedChar.equals("&")) {
                            if (
                                (parsedChar.equals("'") && !isPrevANonterminal) 
                                || !parsedChar.equals("'")
                            ) {
                                terminalList.add(parsedChar);
                            }
                        }
                        isPrevANonterminal = false;
                    } else {
                        isPrevANonterminal = true;
                    }
                }
            });
        });
        terminalList.add("$");
        return terminalList;
    }
    
    public static boolean isNonterminal(
        ArrayList<NonterminalSymbol> cfg, String symbol
    ) {
        return cfg.stream().anyMatch(nonterminalSymbol -> 
            nonterminalSymbol.getSymbol().equals(symbol)
        );
    }
    
    private static NonterminalSymbol findProduction(
        ArrayList<NonterminalSymbol> productions, String gs
    ) {
        return productions.stream()
            .filter(production -> production.getSymbol().equals(gs))
            .findFirst()
            .orElse(null);
    }
    
    private static void removeLeftRecursion(
        ArrayList<NonterminalSymbol> cfg,
        NonterminalSymbol nonterminalSymbol
    ) {
        ArrayList<String> productions = nonterminalSymbol.getProductions();
        ArrayList<String> prods = new ArrayList<>();
        ArrayList<String> newOriginalNonterminalProductions = new ArrayList<>();
        String newNTSymbol = nonterminalSymbol.getSymbol() + "'";
        for (int i = 0; i < productions.size(); i++) {
            String prod = productions.get(i);
            if (prod.startsWith(nonterminalSymbol.getSymbol())) {
                prods.add(prod.substring(1) + newNTSymbol);
            } else {
                newOriginalNonterminalProductions.add(prod + newNTSymbol);
            }
        }
        prods.add("&");
        nonterminalSymbol.setProduction(newOriginalNonterminalProductions);
        cfg.add(
            cfg.indexOf(nonterminalSymbol) + 1,
            new NonterminalSymbol(newNTSymbol, prods)
        );
    }
    
    private static boolean findLeftRecursion(NonterminalSymbol nonterminalSymbol) {
        int index = 0;
        ArrayList<String> productions = nonterminalSymbol.getProductions();
        while (index < productions.size()) {
            String production = productions.get(index);
            if (hasLeftRecursion(nonterminalSymbol.getSymbol(), production)) {
                return true;
            } else {
                index++;
            }
        }
        return false;
    }
    
    private static boolean hasLeftRecursion(
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
    
    private static void completeCFGMAssociation(ArrayList<NonterminalSymbol> cfg) {
        cfg.forEach(nonterminal -> {
            replaceEpsilonInMAssociation(nonterminal);
        });
    }
    
    private static void replaceEpsilonInMAssociation(NonterminalSymbol nonterminal) {
        Map<String, String> mTableAssociation = nonterminal.getmTableAssociation();
        String production = mTableAssociation.get("&");
        if (production != null) {
            mTableAssociation.remove("&");
            nonterminal.getFollowingList().forEach(terminal -> {
                mTableAssociation.put(terminal, production);
            });
        }
    }
}
