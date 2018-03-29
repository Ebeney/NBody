


public class ParticleTask implements Runnable{
	// Tâche associée à une particule
	
	final int ID;
	
	// Constructeur
	public ParticleTask(int ID) {this.ID = ID;}
	
	
	public void run() {
		// Le booléen threadcontinue permet de contrôler l'arret des threads par l'utilisateur
		while (Simulator.threadcontinue.get()) {
			// On récupère position et vitesse depuis le buffer
			double x = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "x");
			double y = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "y");
			double vx = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "vx");
			double vy = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "vy");
			double ax = 0;
			double ay = 0;
			
			// On calcule la force associée à chaque autre point
			for (int i = 0; i<Simulator.N; i++) {
				if (i==ID) { continue; } 
				double x2 = Simulator.buffer.read(Simulator.buffer_frame.get(), i, "x");
				double y2 = Simulator.buffer.read(Simulator.buffer_frame.get(), i, "y");
				
				//Adding acceleration due to force (add new force here)
				double massinvcubedist = Simulator.dataTab[i][0]*Math.pow(Math.pow(x2-x, 2) + Math.pow(y2-y, 2), -1.5);
				ax = ax + massinvcubedist*(x2-x);
				ay = ay + massinvcubedist*(y2-y);
				
			}
			
			// On ajoute le point au buffer
			long nextframe = Simulator.buffer_frame.get() + 1;
			Simulator.buffer.write(nextframe, ID, "x", x+Simulator.timeIncrement*vx);
			Simulator.buffer.write(nextframe, ID, "y", y+Simulator.timeIncrement*vy);
			Simulator.buffer.write(nextframe, ID, "vx", vx+Simulator.timeIncrement*ax);
			Simulator.buffer.write(nextframe, ID, "vy", vy+Simulator.timeIncrement*ay);
			Simulator.threadsync.attempt();
		}
	}
}
