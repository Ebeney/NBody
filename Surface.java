import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class Surface extends JPanel{
	// Classe gÃ©rant l'affichage
	
	static int diam;
	
	// Constructeur
	public Surface(JFrame window) {
		window.add(this);
		window.setVisible(true); //Affiche la fenêtre
		diam = Simulator.rad << 1; //Multiplication par deux de radiant en utilisant le décalage de bits
		setBackground(Color.BLACK);
		
	}
	
	
	void doDrawing(Graphics g) { //Fonction de dessin
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.drawString("Current frame : " + Simulator.view_frame.get(), 10, 30);
		g2d.drawString("Buffer health : " + (Simulator.buffer_frame.get()-Simulator.view_frame.get()), 10, 60);
		g2d.drawString("Simulation speed : " + Simulator.sim_speed.get(), 10, 90);
		
		//Parcours de tous les points
		for (int i = 0; i < Simulator.N; i++) {
			//On récupère les coordonnées dans le buffer pour chaque points
			int x = (int) (Simulator.resX*Simulator.buffer.read(Simulator.view_frame.get(), i, "x")/Simulator.dimX);
			int y = (int) (Simulator.resY*Simulator.buffer.read(Simulator.view_frame.get(), i, "y")/Simulator.dimY);
			
			//On affiche le point
			g2d.setColor(Simulator.colorTab[i]);
			g2d.fillOval(x-Simulator.rad, y-Simulator.rad, diam, diam);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) { 
		//Fonction nécéssaire afin de contrôler ce qui se passe lorsque 
		//l'affichage est rafraichi
		super.paintComponent(g);
		doDrawing(g);
	}
	
	public void newFrame() {
		//Rafraîchissement de l'affichage
		this.repaint();
		this.revalidate();
	}
}




