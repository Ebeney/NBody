import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Simulator {
	
	//window size
	static int resX = 1920;
	static int resY = 1920;
	
	//universe size (meters)
	static double dimX = 1000;
	static double dimY = 1000;
	
	//buffer size (frames)
	static int buffer_size = 400;
	
	
	static int number_of_forces = 1;	
	static Buffer buffer;
	static Surface surface;	
	static int N = 50;
	static int rad = 5;	
	static float timeIncrement = 0.1f;
	static float[][] dataTab;
	static Color[] colorTab;
	static AtomicLong buffer_frame;
	static AtomicLong view_frame;
	static AtomicBoolean threadcontinue;
	static Lock threadsync;	
	static Thread[] threads;
	static Timer timer;
	static int fps = 60;
	static AtomicInteger sim_speed;
	static boolean optimize_speed = false;

	public static void main(String[] args) {
		
		// On commence par faire la fenetre à partir d'informations de l'utilisateur
		JFrame window = new JFrame();
		window.setSize(resX+16, resY+39);
		window.setTitle("N-body simulator");
		try {
			N = Integer.parseInt(JOptionPane.showInputDialog(window, "Number of particles to simulate"));
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(window, "Invalid input, defaulting to N = "+N, "Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
		
		// Puis on crée les objets utiles :
		// Tableau de threads, un par point
		threads = new Thread[N];
		// Nombre de frames calculées gardées en mémoire : permet un affichage fluide
		buffer_frame = new AtomicLong(0);
		// 
		view_frame = new AtomicLong(0);
		// Booléen pour alerter les threads d'un arrêt venu de l'utilisateur
		threadcontinue = new AtomicBoolean(true);
		// Le Lock sert à faire attendre les threads les uns entre eux, qu'un état soit complètement calculé avant de passer au calcul du suivant
		threadsync = new Lock();
		// Le buffer absorbe les points et choisit le rythme auquel les envoyer à l'affichage
		buffer = pointsGen(buffer_size, N);
		// Tableau des  infos par particules (masses, charge, etc)
		dataTab = new float[N][number_of_forces];
		// Tableau des couleurs (pour l'affichage)
		colorTab = new Color[N];
		// Vitesse de la simulation : des frames sont sautées pour l'affichage
		sim_speed = new AtomicInteger(1);
		
		// Génération des couleurs
		Random r = new Random();
		for (int i = 0; i<N; i++) {
			dataTab[i][0] = 10;
			colorTab[i] = new Color(0.5f+(r.nextFloat())/2, 0.5f+(r.nextFloat())/2, 0.5f+(r.nextFloat())/2);
		}
		
		Surface surface = new Surface(window);
		
		// Création des threads, associés à une particule
		for(int i = 0; i<N; i++) {
			threads[i] = new Thread(new ParticleTask(i));
			threads[i].start();
		}
		
		// On utilise un évenement pour déclencher l'affichage à intervalles de temps précis
		ActionListener taskperformer = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (Simulator.view_frame) {
					if ((Simulator.buffer_frame.get() - Simulator.view_frame.get()) < (int) (Simulator.buffer_size/2)) return;
					view_frame.getAndAdd(sim_speed.get());
					surface.newFrame();
					Simulator.view_frame.notifyAll();
				}	
			}
		};
		
		//Le timer permet d'engendrer des événements pour déclencher l'affichage
		timer = new Timer((int) 1/fps, taskperformer);
		timer.start();
		
		//On vérifie toutes les secondes que la fenêtre n'a pas été fermée par l'utilisateur
		while (window.isShowing()) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//Ici, la fenêtre a été fermée
		threadcontinue.set(false); //Le flag threadcontinue permet d'alerter tous les threads qu'il faut arrêter les calculs
		threadsync.flush(); //On libère tous les threads du verrou threadsync afin qu'ils puissent terminer
		timer.stop(); //On arrête la génération de l'affichage
		window.dispose(); //On nettoie la fenêtre
		
	}
	
	private static Buffer pointsGen(int size, int N) {
		// Fonction qui permet de générer un buffer avec les points à des positions aléatoires
		Buffer res = new Buffer();
		Random rn = new Random();
		for (int i = 0; i<N; i++) {
			res.write(0, i, "x", dimX*rn.nextDouble());
			res.write(0, i, "y", dimY*rn.nextDouble());
			res.write(0, i, "vx", 2*(rn.nextDouble()-.5));
			res.write(0, i, "vy", 2*(rn.nextDouble()-.5));
			
		}		
		return res;
	}

}
