package com.umlet.element.experimental.facet.common;

import com.baselet.diagram.draw.BaseDrawHandler;
import com.baselet.diagram.draw.helper.ColorOwn;
import com.umlet.element.experimental.PropertiesConfig;
import com.umlet.element.experimental.facet.KeyValueFacet;

public class BackgroundColorFacet extends KeyValueFacet {
	
	public static BackgroundColorFacet INSTANCE = new BackgroundColorFacet();
	private BackgroundColorFacet() {}

	public static final String KEY = "bg";
	
	@Override
	public KeyValue getKeyValue() {
		return new KeyValue(KEY, false, "red", "background " + ColorOwn.EXAMPLE_TEXT);
	}

	@Override
	public void handleValue(String value, BaseDrawHandler drawer, PropertiesConfig propConfig) {
		drawer.setBackgroundColor(value);
	}

	public Priority getPriority() {
		return Priority.HIGHER;
	}

}
