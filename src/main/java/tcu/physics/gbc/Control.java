package main.java.tcu.physics.gbc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Control extends JPanel implements ChangeListener, ItemListener, ActionListener{

    private static final long serialVersionUID = 1L;

    private static final int pointsPerValue = 1000;
    
    private GBCSlider ballPosR, ballVelT, ballVelP, ke, amp;
    private GBCSlider coneAngle, mass, gravity, radius, muS, muK, e;
    private GBCComboBox xAxis, yAxis;
    private GBCButton EButton, sampleButton, bindButton, drivingButton, dataButton, resetButton, speedButton, frictionButton;
    public Double energy, moml;

    private GBCLabel versusLabel;
    private GridBagConstraints dataConstraints;

    private Simulator sim;
    private Model model;
    private Plot plot;

    private boolean sample;
    public boolean driving, sticky;
    	
    public Control(Simulator s){
	sim = s;

	sample = false;
	energy = Double.NaN;
	moml = Double.NaN;
	bindButton = new GBCButton("Bind E,\u2113\u00B7z", this);
	sampleButton = new GBCButton("Sample r", this);
	drivingButton = new GBCButton("Add Driving", this);
	frictionButton = new GBCButton("Add Friction", this);
	
	xAxis = new GBCComboBox(this);
	yAxis = new GBCComboBox(this);
	versusLabel = new GBCLabel("vs.", GBC.FONT1);
	dataButton = new GBCButton("\u205c", this);
	resetButton = new GBCButton("\u21BB", this);
	speedButton = new GBCButton("\u23E9", this);
	
	coneAngle = new GBCSlider("angle of cone surface from vertical", JSlider.HORIZONTAL, this);
	mass = new GBCSlider("mass of billiard", JSlider.HORIZONTAL, this);
	gravity = new GBCSlider("gravitational acceleration", JSlider.HORIZONTAL, this);
	radius = new GBCSlider("radius of billiard", JSlider.HORIZONTAL, this);
	muS = new GBCSlider("coefficient of static friction", JSlider.HORIZONTAL, this);
	muK = new GBCSlider("coefficient of kinetic friction", JSlider.HORIZONTAL, this);
	e = new GBCSlider("coefficient of restitution", JSlider.HORIZONTAL, this);
	
	ballPosR = new GBCSlider("distance from origin", JSlider.HORIZONTAL, this);
	ballVelT = new GBCSlider("angle of velocity vector from surface", JSlider.HORIZONTAL, this);
	ballVelP = new GBCSlider("angle of velocity vector from x-z plane", JSlider.HORIZONTAL, this);
	amp = new GBCSlider("magnitude of driving", JSlider.HORIZONTAL, this);
	ke = new GBCSlider("kinetic energy of billiard", JSlider.HORIZONTAL, this);
	
	EButton = new GBCButton("e", this);
	
	coneAngle.setScale(0, 90, 5, 15, false);
	mass.setScale(0, 1, 0.1, 0.5, true);
	gravity.setScale(0, 10, 0.5, 2, false);
	radius.setScale(0, 0.1, 0.01, 0.05, true);
	ballPosR.setScale(0, 1, 0.1, 0.5, true);
	ballVelT.setScale(0, 180, 30, 90, false);
	ballVelP.setScale(0, 90, 5, 15, false);
	ke.setScale(0, 5, 0.5, 1, false);
	muS.setScale(0, 2, 0.5, 1, false);
	muK.setScale(0, 2, 0.5, 1, false);
	e.setScale(0.95, 1.0, 0.01, 0.05, true);
	amp.setScale(0, 10, 1, 5, false);
	
	coneAngle.setValueDouble(30);
	mass.setValueDouble(1.0);
	gravity.setValueDouble(1.0);
	radius.setValueDouble(0.025);
	ballPosR.setValueDouble(0.5);
	ballVelT.setValueDouble(90);
	ballVelP.setValueDouble(45);
	ke.setValueDouble(0.5);
	muS.setValueDouble(1.0);
	muK.setValueDouble(1.0);
	e.setValueDouble(1.0);
	amp.setValueDouble(2.0);

	sim.initialize(this);
	model = new Model(sim);
	plot = new Plot(sim);

	xAxis.setSelectedIndex(2);
	yAxis.setSelectedIndex(5);
	toggleBind();
	sticky = true;
	toggleSticky();

	setBackground(Color.BLACK);
	setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.HORIZONTAL;
	int inset = GBC.PIX2 / 2;
	c.insets = new Insets(inset,inset,inset,inset);
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 1;
	c.gridheight = 1;
	add(speedButton, c);
	c.gridwidth = 2;
	c.gridx ++;
	add(new GBCLabel("initial conditions", GBC.FONT1), c);
	c.gridx = 4;
	c.gridy++;
	c.gridwidth = 2;
	add(frictionButton, c);
	c.gridx += 2;
	add(drivingButton, c);
	c.gridx -= 3;
	c.gridy--;;
	c.gridwidth = 1;
	add(resetButton, c);
	c.gridx++;
	add(dataButton, c);
	c.gridx++;	
	dataConstraints = (GridBagConstraints)c.clone();
	add(yAxis, c);
	c.gridx++;
	add(versusLabel, c);
	c.gridx++;
	add(xAxis, c);
	c.gridx = 0;
	c.gridy = 1;
	add(new GBCLabel("r =", GBC.FONT1), c);
	c.gridy++;
	add(new GBCLabel("\u03C6 =", GBC.FONT1), c);
	c.gridy++;
	add(new GBCLabel("KE =", GBC.FONT1), c);
	c.gridy++;
	add(new GBCLabel("A =", GBC.FONT1), c);
	c.gridx = 1;
	c.gridy = 1;
	add(ballPosR.getField(), c);
	c.gridy++;
	add(ballVelT.getField(), c);
	c.gridy++;
	//	add(ballVelP.getField(), c);
	//	c.gridy++;
	add(ke.getField(), c);
	c.gridy++;
	add(amp.getField(), c);
	c.gridx = 2;
	c.gridy = 1;
	add(ballPosR, c);
	c.gridy++;
	add(ballVelT, c);
	c.gridy++;
	//	add(ballVelP, c);
	//	c.gridy++;
	add(ke, c);
	c.gridy++;
	add(amp, c);

	c.gridy++;
	c.gridx -= 2;
	c.gridwidth++;
	add(bindButton, c);
	c.gridx += 2;
	c.gridwidth--;
	add(sampleButton, c);
	
	c.gridx = 4;
	c.gridy = 2;
	//	add(new GBCLabel("m", GBC.FONT1), c);
	add(new GBCLabel("\u03BCs =", GBC.FONT1), c);
	c.gridy++;
	//	add(new GBCLabel("g", GBC.FONT1), c);
	add(new GBCLabel("\u03BCk =", GBC.FONT1), c);	
	c.gridy++;
	//	add(new GBCLabel("\u03B1", GBC.FONT1), c);
	add(EButton, c);
	c.gridy++;
	add(new GBCLabel("\u03B8 =", GBC.FONT1), c);
	c.gridx = 5;
	c.gridy = 2;
	//	add(mass.getField(), c);
	add(muS.getField(), c);
	c.gridy++;
	//	add(gravity.getField(), c);
	add(muK.getField(), c);
	c.gridy++;
	//	add(radius.getField(), c);
	add(e.getField(), c);
	c.gridy++;
	add(coneAngle.getField(), c);
	c.gridx = 6;
	c.gridy = 2;
	c.gridwidth = 2;
	//	add(mass, c);
	add(muS, c);
	c.gridy++;
	//	add(gravity, c);
	add(muK, c);
	c.gridy++;
	//	add(radius, c);
	add(e, c);
	c.gridy++;
	add(coneAngle, c);
    }

    public double getBallPosR(){
	if(sample) return Double.NaN;
	return ballPosR.getValueDouble();      
    }

    public double getBallVelT(){
	return ballVelT.getValueDouble();
    }
    
    public double getBallVelP(){
	return ballVelP.getValueDouble();
    }

    public double getKE(){
	return ke.getValueDouble();
    }
    
    public double getConeAngle(){
	return coneAngle.getValueDouble();
    }

    public double getMass(){
	return mass.getValueDouble();
	
    }

    public double getAmp(){
	return amp.getValueDouble();
    }

    public double getGravity(){
	return gravity.getValueDouble();
    }

    public double getRadius(){
	return radius.getValueDouble();
    }

    public double getMuS(){
	return muS.getValueDouble();
    }

    public double getMuK(){
	return muK.getValueDouble();
    }

    public double getE(){
	return e.getValueDouble();
    }

    public void stateChanged(ChangeEvent e) {
	GBCSlider slider = (GBCSlider) e.getSource();
	slider.updateField();
	updateSim();
    }

    public Model getModel(){
	return model;
    }

    public Plot getPlot(){
	return plot;
    }

    public void toggleBind(){
	if(energy.isNaN() || moml.isNaN()){
	    energy = getKE() + getMass() * getGravity() * getBallPosR() * sim.costheta;
	    moml = Math.sin(Math.toRadians(getBallVelP())) * sim.sintheta * getBallPosR() * Math.sqrt(2 * getMass() * getKE());
	    bindButton.setText("Unbind E,\u2113\u00B7z");
	    sampleButton.setEnabled(true);
	} else {
	    energy = Double.NaN;
	    moml = Double.NaN;
	    bindButton.setText("Bind E,\u2113\u00B7z");
	    if(sample) toggleSample();
	    sampleButton.setEnabled(false);
	}
	ballVelP.setEnabled(moml.isNaN());
	ballVelP.getField().setEnabled(moml.isNaN());
	updateSim();
    }
    
    public void toggleSample(){
	if(!energy.isNaN() && !moml.isNaN()){
	    sample = !sample;
	    sampleButton.setText(sample ? "Specify r" : "Sample r");
	    ballPosR.setEnabled(!sample);
	    ballPosR.getField().setEnabled(!sample);
	    bindButton.setEnabled(!sample);
	    updateSim();
	}
    }

    public void toggleSticky(){
	sticky = !sticky;
	muK.setEnabled(!sticky);
	muK.getField().setEnabled(!sticky);
	muS.setEnabled(!sticky);
	muS.getField().setEnabled(!sticky);
	frictionButton.setText(sticky ? "Add Slipping" : "Make Sticky");
	updateSim();
    }

    public void toggleElasticity(){
	if(EButton.getText().equals("e")){
	    e.setScale(0, 1000, 100, 500, false);
	    e.setValueDouble(500);
	    EButton.setText("c");
	} else{
	    e.setScale(0.95, 1.0, 0.01, 0.05, true);
	    e.setValueDouble(1.0);
	    EButton.setText("e");
	}
	updateSim();
    }

    public void toggleSpeed(){
	sim.setSpeedy(!sim.isSpeedy());
    }

    public void toggleDriving(){
	driving = !driving;
	drivingButton.setText(driving ? "Remove Driving" : "Add Driving");
	updateSim();
    }

    public void toggleData(){
	if(plot.isTimeSeries()){
	    GridBagConstraints c = (GridBagConstraints)dataConstraints.clone();
	    add(yAxis, c);
	    c.gridx++;
	    add(versusLabel, c);
	    c.gridx++;
	    add(xAxis, c);
	} else {
	    remove(xAxis);
	    remove(versusLabel);
	    remove(yAxis);
	}
	plot.toggleIsTimeSeries();
    }
    
    private void updateSim(){
	sim.initialize(this);
	if(model != null) model.stateChanged(new ChangeEvent(sim));	
    }

    @Override
    public void setPreferredSize(Dimension d){
	super.setPreferredSize(d);
	int w = (int)(d.getWidth() / 2.0);
	model.setPreferredSize(new Dimension(w, w));
	plot.setPreferredSize(new Dimension(w, w));
    }
    
    public class GBCLabel extends JLabel{
	private static final long serialVersionUID = 1L;

	public GBCLabel(String s, Font font){
	    super(s);
	    setFont(font);
	    setForeground(Color.WHITE);
	}
    }

    public class GBCSlider extends JSlider implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private GBCTextField field;

	public GBCSlider(String name, int dir, ChangeListener cl){
	    super(dir);
	    setPaintTicks(true);
	    setPaintLabels(true);
	    setBackground(Color.BLACK);
	    setForeground(Color.WHITE);
	    setFont(GBC.FONT2);
	    setToolTipText(name);
	    addChangeListener(cl);
	    setPreferredSize(new Dimension(7 * GBC.PIX1, 2 * GBC.PIX1));
	    field = new GBCTextField(this);
	    field.setToolTipText(name);
	    updateField();
	}

	public void setScale(double min, double max, double minor, double major, boolean decimal){
	    setMinimum((int)(pointsPerValue * min));
	    setMaximum((int)(pointsPerValue * max));
	    setMinorTickSpacing((int)(pointsPerValue * minor));
	    setMajorTickSpacing((int)(pointsPerValue * major));
	    Hashtable<Integer, JLabel> decLabelTable = new Hashtable<Integer, JLabel>();
	    int minVal = (int)(min * pointsPerValue);
	    int maxVal = (int)(max * pointsPerValue);
	    int majSpac = (int)(major * pointsPerValue);
	    String s;
	    for(int i = minVal; i <= maxVal; i += majSpac){
		s = decimal ? Double.toString((double) i / pointsPerValue) : Integer.toString(i / pointsPerValue);
		decLabelTable.put(new Integer(i), new GBCLabel(s, GBC.FONT2));
	    }
	    setLabelTable(decLabelTable);
	}

	public GBCTextField getField(){
	    return field;
	}

	public double getValueDouble(){
	    return (double) getValue() / (double) pointsPerValue;
	}

	public void setValueDouble(double d){
	    setValue((int)(pointsPerValue * d));
	}
	
	public void propertyChange(PropertyChangeEvent e) {
	    setValueDouble(field.getValueDouble());
	    updateField();
	    getChangeListeners()[0].stateChanged(new ChangeEvent(this));
	}

	public void updateField(){
	    field.setValueDouble(getValueDouble());
	}
    }

    public class GBCTextField extends JFormattedTextField {
	private static final long serialVersionUID = 1L;
	
	public GBCTextField(PropertyChangeListener pcl){
	    super(NumberFormat.getNumberInstance());
	    setFont(GBC.FONT2);
	    setPreferredSize(new Dimension(4 * GBC.PIX1, GBC.PIX1));
	    setScrollOffset(0);
	    addPropertyChangeListener("value", pcl);
	}

	public double getValueDouble(){
	    double d;
	    try{
		d = Double.parseDouble(getText());
	    } catch (Exception e){
		return Double.NEGATIVE_INFINITY;
	    }
	    return d;
	}

	public void setValueDouble(double d){
	    setText(Double.toString(d));
	}
    }

    public class GBCComboBox extends JComboBox<Parameter> {
	private static final long serialVersionUID = 1L;

	public GBCComboBox(ItemListener il){
	    super(Parameter.getAll());
	    setPreferredSize(new Dimension(3 * GBC.PIX1, GBC.PIX1));
	    setFont(GBC.FONT2);
	    addItemListener(il);
	}
    }

    public class GBCButton extends JButton{
	private static final long serialVersionUID = 1L;

	public GBCButton(String s, ActionListener al){
	    super(s);
	    setPreferredSize(new Dimension((int)(GBC.PIX1 * 2.5), GBC.PIX1));
	    addActionListener(al);
	    setFont(GBC.FONT2);
	}
    }
    
    public void itemStateChanged(ItemEvent e) {
	plot.clearData();
	plot.setParams((Parameter)xAxis.getSelectedItem(), (Parameter)yAxis.getSelectedItem());
    }

    public void actionPerformed(ActionEvent e) {
	GBCButton source = (GBCButton) e.getSource();
	if(source.equals(sampleButton)){
	    toggleSample();
	} else if(source.equals(frictionButton)){
	    toggleSticky();
	} else if(source.equals(EButton)){
	    toggleElasticity();
	}else if(source.equals(bindButton)){
	    toggleBind();
	} else if(source.equals(drivingButton)){
	    toggleDriving();
	} else if(source.equals(dataButton)){
	    toggleData();
	} else if(source.equals(resetButton)){
	    updateSim();
	} else if(source.equals(speedButton)){
	    toggleSpeed();
	}
    }
}
