package main.java.tcu.physics.gbc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Billiard {

    private double tn, tnp1;
    private double[] posXYZ, posRTP;
    private double[] velXYZ, avelXYZ;
    private double[] velRSN, avelRSN, rvelRSN, impRSN;
    private double u, psi;
    private double sinphi, cosphi;

    private Queue<Double> roots;

    private static Simulator sim;

    private boolean invalid;

    public Billiard(double rad, double ph, double ps, double vl){

	posXYZ = new double[3];
	velXYZ = new double[3];
	avelXYZ = new double[3];
	posRTP = new double[3];
	velRSN = new double[3];
	avelRSN = new double[3];
	rvelRSN = new double[3];
	impRSN = new double[3];

	roots = new LinkedList<Double>();
	
	tn = 0.0;
	tnp1 = -1.0;

	setPosRTP(rad, sim.theta, 0);
	setPosXYZfromRTP();
	setVelRSNfromInput(vl, ph, ps);
	setVelXYZfromRSN();

	u = Math.hypot(velXYZ[0], velXYZ[1]);
	psi = posRTP[2] - Math.atan2(-velXYZ[1], -velXYZ[0]);
    }

    public void update(double t){
	while(t > getTNP1()){
	    doCollision();
	    GBC.logData(this);
	}
	if(sim.doTrajectory() && !sim.isSpeedy()) doTrajectory(t - getTN());
    }
    
    public void doCollision(){
	double dt = tnp1 - tn;

	doTrajectory(dt);
	velXYZ[2] -= sim.gravity * dt;
	setPosRTPfromXYZ();
	
	//System.out.println(velRSN[0] + ", " + velRSN[1] + ", " + velRSN[2]);	    
	//	System.out.println(avelRSN[0] + ", " + avelRSN[1] + ", " + avelRSN[2]);
	
	setVelRSNfromXYZ();
	setAvelRSNfromXYZ();
	
	doImpact();
	
	setVelXYZfromRSN();
	setAvelXYZfromRSN();

	u = Math.hypot(velXYZ[0], velXYZ[1]);
	psi = posRTP[2] - Math.atan2(-velXYZ[1], -velXYZ[0]);
	
	tn = tnp1;
	tnp1 = -1.0;	
    }

    public void doImpact(){

	double e = (sim.e > 1.0) ? Math.exp(-Math.pow(Math.abs(rvelRSN[2]), 3.0 / 5) / sim.e) : sim.e;

	if(sim.isSticky){
	    avelRSN[0] = (2*avelRSN[0] + 5*(sim.omega*(posXYZ[2]*Math.tan(sim.theta)+sim.radius*sim.costheta)-velRSN[1])/sim.radius)/7.0;
	    avelRSN[1] = (2*avelRSN[1] + 5*velRSN[0]/sim.radius)/7.0;
	    velRSN[0] = (5*velRSN[0] + 2*sim.radius*avelRSN[1])/7.0;
	    velRSN[1] = (5*velRSN[1] - 2*sim.radius*avelRSN[0] + 2*sim.omega*(posXYZ[2]*Math.tan(sim.theta) + sim.radius*sim.costheta))/7.0;
	    velRSN[2] *= -e;
	} else {
	    rvelRSN[0] = velRSN[0] - sim.radius * avelRSN[1];
	    rvelRSN[1] = velRSN[1] + sim.radius * (avelRSN[0] - sim.omega * sim.costheta) - posRTP[0] * sim.omega * sim.sintheta;
	    rvelRSN[2] = velRSN[2];	

	    double rvelTan = Math.hypot(rvelRSN[0], rvelRSN[1]);
	
	    impRSN[2] = -sim.mass * (1 + e) * rvelRSN[2];

	    //double OGimpStop = -sim.mass * rvelTan / (1 / sim.mass + sim.radius * sim.radius / sim.momi);
	    double impStop = -2.0 * sim.mass * rvelTan / 7.0;
	    double impStick = -sim.muS * impRSN[2];
	    double impSlide = -sim.muK * impRSN[2];

	    if(Math.abs(impStop) < Math.abs(impStick)){
		impRSN[0] = impStop * rvelRSN[0] / rvelTan;
		impRSN[1] = impStop * rvelRSN[1] / rvelTan;

	    } else {
		impRSN[0] = impSlide * rvelRSN[0] / rvelTan;
		impRSN[1] = rvelRSN[1] * impRSN[0] / rvelRSN[0];

	    }
	    
	    avelRSN[0] += sim.radius * impRSN[1] / sim.momi;
	    avelRSN[1] -= sim.radius * impRSN[0] / sim.momi;
	    velRSN[0] += impRSN[0] / sim.mass;
	    velRSN[2] += impRSN[2] / sim.mass;
	    velRSN[1] += impRSN[1] / sim.mass;
	}
	
	invalid = Math.abs(velRSN[2]) < 0.01;
    }

    public void doTrajectory(double dt){
	setPosXYZfromRTP();
	posXYZ[0] += velXYZ[0] * dt;
	posXYZ[1] += velXYZ[1] * dt;
	posXYZ[2] += velXYZ[2] * dt - 0.5 * sim.gravity * dt * dt;
    }

    public boolean isInvalid(){
	return invalid;
    }
    
    public double getTN(){
	return tn;
    }

    public double getTNP1(){
	if(tnp1 < 0.0) computeTNP1();
	return tnp1;
    }
	
    private double computeTNP1(){
	double gamma = u * sim.costheta / (sim.sintheta * velXYZ[2]);
	double nu = sim.gravity * posXYZ[2] / (velXYZ[2] * velXYZ[2]);
	double lambda = solveCubic(0.25, -1.0, (1.0 - (gamma * gamma) - nu), 2.0 * nu * (1.0 +/*<--changed to plus!*/ (gamma * Math.cos(psi))));
	double dt = (velXYZ[2] / sim.gravity) * lambda;
	roots.add(dt);
	if(roots.size() > 3) roots.poll();
	tnp1 = tn + dt;
	return dt;
    }

    public double getU(){
	return u;
    }

    public double getPsi(){
	return psi;
    }

    public double getEnergy(){
	return 0.5 * sim.mass * square(velRSN) + 0.5 * sim.momi * square(avelRSN) + sim.mass * sim.gravity * posRTP[0] * sim.costheta;
    }

    public double getSmomZ(){
	return sim.momi * avelXYZ[2];
    }

    public double getOmomZ(){
	return sim.mass * posRTP[0] * sim.sintheta * velRSN[1];
    }

    public double getAmomZ(){
	return getSmomZ() + getOmomZ();
    }

    public double[] getPosRTP(){
	return posRTP;
    }
    
    private void setPosRTP(double r, double t, double p){
	posRTP[0] = r;
	posRTP[1] = t;
	posRTP[2] = p;
	sinphi = Math.sin(posRTP[2]);
	cosphi = Math.cos(posRTP[2]);
    }


    private void setPosRTPfromXYZ(){
	posRTP[0] = Math.sqrt(posXYZ[0] * posXYZ[0] + posXYZ[1] * posXYZ[1] + posXYZ[2] * posXYZ[2]);
	posRTP[1] = Math.atan(Math.sqrt(posXYZ[0] * posXYZ[0] + posXYZ[1] * posXYZ[1]) / posXYZ[2]);
	posRTP[2] = Math.atan2(posXYZ[1], posXYZ[0]);
	sinphi = Math.sin(posRTP[2]);
	cosphi = Math.cos(posRTP[2]);
    }

    public double[] getPosXYZ(){
	return posXYZ;
    }

    private void setPosXYZfromRTP(){
	posXYZ[0] = posRTP[0] * Math.sin(posRTP[1]) * Math.cos(posRTP[2]);
	posXYZ[1] = posRTP[0] * Math.sin(posRTP[1]) * Math.sin(posRTP[2]);
	posXYZ[2] = posRTP[0] * Math.cos(posRTP[1]);
    }

    public double[] getVelRSN(){
	return velRSN;
    }
    
    private void setVelRSNfromXYZ(){
	double xysincos = (velXYZ[0] * cosphi + velXYZ[1] * sinphi);
	velRSN[0] = sim.sintheta * xysincos + velXYZ[2] * sim.costheta;
	velRSN[1] = -velXYZ[0] * sinphi + velXYZ[1] * cosphi;
	velRSN[2] = -sim.costheta * xysincos + velXYZ[2] * sim.sintheta;	
    }

    public double[] getVelXYZ(){
	return velXYZ;
    }

    private void setVelXYZfromRSN(){
	velXYZ[0] = velRSN[0] * sim.sintheta * cosphi - velRSN[2] * sim.costheta * cosphi - velRSN[1] * sinphi;
	velXYZ[1] = velRSN[0] * sim.sintheta * sinphi - velRSN[2] * sim.costheta * sinphi + velRSN[1] * cosphi;
	velXYZ[2] = velRSN[0] * sim.costheta + velRSN[2] * sim.sintheta;
    }
    
    private void setVelRSNfromInput(double v, double p, double s){
	velRSN[0] = v * Math.cos(p);
	velRSN[1] = v * Math.sin(p) * Math.sin(s);
	velRSN[2] = v * Math.sin(p) * Math.cos(s);	
    }

    public double[] getAvelXYZ(){
	return avelXYZ;
    }

    public void setAvelXYZfromRSN(){
	double wsincos = avelRSN[0] * sim.sintheta - avelRSN[2] * sim.costheta;
	avelXYZ[0] = cosphi * wsincos - avelRSN[1] * sinphi;
	avelXYZ[1] = sinphi * wsincos + avelRSN[1] * cosphi;
	avelXYZ[2] = avelRSN[0] * sim.costheta + avelRSN[2] * sim.sintheta;
    }

    public double[] getAvelRSN(){
	return avelRSN;
    }

    public void setAvelRSNfromXYZ(){
	double wcossin = avelXYZ[0] * cosphi + avelXYZ[1] * sinphi;
	avelRSN[0] = sim.sintheta * wcossin + avelXYZ[2] * sim.costheta;
	avelRSN[1] = -avelXYZ[0] * sinphi + avelXYZ[1] * cosphi;
	avelRSN[2] = -sim.costheta * wcossin + avelXYZ[2] * sim.sintheta;	
    }

    public Double[] getRoots(){
	if(roots.size() < 3) return null;
	return Arrays.copyOfRange(roots.toArray(), 0, 3, Double[].class);
    }

    public static double solveCubic(double a, double b, double c, double d){
	double xn = -b / (3 * a);
	double yn = (2.0 * b * b * b) / (27.0 * a * a) - b * c / (3.0 * a) + d;
	double delta2 = (b * b - 3.0 * a * c) / (9.0 * a * a);	    
	double delta = Math.sqrt(delta2);
	double h = 2 * a * delta * delta * delta;
	double phi = Math.asin(yn / h) / 3.0;
	return 2.0 * delta * Math.sin(phi) + xn;
    }

    private static double norm(double[] d){
	return Math.sqrt(square(d));
    }

    private static double square(double[] d)  {
	return d[0] * d[0] + d[1] * d[1] + d[2] * d[2];
    }

    public static void setSim(Simulator s){
	sim = s;
    }
}	
