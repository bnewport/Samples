//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere;

import com.devwebsphere.wxs.asyncservice.KeyOperator;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

public class ShoppingCartWorker implements KeyOperator<String> {
	private static final long serialVersionUID = 5862859013522161703L;

	public int quantity;
	String badSku;

	public boolean apply(Session localSession, String sku) throws ObjectGridException {
		ObjectMap invMap = localSession.getMap(TestMapNames.skuMap);
		InventoryLevel level = (InventoryLevel) invMap.getForUpdate(sku);
		if (level != null && level.availableToSell >= quantity) {
			level.demand++;
			level.availableToSell -= quantity;
			invMap.update(sku, level);
			return true;
		} else {
			badSku = sku;
			return false;
		}
	}

	public void unapply(Session localSession, String sku) throws ObjectGridException {
		ObjectMap invMap = localSession.getMap(TestMapNames.skuMap);
		InventoryLevel level = (InventoryLevel) invMap.getForUpdate(sku);
		if (level != null) {
			level.availableToSell += quantity;
			level.demand--;
			invMap.update(sku, level);
		}
	}
}
