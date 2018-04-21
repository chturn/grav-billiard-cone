package main.java;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import main.java.tcu.physics.gbc.*;

public class GBCStart {
	public static void main(String[] args){
	    System.out.println("Running Gravitational Billiard Simulation...");
	    try {
     		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    SwingUtilities.invokeLater(new Runnable(){
		    public void run(){
			new GBC();
    		    }
		});
	}
}
