package main.java.tcu.physics.gbc;

public enum Parameter{
    TIME("t"),
    HEIGHT("z"),
    RADIUS("r"),
    PHI("\u03D5"),
    AVEL_R("\u03C9\u00B7r"),
    VEL_R("v\u00B7r"),
    VEL_T("v\u00B7n"),
    MOM_L("\u2113\u00B7z"),
    SPIN_Z("S\u00B7z"),
    AMOM_Z("J\u00B7z"),
    ENERGY("E");

    private static Simulator sim;
    String name;
    
    Parameter(String s){
	name = s;
    }

    public double getValue(Billiard b){
	switch(this){
	case TIME:
	    return b.getTN();
	case HEIGHT:
	    return b.getPosXYZ()[2];
	case RADIUS:
	    return b.getPosRTP()[0];
	case PHI:
	    return b.getPosRTP()[2];
	case AVEL_R:
	    return b.getAvelRSN()[0];
	case VEL_R:
	    return b.getVelRSN()[0];
	case VEL_T:
	    return -b.getVelRSN()[2];
	case MOM_L:
	    return b.getOmomZ();
	case SPIN_Z:
	    return b.getSmomZ();
	case AMOM_Z:
	    return b.getAmomZ();
	case ENERGY:
	    return b.getEnergy();
	}
	return -1;
    }

    public double getMin(){
	switch(this){
	case ENERGY:
	case TIME:
	case HEIGHT:
	case RADIUS:
	    return 0.0;
	case PHI:
	    return -Math.PI;
	case AVEL_R:
	    return -getMaxW();
	case VEL_R:
	case VEL_T:
	    return -getMaxV();
	case MOM_L:
	    return -getMaxL();
	case SPIN_Z:
	    return -getMaxS();
	case AMOM_Z:
	    return -getMaxJ();

	}
	return -1;
    }

    public double getMax(){
	switch(this){
	case TIME:
	    return sim.time;
	case HEIGHT:
	    return 20;
	case RADIUS:
	    return getMaxR();
	case PHI:
	    return Math.PI;
	case AVEL_R:
	    return getMaxW();
	case VEL_R:
	case VEL_T:
	    return getMaxV();
	case MOM_L:
	    return getMaxL();
	case SPIN_Z:
	    return getMaxS();
	case AMOM_Z:
	    return getMaxJ();
	case ENERGY:
	    return 100;
	}
	return -1;
    }

    private double getMaxR(){
	return sim.energy / (sim.mass * sim.gravity * sim.costheta);
    }

    private double getMaxV(){
	return Math.sqrt(2 * sim.energy / sim.mass);
    }

    private double getMaxL(){
	return Math.sqrt(2 * sim.energy * sim.mass);
    }

    private double getMaxW(){
	return Math.sqrt(2 * sim.energy / sim.momi);
    }

    private double getMaxS(){
	return Math.sqrt(2 * sim.energy * sim.momi);
    }

    private double getMaxJ(){
	return sim.mass > sim.momi ? getMaxL() : getMaxS();
    }

    @Override
    public String toString(){
	return name;
    }

    public static void setSimulator(Simulator s){
	sim = s;
    }

    public static Parameter[] getAll(){
	return new Parameter[]{TIME, HEIGHT, RADIUS, PHI, AVEL_R, VEL_R, VEL_T, MOM_L, SPIN_Z, AMOM_Z, ENERGY};
    }
}
