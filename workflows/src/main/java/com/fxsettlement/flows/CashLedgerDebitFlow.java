package com.fxsettlement.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.fxsettlement.contracts.CashLedgerContract;
import com.fxsettlement.states.CashLedgerState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;

import static java.util.Collections.singletonList;

public class CashLedgerDebitFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class CashLedgerDebitFlowInitiator extends FlowLogic<SignedTransaction> {
        private final Party responder;
        private final String accountId;
        private final String currency;
        private final float amount;

        public CashLedgerDebitFlowInitiator(Party responder, String accountId, String currency, float amount) {
            this.responder = responder;
            this.accountId = accountId;
            this.currency = currency;
            this.amount = amount;
        }

        private final ProgressTracker progressTracker = new ProgressTracker();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            // We get a reference to our own identity.
            Party issuer = getOurIdentity();

            // We create our new TokenState.
            CashLedgerState cashLedgerState = new CashLedgerState(issuer, responder, accountId, amount, currency);

            // We build our transaction.
            TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                    .addOutputState(cashLedgerState)
                    .addCommand(new CashLedgerContract.Commands.Add(), Arrays.asList(issuer.getOwningKey(), responder.getOwningKey()));

            // We check our transaction is valid based on its contracts.
            transactionBuilder.verify(getServiceHub());

            FlowSession session = initiateFlow(responder);

            // We sign the transaction with our private key, making it immutable.
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // The counterparty signs the transaction
            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));

            // We get the transaction notarised and recorded automatically by the platform.
            return subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
        }
    }


@InitiatedBy(CashLedgerDebitFlowInitiator.class)
public static class CashLedgerDebitFlowResponder extends FlowLogic<Void>{
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public CashLedgerDebitFlowResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
            @Suspendable
            @Override
            protected void checkTransaction(SignedTransaction stx) throws FlowException {
                /*
                 * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                 * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                 * What if we don’t want to deal with the counterparty in question, or the value is too high,
                 * or we’re not happy with the transaction’s structure? checkTransaction
                 * allows us to define these additional checks. If any of these conditions are not met,
                 * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                 * ----------
                 * For this hello-world cordapp, we will not implement any aditional checks.
                 * */
            }
         }
        );
        //Stored the transaction into data base.
        subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
        return null;
    }
    }
}