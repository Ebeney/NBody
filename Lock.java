

public class Lock {
	// Le Lock permet de forcer que toutes les positions soient calculées
	// à un instant t, avant que ne soient calculées les positions en t+dt
	
	private volatile int counter;
	
	// Constructeur
	public Lock() {counter = 0;}
	
	// Chaque thread indique quand son calcul est fini. Lorsque N calculs sont finis, on prévient
	// tous les threads de recommencer à calculer
	public synchronized void attempt() {
		counter = counter + 1;
		if (counter == Simulator.N) {
			counter = 0;
			synchronized (Simulator.view_frame) {
				// Gestion de la vitesse d'affichage
				
				//On v�rifie que le buffer ne rentre pas en collision avec la frame de l'afficheur
				if (Simulator.buffer_frame.incrementAndGet() - Simulator.view_frame.get() > Simulator.buffer_size - 2){
					if (Simulator.optimize_speed) Simulator.sim_speed.incrementAndGet();
					try {
						Simulator.view_frame.wait(); 
						//Le thread est sorti de cette attente par l'affichage d'une nouvelle frame
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Simulator.view_frame.notifyAll();
			}
			notifyAll();
				
		} else {
			try {
				wait(); //On attend les autres threads
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	// Nettoyage des threads si arret utilisateur
	public synchronized void flush() {
		notifyAll();
	}
}
