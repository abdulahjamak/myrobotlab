package org.myrobotlab.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.InMoovGestureCreator;
import org.myrobotlab.service.SwingGui;
import org.myrobotlab.service.model.Frame;
import org.myrobotlab.service.model.Frame.RobotSection;
import org.slf4j.Logger;

/**
 * based on _TemplateServiceGUI
 */
/**
 *
 * @author LunDev (github), Ma. Vo. (MyRobotlab)
 */
public class InMoovGestureCreatorGui extends ServiceGui implements ActionListener, ItemListener {

	static final long serialVersionUID = 1L;
	private final static Logger LOGGER = LoggerFactory.getLogger(InMoovGestureCreatorGui.class);
	private static final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
			.getNumberInstance(Locale.getDefault());

	private final JFormattedTextField gestureName = new JFormattedTextField("Gesture Name");

	private final JButton controlConnect = new JButton("Connect");
	private final JButton loadScriptFolder = new JButton("Open Folder");
	private final JButton loadGestureScript = new JButton("Load");
	private final JButton controlSaveScript = new JButton("Save");
	private final JButton controlNewGesture = new JButton("New");
	private final JButton controlExecuteGesture = new JButton("Execute");
	private final JFormattedTextField frameNameTextField = new JFormattedTextField();
	private final JFormattedTextField frameSleepTextField = new JFormattedTextField(decimalFormat);
	private final JFormattedTextField frameSpeechTextField = new JFormattedTextField();
	
	private final JButton frameNew = new JButton("New");
	private final JButton frameRemove = new JButton("Remove");
	private final JButton frameCopy = new JButton("Copy");
	private final JButton frameUp = new JButton("Up");
	private final JButton frameDown = new JButton("Down");
	private final JButton frameExecute = new JButton("Execute");
	private final JCheckBox frameMoveRealTime = new JCheckBox("Move Real Time");

	private static final String[] GESTURE_LIST_PLACEHOLDER = { "Load folder with scripts" };
	private static final String[] FRAME_LIST_PLACEHOLDER = {"Load a script to see frames..."}; 
	
	private final DefaultListModel<String> frameListModel = new DefaultListModel<String>();

	private final JList<String> gestureList = new JList<String>(GESTURE_LIST_PLACEHOLDER);
	private final JList<String> frameList = new JList<String>(frameListModel);
	
	private final JPanel bottomTop = new JPanel();
	private final JPanel top = new JPanel();

  	private final Map<RobotSection, JCheckBox> robotSectionMoveSetCheckboxes = new HashMap<RobotSection, JCheckBox>();
  	private final Map<RobotSection, JCheckBox> robotSectionSpeedSetCheckboxes = new HashMap<RobotSection, JCheckBox>();
  	private final Map<RobotSection, List<JSlider>> robotSectionMoveSliders = new HashMap<RobotSection, List<JSlider>>();
  	private final Map<RobotSection, List<JFormattedTextField>> robotSectionSpeedTextBoxes = new HashMap<RobotSection, List<JFormattedTextField>>();

  	public InMoovGestureCreatorGui(final String boundServiceName, final SwingGui myService) {
  		super(boundServiceName, myService);
		LOGGER.info("InMoovGestureCreatorGui constructor [START]");
		try {
			decimalFormat.setGroupingUsed(false);
			frameListModel.addElement(FRAME_LIST_PLACEHOLDER[0]);

			// display:
			// |--------------------|
			// |####################|
			// |########top#########|
			// |####################|
			// |--------------------| <- splitpanetopbottom
			// |######bottom########|
			// |####################|
			// |--------------------|

			// top:
			// |--------------------|
			// |top#| ## top 2 #####|
			// |# 1#| ##############|
			// |--------------------|

			JPanel topLeft = new JPanel();
			topLeft.setLayout(new BorderLayout());

			JPanel topLeftTop = new JPanel();
			topLeftTop.setLayout(new BoxLayout(topLeftTop, BoxLayout.X_AXIS));

			JLabel gesturePanelLabel = new JLabel("Gestures");
			topLeftTop.add(gesturePanelLabel);
			
			topLeftTop.add(controlConnect);
			controlConnect.addActionListener(this);

			topLeft.add(BorderLayout.NORTH, topLeftTop);

			JPanel topLeftRight = new JPanel();
			topLeftRight.setLayout(new BoxLayout(topLeftRight, BoxLayout.Y_AXIS));

			topLeftRight.add(loadScriptFolder);
			loadScriptFolder.addActionListener(this);

			topLeftRight.add(loadGestureScript);
			loadGestureScript.addActionListener(this);

			topLeftRight.add(controlSaveScript);
			controlSaveScript.addActionListener(this);

			topLeftRight.add(controlNewGesture);
			controlNewGesture.addActionListener(this);

			topLeftRight.add(controlExecuteGesture);
			controlExecuteGesture.addActionListener(this);

			topLeft.add(BorderLayout.EAST, topLeftRight);
			
			gestureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane gestureListScroller = new JScrollPane(gestureList);
			gestureListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			gestureListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			topLeft.add(BorderLayout.CENTER, gestureListScroller);

			JPanel topRight = new JPanel();
			topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));

			JPanel topRightTop = new JPanel();
			topRightTop.setLayout(new BoxLayout(topRightTop, BoxLayout.X_AXIS));

			JLabel gestureNameLabel = new JLabel("Gesture name");
			topRightTop.add(gestureNameLabel);
			
			PropertyChangeListener gestureNameTextListener = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                	myService.send(boundServiceName, "updateGestureName", text);	
		        }
		    };
		    gestureName.addPropertyChangeListener("value", gestureNameTextListener);
			topRightTop.add(gestureName);

			topRightTop.add(frameNew);
			frameNew.addActionListener(this);
			
			topRightTop.add(frameRemove);
			frameRemove.addActionListener(this);

			topRightTop.add(frameCopy);
			frameCopy.addActionListener(this);

			topRightTop.add(frameUp);
			frameUp.addActionListener(this);

			topRightTop.add(frameDown);
			frameDown.addActionListener(this);

			topRightTop.add(frameExecute);
			frameExecute.addActionListener(this);

			topRight.add(BorderLayout.NORTH, topRightTop);

			frameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			JScrollPane frameListScroller = new JScrollPane(frameList);
			frameListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			frameListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			topRight.add(BorderLayout.CENTER, frameListScroller);
			
			frameList.addListSelectionListener(new ListSelectionListener() {
	            @Override
	            public void valueChanged(ListSelectionEvent arg0) {
	                if (!arg0.getValueIsAdjusting()) {
	                	myService.send(boundServiceName, "frameSelectionChanged", 
	                			frameList,
	                			frameListModel,
	    						frameNameTextField,
	    						frameSleepTextField,
	    						frameSpeechTextField,
	                			robotSectionMoveSetCheckboxes,
	                			robotSectionSpeedSetCheckboxes,
	                			robotSectionMoveSliders,
	                			robotSectionSpeedTextBoxes);	
	                }
	            }
	        });

			JPanel bottom = new JPanel();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
			
			Dimension maximumSize = new Dimension(Integer.MAX_VALUE, 50);
			bottomTop.setMaximumSize(maximumSize);
			bottomTop.setMinimumSize(maximumSize);
			bottomTop.setLayout(new BoxLayout(bottomTop, BoxLayout.X_AXIS));	
			bottomTop.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
			
			JPanel bottomBottom = new JPanel();
			bottomBottom.setLayout(new BoxLayout(bottomBottom, BoxLayout.X_AXIS));
			
			for (RobotSection robotSection : RobotSection.values()) {
				final JPanel sectionPanel = new JPanel();
				sectionPanel.setLayout(new BorderLayout());
				sectionPanel.add(BorderLayout.NORTH, new JLabel(Frame.getSectionLabel(robotSection)));
				sectionPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1)); 
				bottomBottom.add(sectionPanel);
				final JPanel robotSectionMovePanel = new JPanel();
				robotSectionMovePanel.setLayout(new BoxLayout(robotSectionMovePanel, BoxLayout.Y_AXIS));
				addEnableCheckBoxesToSectionPane(myService, robotSectionMovePanel, "Move?", robotSection, true);
				final JPanel robotSectionSlidersPanel = new JPanel();
				robotSectionSlidersPanel.setLayout(new BoxLayout(robotSectionSlidersPanel, BoxLayout.X_AXIS));
				addMoveSlidersToSectionPane(myService, robotSectionSlidersPanel, robotSection);
				robotSectionMovePanel.add(robotSectionSlidersPanel);
				final JPanel robotSectionSpeedPanel = new JPanel();
				robotSectionSpeedPanel.setLayout(new BoxLayout(robotSectionSpeedPanel, BoxLayout.Y_AXIS));
				sectionPanel.add(BorderLayout.CENTER, robotSectionMovePanel);
				sectionPanel.add(BorderLayout.SOUTH, robotSectionSpeedPanel);
				addEnableCheckBoxesToSectionPane(myService, robotSectionSpeedPanel, "Set Speed?", robotSection, false);
				final JPanel robotSectionSpeedNumberBoxesPanel = new JPanel();
				robotSectionSpeedNumberBoxesPanel.setLayout(new BoxLayout(robotSectionSpeedNumberBoxesPanel, BoxLayout.X_AXIS));
				addSpeedTextToSectionPane(myService, robotSectionSpeedNumberBoxesPanel, robotSection);
				robotSectionSpeedPanel.add(robotSectionSpeedNumberBoxesPanel);
			}
			
			frameMoveRealTime.addChangeListener(new ChangeListener() {
	            @Override
	            public void stateChanged(ChangeEvent e) {
                	myService.send(boundServiceName, "updateMoveRealTime", frameMoveRealTime.isSelected());	
	            }
	        });
			frameMoveRealTime.setSelected(false);
			bottomTop.add(frameMoveRealTime);
			
			JLabel frameNameLabel = new JLabel("Frame Name");
			bottomTop.add(frameNameLabel);			

			PropertyChangeListener frameNameTextListener = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                	myService.send(boundServiceName, "updateFrameName", frameList, text);	
		        }
		    };
		    frameNameTextField.addPropertyChangeListener("value", frameNameTextListener);
			bottomTop.add(frameNameTextField);
			JLabel speechLabel = new JLabel("Speech");
			bottomTop.add(speechLabel);	

			PropertyChangeListener frameSpeechTextListener = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                	myService.send(boundServiceName, "updateFrameSpeech", frameList, frameListModel, text);	
		        }
		    };
		    frameSpeechTextField.addPropertyChangeListener("value", frameSpeechTextListener);	
			bottomTop.add(frameSpeechTextField);

			JLabel sleepLabel = new JLabel("Sleep (s)");
			bottomTop.add(sleepLabel);

			PropertyChangeListener frameSleepTextListener = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                	myService.send(boundServiceName, "updateFrameSleep", frameList, frameListModel, Integer.valueOf(text));	
		        }
		    };
		    frameSleepTextField.addPropertyChangeListener("value", frameSleepTextListener);			
			frameSleepTextField.setColumns(3);
			bottomTop.add(frameSleepTextField);
			
			bottom.add(bottomTop);
			bottom.add(bottomBottom);

			JSplitPane splitPaneTopLeftTopRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topLeft, topRight);
			splitPaneTopLeftTopRight.setOneTouchExpandable(true);
			// splitpanebottom1bottom2.setDividerLocation(200);

			top.add(splitPaneTopLeftTopRight);

			JSplitPane splitPaneTopBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
			splitPaneTopBottom.setOneTouchExpandable(true);
			// splitpanetopbottom.setDividerLocation(300);

			display.add(splitPaneTopBottom);
			// initilaize with a blank FRAME
			swingGui.send(boundServiceName, "clearGestureAndSelectedFrame", 
					frameListModel,
					frameNameTextField,
					frameSleepTextField,
					frameSpeechTextField,
        			robotSectionMoveSetCheckboxes,
        			robotSectionSpeedSetCheckboxes,
        			robotSectionMoveSliders,
        			robotSectionSpeedTextBoxes);
		} catch (Exception e) {
			LOGGER.warn("Exception occured", e);
		}

		LOGGER.info("InMoovGestureCreatorGui constructor [END]");
	}

	public void addBottomTopPane(final SwingGui myService) {
		LOGGER.trace("addBottomTopPane [START]");
		try {
		} catch (Exception e) {
			LOGGER.warn("addBottomTopPane error: ", e);
		}
		LOGGER.trace("addBottomTopPane [END]");
	}

	private void addMoveSlidersToSectionPane(final SwingGui myService, final JPanel panel, final RobotSection robotSection) {
		LOGGER.trace("addMoveSlidersToSectionPane for \"" + robotSection + "\"");
		List<JSlider> sliders = new ArrayList<JSlider>();
		for(int i = 0; i < Frame.getSubSectionSize(robotSection); i++) {
			// preset the slider			
			final int sectionIndex = i;
			final JLabel sliderLabel = new JLabel(Frame.getSectionLabel(robotSection, sectionIndex));
			final JSlider slider = new JSlider();
			slider.setOrientation(SwingConstants.VERTICAL);
			slider.setMinimum(0);
			slider.setMaximum(180);
			slider.setMajorTickSpacing(20);
			slider.setMinorTickSpacing(1);
			slider.createStandardLabels(1);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent ce) {
                	myService.send(boundServiceName, "updateFrameSliders", frameList, frameListModel, robotSection, sectionIndex, slider.getValue(), slider.getValueIsAdjusting());
				}
			});
			final JPanel sliderLabelContainer = new JPanel();
			sliderLabelContainer.setLayout(new BoxLayout(sliderLabelContainer, BoxLayout.Y_AXIS));
			sliderLabelContainer.add(sliderLabel);
			sliderLabelContainer.add(slider);
			panel.add(sliderLabelContainer);
			sliders.add(slider);
		}		
		robotSectionMoveSliders.put(robotSection, sliders);
	}
	
	private void addSpeedTextToSectionPane(final SwingGui myService, final JPanel panel, final RobotSection robotSection) {
		LOGGER.trace("addSpeedTextToSectionPane for \"" + robotSection +  "\"");
		List<JFormattedTextField> fields = new ArrayList<JFormattedTextField>();
		for(int i = 0; i < Frame.getSubSectionSize(robotSection); i++) {
			final JFormattedTextField frameSpeed = new JFormattedTextField(decimalFormat);
			frameSpeed.setColumns(3);
			final int sectionIndex = i;
			PropertyChangeListener l = new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent evt) {
		            String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                	myService.send(boundServiceName, "updateFrameSpeed", frameList, frameListModel, robotSection, sectionIndex, Double.valueOf(text));	
		        }
		    };
		    frameSpeed.addPropertyChangeListener("value", l);
			panel.add(frameSpeed);
			fields.add(frameSpeed);
		}
		robotSectionSpeedTextBoxes.put(robotSection, fields);
	}

	private void addEnableCheckBoxesToSectionPane(final SwingGui myService, final JPanel panel, final String title, 
			final RobotSection robotSection, final boolean move) {
		final JCheckBox checkbox = new JCheckBox(title);
		checkbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
            	myService.send(boundServiceName, "updateFrameBooleans", frameList, frameListModel, robotSection, checkbox.isSelected(), move);	
			}
		});
		panel.add(checkbox);

		if (move) {
			robotSectionMoveSetCheckboxes.put(robotSection, checkbox);
		} else {
			robotSectionSpeedSetCheckboxes.put(robotSection, checkbox);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();

		// Button - Events
		if (o == controlConnect) {
			swingGui.send(boundServiceName, "controlConnect", controlConnect);
		} else if (o == loadScriptFolder) {
			swingGui.send(boundServiceName, "loadScriptFolder", gestureList);
		} else if (o == loadGestureScript) {
			swingGui.send(boundServiceName, "loadGestureScript", gestureList, frameListModel, gestureName);
		} else if (o == controlSaveScript) {
			swingGui.send(boundServiceName, "controlSaveScript");
		} else if (o == controlNewGesture) {
			this.gestureName.setText("Gesture name here");
			swingGui.send(boundServiceName, "clearGestureAndSelectedFrame", 
					frameList,
					frameNameTextField,
					frameSleepTextField,
					frameSpeechTextField,
        			robotSectionMoveSetCheckboxes,
        			robotSectionSpeedSetCheckboxes,
        			robotSectionMoveSliders,
        			robotSectionSpeedTextBoxes);
		} else if (o == controlExecuteGesture) {
			swingGui.send(boundServiceName, "gestureExecute", frameList);
		} else if (o == frameNew) {
			swingGui.send(boundServiceName, "frameNew", frameList, frameListModel);
		} else if (o == frameRemove) {
			swingGui.send(boundServiceName, "frameRemove", frameList, frameListModel);
		} else if (o == frameCopy) {
			swingGui.send(boundServiceName, "frameCopy", frameList, frameListModel);
		} else if (o == frameUp) {
			swingGui.send(boundServiceName, "frameUp", frameList, frameListModel);
		} else if (o == frameDown) {
			swingGui.send(boundServiceName, "frameDown", frameList, frameListModel);
		} else if (o == frameExecute) {
			swingGui.send(boundServiceName, "frameExecute", frameList);
		}
		swingGui.send(boundServiceName, "publishState");
	}

	@Override
	public void subscribeGui() {
		// commented out subscription due to this class being used for
		// un-defined gui's

		// subscribe("publishState", "onState", _TemplateService.class);
		// send("publishState");
	}

	@Override
	public void unsubscribeGui() {
		// commented out subscription due to this class being used for
		// un-defined gui's

		// unsubscribe("publishState", "onState", _TemplateService.class);
	}

	public void onState(final InMoovGestureCreator inMoovGestureCreator) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

			}
		});
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
//		Object o = ie.getSource();
		// CheckBox - Events
//		if (o == frameMoveRealTime) {
//			swingGui.send(boundServiceName, "frameMoveRealTime", frameMoveRealTime);
//		}
	}
}