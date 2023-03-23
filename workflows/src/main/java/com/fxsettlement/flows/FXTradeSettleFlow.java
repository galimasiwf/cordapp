package com.fxsettlement.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.fxsettlement.contracts.FXTradeContract;
import com.fxsettlement.states.FXTradeState;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
//import net.corda.confidential.IdentitySyncFlow;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;

public class FXTradeSettleFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class FXTradeSettleFlowInitiator extends FlowLogic<SignedTransaction> {
        private final Party owner;
        private final String buycurrency;
        private final String sellcurrency;
        private final int buyamount;
        private final int sellamount;
        private final String settledate;
        private final UniqueIdentifier stateLinearId ;

        public FXTradeSettleFlowInitiator (Party owner, String buycurrency,int buyamount, String sellcurrency, int sellamount, String settledate,UniqueIdentifier stateLinearId ) {
            this.owner = owner;
            this.buycurrency = buycurrency;
            this.buyamount = buyamount;
            this.sellcurrency = sellcurrency;
            this.sellamount = sellamount;
            this.settledate = settledate;
            this.stateLinearId = stateLinearId;
        }


        private final ProgressTracker progressTracker = new ProgressTracker();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            //progressTracker.setCurrentStep(INITIALISING);

            // 1. Retrieve the IOU State from the vault using LinearStateQueryCriteria
            List<UUID> listOfLinearIds = Arrays.asList(stateLinearId.getId());
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, listOfLinearIds);

            Vault.Page results = getServiceHub().getVaultService().queryBy(FXTradeState.class, queryCriteria);
            StateAndRef inputStateAndRefToSettle = (StateAndRef) results.getStates().get(0);
            FXTradeState inputStateToSettle = (FXTradeState) ((StateAndRef) results.getStates().get(0)).getState().getData();
            Party counterparty = inputStateToSettle.getOwner();

            // Step 2. Check the party running this flows is the borrower.
            if (!inputStateToSettle.getIssuer().getOwningKey().equals(getOurIdentity().getOwningKey())) {
                throw new IllegalArgumentException("The issuer must issue the flows");
            }
            // Step 3. Create a transaction builder.
            // Obtain a reference to a notary we wish to use.
            Party notary = inputStateAndRefToSettle.getState().getNotary();

            TransactionBuilder tb = new TransactionBuilder(notary);

            // Step 4. Check we have enough cash to settle the requested amount.
            /*
            final Amount<Currency> cashBalance = getCashBalance(getServiceHub(), (Currency) amount.getToken());
            if (cashBalance.getQuantity() < amount.getQuantity()) {
                throw new IllegalArgumentException("Borrower doesn't have enough cash to settle with the amount specified.");
            } else if (amount.getQuantity() > (inputStateToSettle.amount.getQuantity() - inputStateToSettle.paid.getQuantity())) {
                throw new IllegalArgumentException("Borrow tried to settle with more than was required for the obligation.");
            }
            */

            // Step 5. Get some cash from the vault and add a spend to our transaction builder.
            // Vault might contain states "owned" by anonymous parties. This is one of techniques to anonymize transactions
            // generateSpend returns all public keys which have to be used to sign transaction
            /*
            List<PublicKey> keyList = CashUtils.generateSpend(getServiceHub(), tb, amount, getOurIdentityAndCert(), counterparty).getSecond();
            */
            List<PublicKey> keyList = Arrays.asList( counterparty.getOwningKey());

            // Step 6. Add the IOU input states and settle command to the transaction builder.

            Command<FXTradeContract.Commands.Settle> command = new Command<>(
                    new FXTradeContract.Commands.Settle(),
                    Arrays.asList(counterparty.getOwningKey(),getOurIdentity().getOwningKey())
            );
            tb.addCommand(command);
            tb.addInputState(inputStateAndRefToSettle);

            // Step 7. Only add an output IOU states of the IOU has not been fully settled.
            /**
            if (amount.getQuantity() < inputStateToSettle.amount.getQuantity()) {
                tb.addOutputState(inputStateToSettle.pay(amount), IOUContract.IOU_CONTRACT_ID);
            }
            **/
            // Step 8. Verify and sign the transaction.
            tb.verify(getServiceHub());
            keyList.addAll(Arrays.asList(getOurIdentity().getOwningKey()));
            SignedTransaction ptx = getServiceHub().signInitialTransaction(tb, keyList);

            // 11. Collect all of the required signatures from other Corda nodes using the CollectSignaturesFlow
            FlowSession session = initiateFlow(counterparty);
            //new IdentitySyncFlow.Send(session, ptx.getTx());

            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(ptx, Arrays.asList(session)));

            /* 12. Return the output of the FinalityFlow which sends the transaction to the notary for verification
             *     and the causes it to be persisted to the vault of appropriate nodes.
             */
            return subFlow(new FinalityFlow(fullySignedTransaction, session));
        }
    }

    @InitiatedBy(FXTradeSettleFlowInitiator.class)
    public static class FXTradeSettleFlowResponder extends FlowLogic<Void>{
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public FXTradeSettleFlowResponder(FlowSession counterpartySession) {
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
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
