package com.hybris.homeserver.endpoints.http.api.secret.barcode;

public class BarcodeMetadata {

	private String productName;
	private String productDescription;

	public BarcodeMetadata() {}
	
	public BarcodeMetadata(String productName, String productDescription) {
		this.productName = productName;
		this.productDescription = productDescription;
	}

	public String getProductName() {
		return productName;
	}

	public String getProductDescription() {
		return productDescription;
	}
}
