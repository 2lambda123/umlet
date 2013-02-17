package com.baselet.gui.standalone;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;

import com.baselet.control.Constants;
import com.baselet.control.Constants.Program;
import com.baselet.control.Constants.ProgramName;
import com.baselet.control.Main;
import com.baselet.control.Path;
import com.baselet.diagram.CustomPreviewHandler;
import com.baselet.diagram.DiagramHandler;
import com.baselet.diagram.DrawPanel;
import com.baselet.diagram.PaletteHandler;
import com.baselet.gui.BaseGUI;
import com.baselet.gui.MenuFactory;
import com.baselet.gui.MenuFactorySwing;
import com.baselet.gui.OwnSyntaxPane;
import com.baselet.gui.TabComponent;
import com.baselet.gui.listener.GUIListener;
import com.umlet.custom.CustomElementHandler;

@SuppressWarnings("serial")
public class StandaloneGUI extends BaseGUI {

	private MenuFactorySwing menuFactory;
	private JFrame window;
	private JMenu editMenu;
	private JMenuItem editUndo;
	private JMenuItem editRedo;
	private JMenuItem editDelete;
	private JMenuItem editSelectAll;
	private JMenuItem editGroup;
	private JMenuItem editUngroup;
	private JMenuItem editCut;
	private JMenuItem editCopy;
	private JMenuItem editPaste;
	private JMenuItem customMenu;
	private JMenu customTemplate;
	private JMenuItem customEdit;

	private boolean custom_element_selected;

	private MenuBuilder menu = new MenuBuilder();
	private StandaloneGUIBuilder gui = new StandaloneGUIBuilder();

	private JToggleButton mailButton;

	public StandaloneGUI(Main main) {
		super(main);
		custom_element_selected = false;
	}

	@Override
	public void updateDiagramName(DiagramHandler diagram, String name) {
		JTabbedPane diagramtabs = gui.getDiagramtabs();
		int index = diagramtabs.indexOfComponent(diagram.getDrawPanel().getScrollPane());
		if (index != -1) {
			diagramtabs.setTitleAt(index, name);
			// update only selected tab to keep scrolling tab position
			((TabComponent)diagramtabs.getTabComponentAt(index)).updateUI();
		}
	}

	@Override
	public void setDiagramChanged(DiagramHandler diagram, boolean changed) {
		String change_string = "";
		if (changed) change_string = " *";

		this.updateDiagramName(diagram, diagram.getName() + change_string);
	}

	@Override
	public void setCustomElementChanged(CustomElementHandler handler, boolean changed) {

	}

	@Override
	public void closeWindow() {
		gui.getMailPanel().closePanel(); // We must close the mailpanel to save the input date
		if (this.askSaveForAllDirtyDiagrams()) {
			this.main.closeProgram();
			this.window.dispose();
			System.exit(0);
		}
	}

	private boolean askSaveForAllDirtyDiagrams() {
		boolean ok = true;
		for (DiagramHandler d : Main.getInstance().getDiagrams()) {
			if (!d.askSaveIfDirty()) ok = false;
		}

		if (!getCurrentCustomHandler().closeEntity()) ok = false;
		return ok;
	}

	@Override
	protected void init() {

		this.addKeyListener(new GUIListener());

		this.window = new JFrame();
		this.window.setContentPane(this);
		this.window.addWindowListener(new WindowListener());
		this.window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.window.setBounds(Constants.program_location.x, Constants.program_location.y, Constants.program_size.width, Constants.program_size.height);
		if (Program.PROGRAM_NAME.equals(ProgramName.UMLET)) this.window.setTitle("UMLet - Free UML Tool for Fast UML Diagrams");
		else if (Program.PROGRAM_NAME.equals(ProgramName.PLOTLET)) this.window.setTitle("Plotlet - Free Tool for Fast Plots");

		/************************ SET PROGRAM ICON ************************/
		String iconPath = Path.homeProgram() + "img/" + Program.PROGRAM_NAME.toLowerCase() + "_logo.png";
		this.window.setIconImage(new ImageIcon(iconPath).getImage());
		/*************************************************************/

		/*********** SET WINDOW BOUNDS **************/
		if (Constants.start_maximized) {
			// If Main starts maximized we set fixed bounds and must set the frame visible
			// now to avoid a bug where the right sidebar doesn't have the correct size
			this.window.setExtendedState(this.window.getExtendedState() | Frame.MAXIMIZED_BOTH);
			this.window.setVisible(true);
		}

		/*********** CREATE MENU *****************/
		JMenuBar menu = new JMenuBar();
		menuFactory = MenuFactorySwing.getInstance();

		JMenu fileMenu = new JMenu(MenuFactory.FILE);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(menuFactory.createNew());
		fileMenu.add(menuFactory.createOpen());
		fileMenu.add(menuFactory.createRecentFiles());
		fileMenu.addSeparator();
		fileMenu.add(menuFactory.createGenerate());
		fileMenu.add(menuFactory.createGenerateOptions());
		fileMenu.addSeparator();
		fileMenu.add(menuFactory.createSave());
		fileMenu.add(menuFactory.createSaveAs());
		fileMenu.add(menuFactory.createExportAs());
		fileMenu.add(menuFactory.createMailTo());
		fileMenu.addSeparator();
		fileMenu.add(menuFactory.createEditCurrentPalette());
		fileMenu.addSeparator();
		fileMenu.add(menuFactory.createOptions());
		fileMenu.addSeparator();
		fileMenu.add(menuFactory.createPrint());
		fileMenu.addSeparator();
		fileMenu.add(menuFactory.createExit());
		menu.add(fileMenu);

		editMenu = new JMenu(MenuFactory.EDIT);
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(editUndo = menuFactory.createUndo());
		editMenu.add(editRedo = menuFactory.createRedo());
		editMenu.add(editDelete = menuFactory.createDelete());
		editMenu.addSeparator();
		editMenu.add(editSelectAll = menuFactory.createSelectAll());
		editMenu.add(editGroup = menuFactory.createGroup());
		editMenu.add(editUngroup = menuFactory.createUngroup());
		editMenu.addSeparator();
		editMenu.add(editCut = menuFactory.createCut());
		editMenu.add(editCopy = menuFactory.createCopy());
		editMenu.add(editPaste = menuFactory.createPaste());
		menu.add(editMenu);
		editDelete.setEnabled(false);
		editGroup.setEnabled(false);
		editCut.setEnabled(false);
		editPaste.setEnabled(false);
		editUngroup.setEnabled(false);

		// Custom Element Menu
		if (Program.PROGRAM_NAME == ProgramName.UMLET) {
			JMenu menu_custom = new JMenu(MenuFactory.CUSTOM_ELEMENTS);
			menu_custom.setMnemonic(KeyEvent.VK_C);
			menu_custom.add(customMenu = menuFactory.createNewCustomElement());
			menu_custom.add(customTemplate = menuFactory.createNewCustomElementFromTemplate());
			menu_custom.add(customEdit = menuFactory.createEditSelected());
			menu_custom.addSeparator();
			menu_custom.add(menuFactory.createCustomElementTutorial());
			menu.add(menu_custom);
			customEdit.setEnabled(false);
		}

		// Help Menu
		JMenu helpMenu = new JMenu(MenuFactory.HELP);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		helpMenu.add(menuFactory.createOnlineHelp());
		if (Program.PROGRAM_NAME == ProgramName.UMLET) {
			helpMenu.add(menuFactory.createOnlineSampleDiagrams());
			helpMenu.add(menuFactory.createVideoTutorials());
		}
		helpMenu.addSeparator();
		helpMenu.add(menuFactory.createProgramHomepage());
		helpMenu.add(menuFactory.createRateProgram());
		helpMenu.addSeparator();
		helpMenu.add(menuFactory.createAboutProgram());
		menu.add(helpMenu);

		menu.add(gui.createSearchPanel());

		menu.add(gui.createZoomPanel());

		mailButton = new JToggleButton("Mail diagram");

		mailButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setMailPanelEnabled(!isMailPanelVisible());
			}
		});
		menu.add(mailButton);

		// Set the finished menu
		this.window.setJMenuBar(menu);

		/************************ CREATE SUB PANELS ******************/

		// create custom element handler

		int mainDividerLoc = Math.min(window.getSize().width-Constants.MIN_MAIN_SPLITPANEL_SIZE, Constants.main_split_position);
		gui.initAll(gui.createDiagramTabPanel(), mainDividerLoc);

		gui.getCustomHandler().getPanel().setVisible(false);
		
		/********************* ADD KEYBOARD ACTIONS ************************/
		Action findaction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.getInstance().getGUI().enableSearch(true);
			}
		};
		this.getActionMap().put("focussearch", findaction);
		this.getInputMap().put(KeyStroke.getKeyStroke('/'), "focussearch");

		/************************ ADD TOP COMPONENT ************************/
		this.add(gui.getMailSplit());
		/************************** SET DEFAULT INITIALIZATION VALUES ******/
		ToolTipManager.sharedInstance().setInitialDelay(100);
		/*************************************************************/

		this.window.setVisible(true);
	}

	@Override
	public void showPalette(String palette) {
		super.showPalette(palette);
		CardLayout cl = (CardLayout) (this.gui.getPalettePanel().getLayout());
		cl.show(this.gui.getPalettePanel(), palette);
	}

	@Override
	public String getSelectedPalette() {
		return this.gui.getPaletteList().getSelectedItem().toString();
	}

	@Override
	public void close(DiagramHandler diagram) {
		gui.getDiagramtabs().remove(diagram.getDrawPanel().getScrollPane());
		DrawPanel p = this.getCurrentDiagram();
		if (p != null) Main.getInstance().setCurrentDiagramHandler(p.getHandler());
		else Main.getInstance().setCurrentDiagramHandler(null);
	}

	@Override
	public void open(DiagramHandler diagram) {
		JTabbedPane diagramtabs = gui.getDiagramtabs();
		diagramtabs.add(diagram.getName(), diagram.getDrawPanel().getScrollPane());
		diagramtabs.setTabComponentAt(diagramtabs.getTabCount() - 1, new TabComponent(diagramtabs, diagram));
		jumpTo(diagram);
	}

	@Override
	public void jumpTo(DiagramHandler diagram) {
		gui.getDiagramtabs().setSelectedComponent(diagram.getDrawPanel().getScrollPane());		
		diagram.getDrawPanel().getSelector().updateSelectorInformation();
		DrawPanel p = this.getCurrentDiagram();
		if (p != null) Main.getInstance().setCurrentDiagramHandler(p.getHandler());
		else Main.getInstance().setCurrentDiagramHandler(null);
	}

	@Override
	public DrawPanel getCurrentDiagram() {
		JScrollPane scr = (JScrollPane) gui.getDiagramtabs().getSelectedComponent();
		if (scr != null) return (DrawPanel) scr.getViewport().getView();
		else return null;
	}

	@Override
	public void elementsSelected(int count) {
		super.elementsSelected(count);
		if (count > 0) {
			editDelete.setEnabled(true);
			editCut.setEnabled(true);
			if (count > 1) editGroup.setEnabled(true);
			else editGroup.setEnabled(false);
		}
		else {
			editDelete.setEnabled(false);
			editGroup.setEnabled(false);
			editCut.setEnabled(false);
			// menu_edit_copy must remain enabled even if no entity is selected to allow the export of the full diagram to the system clipboard.
		}
	}

	@Override
	public void enablePasteMenuEntry() {
		editPaste.setEnabled(true);
	}

	@Override
	public void setUngroupEnabled(boolean enabled) {
		editUngroup.setEnabled(enabled);
	}

	@Override
	public void setCustomPanelEnabled(boolean enable) {
		gui.setCustomPanelEnabled(enable);
		this.customEdit.setEnabled(!enable && this.custom_element_selected);
		this.customMenu.setEnabled(!enable);
		this.customTemplate.setEnabled(!enable);

		setDiagramsEnabled(enable);
	}

	private void setDiagramsEnabled(boolean enable) {
		JTabbedPane diagramtabs = gui.getDiagramtabs();
		this.gui.getPalettePanel().setEnabled(enable);
		for (Component c : gui.getPalettePanel().getComponents())
			c.setEnabled(enable);
		diagramtabs.setEnabled(enable);
		for (Component c : diagramtabs.getComponents())
			c.setEnabled(enable);
		for (int i = 0; i < diagramtabs.getTabCount(); i++)
			diagramtabs.getTabComponentAt(i).setEnabled(enable);
		this.gui.getSearchField().setEnabled(enable);
	}

	@Override
	public void setMailPanelEnabled(boolean enable) {
		gui.setMailPanelEnabled(enable);
		mailButton.setSelected(false);
	}

	@Override
	public boolean isMailPanelVisible() {
		return gui.getMailPanel().isVisible();
	}

	@Override
	public void setCustomElementSelected(boolean selected) {
		this.custom_element_selected = selected;
		if (customEdit != null) customEdit.setEnabled(selected && !gui.getCustomPanel().isEnabled());
	}

	@Override
	public void diagramSelected(DiagramHandler handler) {
		updateGrayedOutMenuItems(handler);
	}

	public void updateGrayedOutMenuItems(DiagramHandler handler) {
		// These menuitems only get changed if this is not the palette or custompreview
		if (!(handler instanceof PaletteHandler) && !(handler instanceof CustomPreviewHandler)) {
			menuFactory.updateDiagramDependendComponents();

			if ((handler == null) || handler.getDrawPanel().getAllEntities().isEmpty()) {
				mailButton.setEnabled(false);
			}
			else {
				mailButton.setEnabled(true);
			}
		}

		// The menu_edit menuitems always work with the actual selected diagram (diagram, palette or custompreview), therefore we change it everytime
		if ((handler == null) || handler.getDrawPanel().getAllEntities().isEmpty()) {
			editCopy.setEnabled(false);
			editSelectAll.setEnabled(false);
		}
		else if (handler instanceof CustomPreviewHandler) {
			setCustomElementEditMenuEnabled(false);
		}
		else {
			editMenu.setEnabled(true); // must be set to enabled explicitely because it could be deactivated from CustomPreview
			setCustomElementEditMenuEnabled(true);
		}

		if ((handler == null) || !handler.getController().isUndoable()) editUndo.setEnabled(false);
		else editUndo.setEnabled(true);
		if ((handler == null) || !handler.getController().isRedoable()) editRedo.setEnabled(false);
		else editRedo.setEnabled(true);
	}

	@Override
	public void enableSearch(boolean enable) {
		gui.getSearchField().requestFocus();
	}

	private void setCustomElementEditMenuEnabled(boolean enabled)
	{
		editGroup.setEnabled(enabled);
		editUngroup.setEnabled(enabled);
		editDelete.setEnabled(enabled);
		editCut.setEnabled(enabled);
		editPaste.setEnabled(enabled);
		editCopy.setEnabled(enabled);
		editSelectAll.setEnabled(enabled);		
	}

	@Override
	public int getMainSplitPosition() {
		return this.gui.getMainSplit().getDividerLocation();
	}

	@Override
	public int getRightSplitPosition() {
		return this.gui.getRightSplit().getDividerLocation();
	}

	@Override
	public int getMailSplitPosition() {
		return Constants.mail_split_position; // must return stored value in Constants, otherwise 0 will be returned in case of a closed panel
	}

	@Override
	public JFrame getTopContainer() {
		return this.window;
	}

	@Override
	public CustomElementHandler getCurrentCustomHandler() {
		return gui.getCustomHandler();
	}

	@Override
	public OwnSyntaxPane getPropertyPane() {
		return gui.getPropertyTextPane();
	}

	@Override
	public void setValueOfZoomDisplay(int i) {
		JComboBox zoomComboBox = gui.getZoomComboBox();
		// This method should just set the value without ActionEvent therefore we remove the listener temporarily
		if (zoomComboBox != null) {
			zoomComboBox.removeActionListener(gui.getZoomListener());
			zoomComboBox.setSelectedIndex(i - 1);
			zoomComboBox.addActionListener(gui.getZoomListener());
		}
	}

	@Override
	public void focusPropertyPane() {
		gui.getPropertyTextPane().getTextComponent().requestFocus();
	}
}
