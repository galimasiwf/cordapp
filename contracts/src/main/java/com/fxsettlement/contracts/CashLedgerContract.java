package com.fxsettlement.contracts;

import com.fxsettlement.states.CashLedgerState;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.contracts.CommandData;

public class CashLedgerContract implements Contract {
    public static String ID = "com.fxsettlement.contracts.CashLedgerContract";
        public void verify(LedgerTransaction tx) throws IllegalArgumentException {
            if(tx.getInputs().size() != 0)
                throw new IllegalArgumentException("Zero Inputs Expected");

            if(tx.getOutputs().size() != 1)
                throw new IllegalArgumentException("One Output Expected");

            if(tx.getCommands().size() !=1)
                throw new IllegalArgumentException("One Command Expected");
            /**
            if(!(tx.getOutput(0) instanceof CashLedgerState))
                throw new IllegalArgumentException("Output of type TokenState Expected");
             **/
            /**
            if(!(tx.getCommand(0).getValue() instanceof Commands.Issue))
                throw new IllegalArgumentException("Issue Command Expected");
            **/
            CashLedgerState tokenState = (CashLedgerState)tx.getOutput(0);
            if(tokenState.getAmount() < 1)
                throw new IllegalArgumentException("Positive amount expected");

            if(!(tx.getCommand(0).getSigners()
                    .contains(tokenState.getIssuer().getOwningKey())))
                throw new IllegalArgumentException("Issuer must sign");

            if(tx.getCommand(0).getValue() instanceof Commands.Add)
                verifyAdd(tx);
            else if(tx.getCommand(0).getValue() instanceof Commands.Credit)
                verifyCredit(tx);
            else if(tx.getCommand(0).getValue() instanceof Commands.Debit)
                verifyDebit(tx);
            else
                throw new IllegalArgumentException("Unsupported Command");
        }

    private void verifyAdd(LedgerTransaction tx){
       /**
        if(tx.getInputs().size() != 0)
            throw new IllegalArgumentException("Zero Inputs Expected");

        if(tx.getOutputs().size() != 1)
            throw new IllegalArgumentException("One Output Expected");

        if(!(tx.getOutput(0) instanceof TokenState))
            throw new IllegalArgumentException("Output of type TokenState Expected");

        CashLedgerState tokenState = (CashLedgerState)tx.getOutput(0);
        if(tokenState.getAmount() < 1)
            throw new IllegalArgumentException("Positive amount expected");

        if(!(tx.getCommand(0).getSigners()
                .contains(tokenState.getIssuer().getOwningKey())))
            throw new IllegalArgumentException("Issuer must sign");
        **/
    }

    private void verifyCredit(LedgerTransaction tx){

    }
    private void verifyDebit(LedgerTransaction tx){

    }

    public interface Commands extends CommandData {
        class Add implements Commands { }
        class Credit implements Commands { }
        class Debit implements Commands { }
    }
}
