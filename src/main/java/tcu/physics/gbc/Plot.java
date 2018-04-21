package main.java.tcu.physics.gbc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Plot extends JPanel implements ActionListener, ChangeListener {
    private static final long serialVersionUID = 1L;

    private ArrayList<double[]> points;
    private boolean isTimeSeries;
    private Parameter xParam, yParam;

    private Simulator sim;

    private JButton clearButton, toggleButton;
    private JSlider viewSlider;

    private double viewAngle;
    
    public Plot(Simulator s){
	points = new ArrayList<double[]>();
	setParams(Parameter.TIME, Parameter.TIME);
	sim = s;
	setLayout(new BorderLayout());
	clearButton = new JButton("clear");
	clearButton.setPreferredSize(new Dimension(GBC.PIX1, GBC.PIX1));
	clearButton.setForeground(Color.WHITE);
	clearButton.setBackground(Color.BLACK);
	clearButton.addActionListener(this);
	clearButton.setFont(GBC.FONT2);
	add(clearButton, BorderLayout.SOUTH);
	toggleButton = new JButton("poincare section");
	toggleButton.setPreferredSize(new Dimension(GBC.PIX1, GBC.PIX1));
	toggleButton.setForeground(Color.WHITE);
	toggleButton.setBackground(Color.BLACK);
	toggleButton.addActionListener(this);
	toggleButton.setFont(GBC.FONT2);
	add(toggleButton, BorderLayout.NORTH);
	viewSlider = new JSlider(JSlider.VERTICAL, 0, 90, 20);
	viewSlider.setPreferredSize(new Dimension(GBC.PIX1, GBC.PIX1));
	viewSlider.setForeground(Color.WHITE);
	viewSlider.setBackground(Color.BLACK);
	viewSlider.setValue(90 - (int)Math.toDegrees(viewAngle));
	viewSlider.addChangeListener(this);
	add(viewSlider, BorderLayout.EAST);
	isTimeSeries = true;
	toggleIsTimeSeries();
    }
    
    @Override
    public void paintComponent(Graphics graphics){
	Graphics2D g = (Graphics2D) graphics;
	g.setColor(Color.BLACK);
	g.fillRect(0, 0, getWidth(), getHeight());
	g.setColor(Color.WHITE);
	g.setFont(GBC.FONT2);
	g.translate(0, getHeight());
	g.scale(1, -1);
	g.translate(GBC.PIX1, GBC.PIX1 + 1);
	double w = getWidth() - GBC.PIX1 * 2;
	double h = getHeight() - GBC.PIX1 * 2;
	double xmin, ymin, xs, ys;
	g.drawRect(0, 0, (int)w - 1, (int)h -1);
	if(!isTimeSeries){
	    xmin = xParam.getMin();
	    ymin = yParam.getMin();
	    xs = w / (xParam.getMax() - xmin);
	    ys = h / (yParam.getMax() - ymin);
	    g.setColor(Color.YELLOW);
	    for(double[] pt : points){
		g.fillRect((int)(xs * (pt[0] - xmin)), (int)(ys * (pt[1] - ymin)), 1, 1);
	    }
	    g.setColor(Color.LIGHT_GRAY);
	    if(sim.isStopped() && sim.getBilliards().size() == 1){
		int xloc = (int)(xs * (xParam.getValue(sim.getBilliards().get(0)) - xmin));
		int yloc = (int)(ys * (yParam.getValue(sim.getBilliards().get(0)) - ymin));
		g.drawLine(xloc, 0, xloc, (int)h);
		g.drawLine(0, yloc, (int)w, yloc);
	    }
	} else {
	    g.setColor(Color.YELLOW);
	    xmin = 0;
	    ymin = 0;
	    xs = w / 2.0;
	    ys = h / 2.0;
	    double[] prj;
	    for(double[] pt : points){
		prj = project(pt);
		g.fillRect((int)(xs * prj[0]), (int)(ys * prj[1]), 1, 1);
	    }
	}
	g.translate(-GBC.PIX1, -GBC.PIX1 - 1);
	g.scale(1, -1);
	g.translate(0, -getHeight());
    }

    public double[] project(double[] p3d){
	double[] p2d = new double[2];
	p2d[0] = p3d[0];
	p2d[1] = p3d[1] * Math.sin(viewAngle) + p3d[2] * Math.cos(viewAngle);
	return p2d;
    }

    public void toggleIsTimeSeries(){
	isTimeSeries = !isTimeSeries;
	viewSlider.setEnabled(isTimeSeries);
	toggleButton.setText(isTimeSeries ? "3D time series" : "poincare section");
	clearData();
    }

    public boolean isTimeSeries(){
	return isTimeSeries;
    }

    public void clearData(){
	points.clear();
    }

    public void logData(Billiard b){
	if(!isTimeSeries){
	    points.add(new double[]{xParam.getValue(b), yParam.getValue(b)});	    
	} else {
	    Double[] arr = b.getRoots();
	    if(arr != null) {
		points.add(new double[]{arr[0], arr[1], arr[2]});
	    }
	}
    }
    
    public void setParams(Parameter x, Parameter y){
	xParam = x;
	yParam = y;
    }

    public void actionPerformed(ActionEvent e) {
	if(e.getSource().equals(clearButton)){
	    points.clear();   
	} else {
	    GBC.toggleFullscreen(this);
	    updateUI();
	}
    }

    public void stateChanged(ChangeEvent e) {
	viewAngle = Math.toRadians(90 - viewSlider.getValue());
    }

}

