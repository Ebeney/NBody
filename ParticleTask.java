


public class ParticleTask implements Runnable{
	// T√¢che associ√©e √† une particule
	
	final int ID;
	
	// Constructeur
	public ParticleTask(int ID) {this.ID = ID;}
	
	
	public void run() {
		// Le bool√©en threadcontinue permet de contr√¥ler l'arret des threads par l'utilisateur
		while (Simulator.threadcontinue.get()) {
			// On r√©cup√®re position et vitesse depuis le buffer
			double x = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "x");
			double y = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "y");
			double vx = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "vx");
			double vy = Simulator.buffer.read(Simulator.buffer_frame.get(), ID, "vy");
			double ax = 0;
			double ay = 0;
			
			// On calcule la force associ√©e √† chaque autre point
			for (int i = 0; i<Simulator.N; i++) {
				if (i==ID) { continue; } 
				double x2 = Simulator.buffer.read(Simulator.buffer_frame.get(), i, "x");
				double y2 = Simulator.buffer.read(Simulator.buffer_frame.get(), i, "y");
				
				//An ajoute l'accÈlÈration due ‡ la force (c'est ici qu'il est aisÈ de rajouter une nouvelle force)
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
			
			//On signale ‡ tous les autres threads que l'on vient de terminer le calcul pour cette frame
			//On attend que tous les threads soient ‡ jour sur cette frame
			Simulator.threadsync.attempt();
		}
	}
}
