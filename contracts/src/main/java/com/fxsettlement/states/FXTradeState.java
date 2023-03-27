package com.fxsettlement.states;

import com.fxsettlement.contracts.FXTradeContract;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/* Our state, defining a shared fact on the ledger.
 * See src/main/java/examples/ArtState.java for an example. */
@BelongsToContract(FXTradeContract.class)
public class FXTradeState implements ContractState,LinearState {
	private final Party issuer;
	private final Party responder;
	private final String buysell;
	private final int buyamount;
	private final int sellamount;
	private final String buycurrency;
	private final String sellcurrency;
	private final float exchangerate;
	private final String tradedate;
	private final String settledate;
	private final String matchstatus;
	private final String settlementstatus;
	private final UniqueIdentifier linearId;

	//private final String paymentstatus;

	//private final float amount;
	//private final String currencyPair;

	public FXTradeState(Party issuer, Party responder, int buyamount, int sellamount, String buycurrency, String sellcurrency, String tradedate, String settledate, float exchangerate, String buysell, String matchstatus, String settlementstatus) {
		this(issuer, responder, buyamount, sellamount, buycurrency,sellcurrency,tradedate,settledate,exchangerate,buysell,matchstatus,settlementstatus, new UniqueIdentifier());
	}
	@ConstructorForDeserialization
	private FXTradeState(Party issuer, Party responder, int buyamount, int sellamount, String buycurrency, String sellcurrency, String tradedate, String settledate, float exchangerate, String buysell, String matchstatus, String settlementstatus,UniqueIdentifier linearId) {
		this.issuer = issuer;
		this.responder = responder;
		this.buyamount = buyamount;
		this.sellamount = sellamount;
		this.buycurrency = buycurrency;
		this.sellcurrency = sellcurrency;
		this.tradedate = tradedate;
		this.settledate = settledate;
		this.exchangerate = exchangerate;
		this.buysell = buysell;
		this.matchstatus = matchstatus;
		this.settlementstatus = settlementstatus;
		this.linearId = linearId;
	}



	public Party getIssuer() {
		return issuer;
	}

	public Party getResponder() {
		return responder;
	}

	public int getBuyamount() { return buyamount; }

	public int getSellamount() { return sellamount; }

	public String getBuycurrency() { return buycurrency; }

	public String getSellcurrency() { return sellcurrency; }

	public String getSettledate() { return settledate; }

	public String getTradedate() { return tradedate; }

	public float getExchangerate() { return exchangerate; }

	public String getBuysell() { return buysell; }

	public String getMatchstatus() { return matchstatus; }

	public String getSettlementstatus() { return settlementstatus; }

	@Override
	public UniqueIdentifier getLinearId() {
		return linearId;
	}

	@NotNull
	@Override
	public List<AbstractParty> getParticipants() {
		return Arrays.asList (issuer,responder);
	}
}