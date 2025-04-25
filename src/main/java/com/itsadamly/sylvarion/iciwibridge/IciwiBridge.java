package com.itsadamly.sylvarion.iciwibridge;
import com.itsadamly.sylvarion.Sylvarion;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.api.IciwiPlugin;

public class IciwiBridge  extends Sylvarion implements IciwiPlugin
{
	@Override
	public Class<? extends IcCard> getFareCardClass ()
	{
		return BankCard.class;
	}
}
