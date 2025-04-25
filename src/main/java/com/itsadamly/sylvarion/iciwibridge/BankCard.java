package com.itsadamly.sylvarion.iciwibridge;
import mikeshafter.iciwi.api.IcCard;

public class BankCard implements IcCard {

@Override
public boolean withdraw (double v) {
	return false;
}

@Override
public String getSerial () {
	return "";
}

@Override
public boolean deposit (double v) {
	return false;
}
}
