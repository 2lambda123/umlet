package com.umlet.element.experimental.uml;

import com.baselet.diagram.draw.BaseDrawHandler;
import com.baselet.element.StickingPolygon;
import com.umlet.element.experimental.ElementId;
import com.umlet.element.experimental.NewGridElement;
import com.umlet.element.experimental.Properties;
import com.umlet.element.experimental.settings.Settings;
import com.umlet.element.experimental.settings.SettingsInterface;

public class Interface extends NewGridElement {

	private int TOP_DISTANCE = 10;
	private int CIRCLE_SIZE = 20;

	@Override
	public ElementId getId() {
		return ElementId.UMLInterface;
	}

	@Override
	protected void updateConcreteModel(BaseDrawHandler drawer, Properties properties) {
		drawer.drawEllipse(posOfCircle(), TOP_DISTANCE, CIRCLE_SIZE, CIRCLE_SIZE);
		properties.drawPropertiesText();
	}

	/**
	 * circle is in the middle. At the moment it's not always on grid (otherwise it would "jump" around)
	 * TODO let circle have a fixed position and grow element around it (like in the old Interface)
	 */
	private int posOfCircle() {
//		int gridSize = (int) (getHandler().getZoomFactor() * NewGridElementConstants.DEFAULT_GRID_SIZE);
		int middlePos = getRealSize().getWidth() / 2 - CIRCLE_SIZE/2;
//		return middlePos - (middlePos % gridSize);
		return middlePos;
	}

	@Override
	public StickingPolygon generateStickingBorder(int x, int y, int width, int height) {
		StickingPolygon p = new StickingPolygon(x, y);
		p.addRectangle(posOfCircle(), TOP_DISTANCE, CIRCLE_SIZE, CIRCLE_SIZE);
		return p;
	}

	@Override
	public Settings getSettings() {
		return new SettingsInterface();
	}
}
