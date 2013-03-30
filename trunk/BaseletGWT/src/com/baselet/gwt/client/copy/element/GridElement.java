package com.baselet.gwt.client.copy.element;

import java.util.List;

import com.baselet.gwt.client.copy.diagram.draw.geom.Dimension;
import com.baselet.gwt.client.copy.diagram.draw.geom.Rectangle;
import com.baselet.gwt.client.copy.gui.AutocompletionText;
import com.baselet.gwt.client.copy.umlet.element.experimental.ComponentInterface;
import com.baselet.gwt.client.copy.umlet.element.experimental.settings.facets.DefaultGlobalFacet.GlobalSetting;

public interface GridElement {

	String getPanelAttributes();

	boolean isSelected();

	void setPanelAttributes(String panelAttributes);

	void setRectangle(Rectangle bounds);

	void setGroup(GroupGridElement object);

	GridElement CloneFromMe();

	void setLocationDifference(int diffx, int diffy);

	void onDeselected();

	void onSelected();

	GroupGridElement getGroup();

	String getAdditionalAttributes();

	void setAdditionalAttributes(String additional_attributes);

	void setLocation(int x, int y);

	void setSize(int width, int height);

	boolean isPartOfGroup();

	int getResizeArea(int x, int y);

	int getPossibleResizeDirections();

	void setStickingBorderActive(boolean stickingBordersActive);

	boolean isStickingBorderActive();

	StickingPolygon generateStickingBorder(int x, int y, int width, int height);

	Rectangle getRectangle();

	void repaint();

	void changeSize(int diffx, int diffy);

	Dimension getZoomedSize();

	/**
	 * @return size of the element as if the zoomlevel would be 100% (eg: if zoom is 80% and width is 80 it would be returned as 100)
	 */
	Dimension getRealSize();

	boolean isInRange(Rectangle rectangle);
	
	ComponentInterface getComponent();

	void updateProperty(GlobalSetting key, String newValue);
	
	void updateModelFromText();

	List<AutocompletionText> getAutocompletionList();

	String getSetting(GlobalSetting key);
	
	Integer getLayer();
}