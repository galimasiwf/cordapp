package com.fxsettlement.states;

import com.fxsettlement.contracts.FXTradeContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/* Our state, defining a shared fact on the ledger.
 * See src/main/java/examples/ArtState.java for an example. */
@BelongsToContract(FXTradeContract.class)
public class FXTradeState implements ContractState{


	private final Party owner;
	private final Party issuer;
	private final int buyamount;
	private final int sellamount;
	private final String buycurrency;
	private final String sellcurrency;
	private final String settledate;

	//private final String currencyPair;
	//private final float exchangeRate;
	//private final float amount;
	//private final String buysell;
	//private final String settlementstatus;
	//private final String paymentstatus;

	public FXTradeState(Party issuer, Party owner, int buyamount, int sellamount, String buycurrency, String sellcurrency, String settledate) {
		this.issuer = issuer;
		this.owner = owner;
		this.buyamount = buyamount;
		this.sellamount = sellamount;
		this.buycurrency = buycurrency;
		this.sellcurrency = sellcurrency;
		this.settledate = settledate;
	}

	public Party getIssuer() {
		return issuer;
	}

	public Party getOwner() {
		return owner;
	}

	public int getBuyamount() { return buyamount; }

	public int getSellamount() { return sellamount; }

	public String getBuycurrency() { return buycurrency; }

	public String getSellcurrency() { return sellcurrency; }

	public String getSettledate() { return settledate; }


	@NotNull
	@Override
	public List<AbstractParty> getParticipants() {
		return Arrays.asList (issuer,owner);
	}
}