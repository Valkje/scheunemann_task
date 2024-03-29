package actr.tasks.driving;

import java.awt.Color;
import java.awt.Graphics;

import actr.env.Frame;
import networking.ServerMain;

/**
 * The main driving environment that includes all other components of the
 * environment.
 * 
 * @author Dario Salvucci
 */
public class Env {
	static Scenario scenario = null;
	Simcar simcar;
	Road road;
	Autocar autocar;
	Speedsign speedsign;
	Construction construction;
	Controls controls;
	int keypress;
	boolean done;
	boolean signVisible = false;

	double time = 0;
	static final double sampleTime = .0050;

	static final int envWidth = Frame.screenSize.width;
	static final int envHeight = Frame.screenSize.height; // 444; // 360;
	static final int heightAdjust = 25;

	static final int simWidth = 1440; // 1440; // 640;
	static final int simHeight = 1000; // 1000; // 360;

	Env(Driver driver, Scenario s, boolean con) {
		scenario = s;

		controls = new Controls();
		controls.startUp();

		road = new Road(con);
		road.startup();

		simcar = new Simcar(driver, this);
		road.vehicleReset(simcar, 2, 100);

		autocar = new Autocar();
		road.vehicleReset(autocar, 2, 110);

		speedsign = new Speedsign(con);

		construction = new Construction(con);

		done = false;
	}

	void update() {
		speedsign.update(this);
		simcar.update(this);
		autocar.update(this);
		construction.update(this);
	}

	void draw(Graphics g) {
		g.clipRect(0, 0, envWidth, envHeight);

		g.setColor(new Color(146, 220, 255));
		g.fillRect(0, 0, envWidth, envHeight);

		Coordinate vp = world2image(Road.location(simcar.fracIndex + 1000, 2.5));
		g.setColor(new Color(0, 125, 15));
		g.fillRect(0, vp.y, envWidth, envHeight);

		road.draw(g, this);
		if (this.speedsign.signPos != null) {
			speedsign.drawSign(g, this);
			signVisible = true;
		} else {
			signVisible = false;
		}

		if (world2image(autocar.p) != null) {
			// order depends on where the cars are
			if (autocar.lane > simcar.lane) {
				autocar.draw(g, this);
				construction.drawConstruction(g, this);
			} else {
				if (autocar.lane == 2) {
					construction.drawConstruction(g, this);
					autocar.draw(g, this);
				} else {
					autocar.draw(g, this);
					construction.drawConstruction(g, this);
				}
			}
		} else
			construction.drawConstruction(g, this);

		simcar.draw(g, this);
		//ServerMain.participant.sendStartTime();
	}

	final double simViewAH = .13;
	final double simViewSD = -.37;
	final double simViewHT = 1.15;
	final double simFocalS = 450;
	final double simOXR = 1.537 / (1.537 + 2.667); // (1.2 + simViewSD) / (1.2 + 1.2);
	final double simOYR = (2.57 - simViewHT) / 2.57; // (1.67 + .2 - simViewHT) / 1.67;
	final double simNear = 1.5; // 1.5; // 2.95;
	final double simFar = 1800.00;

	public Coordinate world2image(Position world) {
		double hx = simcar.h.x;
		double hz = simcar.h.z;
		double px = simcar.p.x + ((hx * simViewAH) - (hz * simViewSD));
		double py = simViewHT;
		double pz = simcar.p.z + ((hz * simViewAH) + (hx * simViewSD));
		double wx1 = world.x - px;
		double wy1 = world.y;
		double wz1 = world.z - pz;
		double wx = hx * wz1 - hz * wx1;
		double wy = py - wy1;
		double wz = hz * wz1 + hx * wx1;
		double ox = simOXR * envWidth;
		double oy = simOYR * envHeight;
		if (wz > 0) {
			int imx = (int) Math.round(ox + ((simFocalS * wx) / wz));
			int imy = (int) Math.round(oy + ((simFocalS * wy) / wz));
			double imd = wz;
			imy -= heightAdjust;
			return new Coordinate(imx, imy, imd);
		} else
			return null;
	}

	public Coordinate world2image(Position world, double y) {
		world.y = y;
		double hx = simcar.h.x;
		double hz = simcar.h.z;
		double px = simcar.p.x + ((hx * simViewAH) - (hz * simViewSD));
		double py = simViewHT;
		double pz = simcar.p.z + ((hz * simViewAH) + (hx * simViewSD));
		double wx1 = world.x - px;
		double wy1 = world.y;
		double wz1 = world.z - pz;
		double wx = hx * wz1 - hz * wx1;
		double wy = py - wy1;
		double wz = hz * wz1 + hx * wx1;
		double ox = simOXR * envWidth;
		double oy = simOYR * envHeight;
		if (wz > 0) {
			int imx = (int) Math.round(ox + ((simFocalS * wx) / wz));
			int imy = (int) Math.round(oy + ((simFocalS * wy) / wz));
			double imd = wz;
			imy -= heightAdjust;
			return new Coordinate(imx, imy, imd);
		} else
			return null;
	}
}
