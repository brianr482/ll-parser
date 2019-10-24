/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.ArrayList;
import java.util.Stack;
import models.NonterminalSymbol;
import models.RecognitionRecord;

/**
 *
 * @author brian
 */
public class RecognitionController {
    private final static String ACCEPT_RESULT = "Aceptar";
    private final static String DECLINE_RESULT = "Error";
    
    public static ArrayList<RecognitionRecord> testString(
        ArrayList<NonterminalSymbol> cfg, String string
    ) {
        ArrayList<RecognitionRecord> history = new ArrayList<>();
        String result = null;
        String stackLine = "$" + cfg.get(0).getSymbol();
        String inputLine = string + "$";
        RecognitionRecord record;
        Stack<String> stack = new Stack<>();
        stack.add(cfg.get(0).getSymbol());
        while (result == null) {
            if (stack.empty()) {
                result = inputLine.equals("$") ? ACCEPT_RESULT
                    : DECLINE_RESULT;
                history.add(
                    new RecognitionRecord("$", inputLine, result)
                );
            } else {
                String stackPointerValue = stack.pop();
                record = new RecognitionRecord(stackLine, inputLine);
                history.add(record);
                NonterminalSymbol nonterminal = cfg.stream()
                    .filter(nt -> nt.getSymbol().equals(stackPointerValue))
                    .findAny()
                    .orElse(null);
                String followingInputTerminal = inputLine.substring(0, 1);
                stackLine = record.getStack().substring(
                    0,
                    record.getStack().length() - stackPointerValue.length()
                );
                if (nonterminal != null) {
                    String production = nonterminal.getmTableAssociation()
                        .get(followingInputTerminal);
                    if (production != null) {
                        record.setOutput(
                            nonterminal.getSymbol() + "->" + production
                        );
                        ArrayList<String> prods = new ArrayList<>();
                        for (int i = 0; i < production.length(); i++) {
                            String grammarSymbol = production.substring(i, i + 1);
                            if (
                                !grammarSymbol.equals("&")
                                 && !(
                                        grammarSymbol.equals("'") 
                                        && i > 0
                                        && ContextFreeGrammarController.isNonterminal(
                                            cfg, production.substring(i - 1, i + 1))
                                    )
                            ) {
                                if (i + 1 < production.length()) {
                                    if (
                                        production.substring(i + 1, i + 2)
                                            .equals("'")
                                     ) {
                                        grammarSymbol += "'";
                                    }
                                }
                                prods.add(0, grammarSymbol);
                            }
                        }
                        for (String prod : prods) {
                            stackLine += prod;
                        }
                        stack.addAll(prods);
                    } else {
                        result = DECLINE_RESULT;
                        record.setOutput(result);
                    }
                } else {
                    if (inputLine.startsWith(stackPointerValue)) {
                        record.setOutput("");
                        inputLine = inputLine.substring(1);
                    } else {
                        result = DECLINE_RESULT;
                        record.setOutput(result);
                    }
                }
            }
        }    
        return history;
    }
}
