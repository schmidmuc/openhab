/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sapp.internal.configs;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.sapp.internal.model.SappAddressOpenClosedStatus;
import org.openhab.binding.sapp.internal.model.SappAddressType;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.BindingConfigParseException;

// Contact binding format
// <pnmasid status>:<status address type, I/O/V>:<status address, 1-250/1-250/1-2500>:<status subaddress, */H/L/1-16>:<open value>
// <open value> can be omitted, default is 1
// example: { sapp="home:V:192:1" }
// example: { sapp="home:V:192:1:0" }

/**
 * This is a helper class holding ContactItem binding specific configuration details
 * 
 * @author Paolo Denti
 * @since 1.8.0
 * 
 */
public class SappBindingConfigContactItem extends SappBindingConfig {

	private SappAddressOpenClosedStatus status;

	/**
	 * Constructor
	 */
	public SappBindingConfigContactItem(Item item, String bindingConfig) throws BindingConfigParseException {

		super(item.getName());

		this.status = parseSappAddressStatus(bindingConfig);
	}

	/**
	 * status getter
	 */
	public SappAddressOpenClosedStatus getStatus() {
		return status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("[itemName:%s: status:%s ]", getItemName(), this.status.toString());
	}

	private String errorMessage(String bindingConfig) {
		return String.format("Invalid Sapp binding configuration for ContactItem '%s'", bindingConfig);
	}

	private SappAddressOpenClosedStatus parseSappAddressStatus(String bindingStringAddress) throws BindingConfigParseException {

		String pnmasId;
		SappAddressType addressType;
		int address;
		String subAddress;
		int openValue = 1;

		String[] bindingAddress = bindingStringAddress.split(":");
		if (bindingAddress.length != 4 && bindingAddress.length != 5) {
			throw new BindingConfigParseException(errorMessage(bindingStringAddress));
		}

		// pnmasId
		pnmasId = bindingAddress[0];
		if (pnmasId.length() == 0) {
			throw new BindingConfigParseException(errorMessage(bindingStringAddress));
		}

		// addressType
		addressType = SappAddressType.fromString(bindingAddress[1].toUpperCase());
		if (!validAddresses.keySet().contains(addressType)) {
			throw new BindingConfigParseException(errorMessage(bindingStringAddress));
		}

		// address
		try {
			address = Integer.parseInt(bindingAddress[2]);
			if (address < validAddresses.get(addressType).getLoRange() || address > validAddresses.get(addressType).getHiRange()) {
				throw new BindingConfigParseException(errorMessage(bindingStringAddress));
			}
		} catch (NumberFormatException e) {
			throw new BindingConfigParseException(errorMessage(bindingStringAddress));
		}

		// subaddress
		subAddress = bindingAddress[3].toUpperCase();
		if (!ArrayUtils.contains(validSubAddresses, subAddress)) {
			throw new BindingConfigParseException(errorMessage(bindingStringAddress));
		}

		// openvalue
		if (bindingAddress.length == 5) {
			try {
				openValue = Integer.parseInt(bindingAddress[4]);
			} catch (NumberFormatException e) {
				throw new BindingConfigParseException(errorMessage(bindingStringAddress));
			}
		}

		return new SappAddressOpenClosedStatus(pnmasId, addressType, address, subAddress, openValue);
	}
}
