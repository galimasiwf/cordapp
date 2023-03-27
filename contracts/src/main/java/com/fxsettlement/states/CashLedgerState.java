package com.fxsettlement.states;

import com.fxsettlement.contracts.CashLedgerContract;
import com.fxsettlement.contracts.FXTradeContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
@BelongsToContract(CashLedgerContract.class)
public class CashLedgerState implements ContractState {
        private final Party issuer;
        private final Party responder;
        private final String accountId;
        private final String currency;
        private final float amount;
        //private final UniqueIdentifier linearId;

        @ConstructorForDeserialization
        public CashLedgerState(Party issuer, Party responder, String accountId, float amount, String currency) {
            this.issuer = issuer;
            this.responder = responder;
            this.accountId = accountId;
            this.amount = amount;
            this.currency = currency;
        }

        public Party getIssuer() {
            return issuer;
        }

        public Party getResponder() {
            return responder;
        }

        public String getAccountId() {
            return accountId;
        }

        public float getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }

        @NotNull
        @Override
        public List<AbstractParty> getParticipants() {
            return Arrays.asList(issuer, responder);
        }
}

