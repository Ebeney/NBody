public class ParticleTask implements Runnable{
	final int ID;
	
	public ParticleTask(int ID) {
		this.ID = ID;
	}
	
	public void run() {
		while (Simulator.threadcontinue.get()) {
			double x = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "x");
			double y = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "y");
			double vx = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "vx");
			double vy = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "vy");
			double ax = 0;
			double ay = 0;
			for (int i = 0; i<Simulator.N; i++) {
				if (i!=ID) {
					double x2 = Simulator.buffer.read(Simulator.buffer_frame.get(), i, "x");
					double y2 = Simulator.buffer.read(Simulator.buffer_frame.get(), i, "y");
					
					//Adding acceleration due to force (add new force here)
					double massinvcubedist = Simulator.dataTab[i][0]*Math.pow(Math.pow(x2-x, 2) + Math.pow(y2-y, 2), -1.5);
					ax = ax + massinvcubedist*(x2-x);
					ay = ay + massinvcubedist*(y2-y);
				}
			}
			Simulator.buffer.write(Simulator.buffer_frame.get()+1, ID, "x", x+Simulator.timeIncrement*vx);
			Simulator.buffer.write(Simulator.buffer_frame.get()+1, ID, "y", y+Simulator.timeIncrement*vy);
			Simulator.buffer.write(Simulator.buffer_frame.get()+1, ID, "vx", vx+Simulator.timeIncrement*ax);
			Simulator.buffer.write(Simulator.buffer_frame.get()+1, ID, "vy", vy+Simulator.timeIncrement*ay);
			Simulator.threadsync.attempt();
		}
	}
}
