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

public class Model extends JPanel implements ChangeListener, ActionListener {
    private static final long serialVersionUID = 1L;

    private double viewHeight, viewAngle, coneHeight, coneLines, coneRings;

    private JSlider viewSlider;
    private JButton playButton, otherButton;

    private Simulator sim;

    private Sample sample;
    
    public Model(Simulator s){
	sim = s;
	coneHeight = 1.4;
	coneLines = 21;
	coneRings = 11;
	viewHeight = 0.62 * coneHeight;
	viewAngle = Math.toRadians(20);
	setLayout(new BorderLayout());
	viewSlider = new JSlider(JSlider.VERTICAL, 0, 90, 20);
	viewSlider.setPreferredSize(new Dimension(GBC.PIX1, GBC.PIX1));
	viewSlider.setForeground(Color.WHITE);
	viewSlider.setBackground(Color.BLACK);
	viewSlider.setValue(90 - (int)Math.toDegrees(viewAngle));
	viewSlider.addChangeListener(this);
	add(viewSlider, BorderLayout.WEST);
	playButton = new JButton("");
	playButton.setPreferredSize(new Dimension(GBC.PIX1, GBC.PIX1));
	playButton.setForeground(Color.WHITE);
	playButton.setBackground(Color.BLACK);
	playButton.addActionListener(this);
	playButton.setFont(GBC.FONT2);
	playButton.setText("play/pause");
	add(playButton, BorderLayout.SOUTH);
	otherButton = new JButton("");
	otherButton.setPreferredSize(new Dimension(GBC.PIX1, GBC.PIX1));
	otherButton.setForeground(Color.WHITE);
	otherButton.setBackground(Color.BLACK);
	otherButton.addActionListener(this);
	//	add(otherButton, BorderLayout.WEST);
	sample = new Sample();
	sample.update();
    }
    
    public void paintComponent(Graphics graphics){
	Graphics2D g = (Graphics2D) graphics;
	g.setColor(Color.BLACK);
	g.fillRect(0, 0, getWidth(), getHeight());
	g.setColor(Color.WHITE);
	g.setFont(GBC.FONT2);
	g.drawString("t: " + String.format("%.3f", sim.time), GBC.PIX1 * 2, GBC.PIX1);
	g.drawString("E: " + String.format("%.3f", sim.getEnergy()), getWidth() - GBC.PIX1 * 4, GBC.PIX1);
	g.translate(scale(1), scale(1));
	g.scale(1, -1);
	g.translate(GBC.PIX2 / 2, 0);
	// CONE BACKGROUND
	paintCone(g, 0.0, Math.PI);	
	// BILLIARDS
	paintBalls(g);
	// CONE FOREGROUND
	paintCone(g, Math.PI, 2 * Math.PI);

	g.translate(-GBC.PIX2 / 2, 0);
	g.scale(1, -1);
	g.translate(-scale(1), -scale(1));
    }

    public void paintBalls(Graphics2D g){
	g.setColor(Color.WHITE);
	int r = scale(sim.radius);
	double[] loc;
	for(Ball b : sample){
	    loc = project(b.getPos());
	    g.fillOval(scale(loc[0]) - r, scale(loc[1]) - r, 2 * r, 2 * r);
	}
    }

    public void paintCone(Graphics2D g, double start, double end){
	double inc = 2.0 * Math.PI / coneLines;
	double h = coneHeight;
	double t = sim.sintheta / sim.costheta;
	double p = t * h;
	double a = sim.phi % inc;
	if(start == 0.0 && a == 0.0) a = start + inc - start % inc;
	while(a < end){
	    g.drawLine(0, scale(-viewHeight * Math.cos(viewAngle)), scale(p * Math.cos(a)), scale((h - viewHeight) * Math.cos(viewAngle) + p * Math.sin(a) * Math.sin(viewAngle)));
	    a += inc;
	}
	inc = h / coneRings;
	while(h > 0){
	    g.drawArc(scale(-p), scale((h - viewHeight) * Math.cos(viewAngle) - p * Math.sin(viewAngle)), scale(2 * p), scale(2 * p * Math.sin(viewAngle)), (int)Math.toDegrees(2 * Math.PI - end), (int)Math.toDegrees(end - start));
	    h -= inc;
	    p = h * t;
	}
    }

    public double[] project(double[] p3d){
	double[] p2d = new double[2];
	p2d[0] = p3d[0];
	p2d[1] = p3d[1] * Math.sin(viewAngle) + (p3d[2] - viewHeight) * Math.cos(viewAngle);
	return p2d;
    }
    
    public int scale(double d){
	return (int)(d * getWidth() / 2.0);
    }
    
    public void stateChanged(ChangeEvent e) {
	if(e.getSource().equals(sim)){
	    sample.update();
	} else {
	    viewAngle = Math.toRadians(90 - viewSlider.getValue());
	} 
    }
    
    private class Ball {

	private int index;
	private double[] pos;
	//	private ArrayList<double[]> trace;
	
	public Ball(int i) {
	    index = i;
	    pos = new double[3];
	    sync();
	}

	public void sync(){
	    pos = sim.getBilliard(index).getPosXYZ();
 	}

	public double[] getPos(){
	    return pos;
	}
    }

    private class Sample extends ArrayList<Ball> {
	
	private static final long serialVersionUID = 1L;

	public void update(){
	    int num = sim.getBilliards().size();	  
	    int add = num - size();
	    if(add < 0) removeRange(num, size());
	    else for(int i = 0; i < add; i++) add(new Ball(num - add + i));   
	    for(Ball b : this){
		b.sync();
	    }
	}
    }

    public void actionPerformed(ActionEvent e) {
	if(sim.isStopped()){
	    sim.start();
	} else {
	    sim.stop();
	}

    }

}
