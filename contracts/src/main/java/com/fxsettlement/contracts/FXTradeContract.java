package com.fxsettlement.contracts;

import com.fxsettlement.states.FXTradeState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.*;


import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class FXTradeContract implements Contract {
    public static String ID = "com.fxsettlement.contracts.FXTradeContract";


    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        //Bhanu
        if (tx.getInputStates().size() > 1) {
            throw new IllegalArgumentException("Token Contract requires zero inputs in the transaction");
        }

        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("Token Contract requires one output in the transaction");
        }

        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Token Contract requires one command in the transaction");
        }

        if (!(tx.getOutput(0) instanceof FXTradeState)) {
            throw new IllegalArgumentException("Token Contract requires  transaction output to be a Token State ");
        }

        FXTradeState FXTradeState = (FXTradeState) tx.getOutput(0);
        if (FXTradeState.getSellamount() < 0 || FXTradeState.getBuyamount() < 0 ) {
            throw new IllegalArgumentException("Token Contract requires Contract Amount to be positive  ");
        }

        if (!((tx.getCommand(0).getValue() instanceof FXTradeContract.Commands.Issue) ||
                (tx.getCommand(0).getValue() instanceof FXTradeContract.Commands.Settle))) {
            throw new IllegalArgumentException("Token Contract requires transaction command to be an Issue ");
        }

        //if (tx.getCommand(0).getSigners().contains(FXTradeState.getIssuer().getOwningKey())) {
        //    throw new IllegalArgumentException("Token Contract requires the issuer to be required signer in the transaction ");
        //}
        //Bhanu
    }

    public interface Commands extends CommandData {
        class Issue implements Commands {
        }

        class Settle extends TypeOnlyCommandData implements Commands{}
    }
}