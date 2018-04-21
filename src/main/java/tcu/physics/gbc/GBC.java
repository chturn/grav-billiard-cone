package main.java.tcu.physics.gbc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

public class GBC extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
    
    public static final int SCREEN_RESOLUTION = defaultToolkit.getScreenResolution();
    public static final int PIX1 = GBC.SCREEN_RESOLUTION / 5;
    public static final int PIX2 = GBC.SCREEN_RESOLUTION / 8;
    public static final Font FONT1 = new Font(Font.DIALOG_INPUT, Font.BOLD, GBC.PIX1);
    public static final Font FONT2 = new Font(Font.DIALOG_INPUT, Font.BOLD, GBC.PIX2);

    public static Simulator simulator;
    public static Control control;

    private static Timer timer;
    private static long timeStamp;

    private static Container pane;
    private static GridBagConstraints modelConstraints, plotConstraints, controlConstraints;
    
    public GBC(){
	super("GBC");
	
	pane = this.getContentPane();
	pane.setLayout(new GridBagLayout());
	simulator = new Simulator();
	Parameter.setSimulator(simulator);
	control = new Control(simulator);	
	
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 1;
	c.gridheight = 1;
	modelConstraints = (GridBagConstraints) c.clone();
	c.gridx++;
	plotConstraints = (GridBagConstraints) c.clone();
	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 2;
	controlConstraints = (GridBagConstraints) c.clone();
	int w = (int)(7 * SCREEN_RESOLUTION);
	setPreferredSize(new Dimension(w, w));
	pack();
	addComponents();
	pane.setBackground(Color.BLACK);
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	pack();
	setResizable(true);
	setVisible(true);
	timeStamp = 0;
	timer = new Timer(16, new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    long l = System.nanoTime();
		    long mil = (l - timeStamp) / 1000000;
		    simulator.update((double)(mil / 1000.0));
		    repaint();
		    timeStamp = l;
		}
	    });
	timer.start();
    }

    public static void toggleFullscreen(Component c){
	if(!c.equals(control)){
	    if(pane.getComponentCount() == 3){
		pane.remove(control);		
		pane.remove(c.equals(control.getPlot()) ? control.getModel() : control.getPlot());
		c.setBounds(pane.getBounds());
		c.setPreferredSize(pane.getSize());
	    } else if(pane.getComponentCount() == 1){
		pane.removeAll();
		addComponents();
	    }
	}
    }

    public static boolean getIsFullscreen(Component c){
	return pane.getComponentCount() == 1 && pane.getComponent(0).equals(c);
    }

    public static void logData(Billiard b){
	control.getPlot().logData(b);
    }
    
    public static void addComponents(){
	pane.add(control.getModel(), modelConstraints);
	pane.add(control.getPlot(), plotConstraints);
	pane.add(control, controlConstraints);
	control.setPreferredSize(new Dimension(pane.getWidth(), pane.getHeight() - pane.getWidth() / 2));
    }
}
