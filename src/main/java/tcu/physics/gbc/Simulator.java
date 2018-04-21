package main.java.tcu.physics.gbc;

import java.util.ArrayList;

public class Simulator {

    private ArrayList<Billiard> billiards;

    private boolean isStopped, doTrajectory, isSpeedy;
    public double time, theta, mass, gravity, radius, momi, omega, amp, freq, phi, muS, muK, e, energy;
    public double sintheta, costheta;
    public boolean isSticky;
    public Double initPosR, initVelT, initVelP, initVel;

    private int numInvalid;
    
    public Simulator(){
	billiards = new ArrayList<Billiard>();
	isStopped = true;
	doTrajectory = true;
	Billiard.setSim(this);
	numInvalid = 0;
    }

    public void update(double dt){
	if(!isStopped){
	    if(isSpeedy){
		time += 32 * dt;
	    } else {
		time += dt;
	    }
	    
	    omega = amp * Math.cos(freq * time);
	    if(freq == 0.0){
		phi = amp * time;
	    } else phi = amp * freq * Math.sin(freq * time);
	    
	    int count = 0;
	    for(Billiard b : billiards){
		if(!b.isInvalid()){
		    b.update(time);
		} else count++;
	    }
	    if(numInvalid != count){
		numInvalid = count;
		System.out.println("Count: " + numInvalid);
	    }
	}
    }
    
    public void initialize(Control c){
	billiards.clear();
	theta = Math.toRadians(c.getConeAngle());
	sintheta = Math.sin(theta);
	costheta = Math.cos(theta);
	mass = c.getMass();
	gravity = c.getGravity();
	radius = c.getRadius();
	initPosR = c.getBallPosR();
	initVelT = Math.toRadians(c.getBallVelT());
	initVelP = Math.toRadians(c.getBallVelP());
	initVel = Math.sqrt(2 * c.getKE() / mass);
	if(initPosR.isNaN()){
	    double p, v;
	    for(double r = 0.05; r < 1.0; r += 0.05){
		v = Math.sqrt(2 * (c.energy / mass - gravity * r * costheta));
		if(c.moml == 0.0) p = 0.0;
		else p = Math.asin(c.moml / (r * sintheta * v * Math.cos(initVelP)));
		if(p >= 0 && v > 0) billiards.add(new Billiard(r, p, initVelP, v));
	    }
	} else billiards.add(new Billiard(initPosR, initVelT, initVelP, initVel));
	energy = getEnergy();
	momi = 2 * mass * radius * radius / 5;
	phi = 0.0;
	amp = c.driving ? c.getAmp() : 0.0;
	freq = 0.0;
	time = 0.0;
	muS = c.getMuS();
	isSticky = c.sticky;
	if(muS < 0.0) muS = 0.0;
	muK = c.getMuK();
	if(muK < 0.0) muK = 0.0;
	e = c.getE();
	stop();
    }

    public void start(){
	isStopped = false;
	for(Billiard b : billiards){
	    GBC.logData(b);
	}
    }
    
    public void stop(){
	isStopped = true;
    }

    public boolean isStopped(){
	return isStopped;
    }

    public boolean isSpeedy(){
	return isSpeedy;
    }

    public void setSpeedy(boolean b){
	isSpeedy = b;
    }

    public boolean doTrajectory(){
	return doTrajectory;
    }

    public double getEnergy(){
	double sum = 0.0;
	int num = billiards.size();
	for(Billiard b : billiards){
	    if(!b.isInvalid()) sum += b.getEnergy();
	    else num--;
	}
	if(num == 0) stop();
	return sum / num;
    }

    public double getSmomZ(){
	double sum = 0.0;
	int num = billiards.size();
	for(Billiard b : billiards){
	    if(!b.isInvalid()) sum += b.getSmomZ();
	    else num--;
	}
	return sum / num;
    }

    public double getOmomZ(){
	double sum = 0.0;
	int num = billiards.size();
	for(Billiard b : billiards){
	    if(!b.isInvalid()) sum += b.getOmomZ();
	    else num--;
	}
	return sum / num;
    }

    public double getAmomZ(){
	double sum = 0.0;
	int num = billiards.size();
	for(Billiard b : billiards){
	    if(!b.isInvalid()) sum += b.getAmomZ();
	    else num--;
	}
	return sum / num;
    }

    public ArrayList<Billiard> getBilliards(){
	return billiards;
    }

    public Billiard getBilliard(int index){
	return billiards.get(index);
    }
}
