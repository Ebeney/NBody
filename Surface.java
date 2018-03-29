import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class Surface extends JPanel implements Runnable{
	// Classe gérant l'affichage
	
	static int diam;
	
	// Constructeur
	public Surface(JFrame window) {
		window.add(this);
		window.setVisible(true);
		diam = Simulator.rad << 1;
		setBackground(Color.BLACK);
		
	}
	
	
	void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.drawString("Current frame : " + Simulator.view_frame.get(), 10, 30);
		g2d.drawString("Buffer health : " + (Simulator.buffer_frame.get()-Simulator.view_frame.get()), 10, 60);
		g2d.drawString("Simulation speed : " + Simulator.sim_speed.get(), 10, 90);
		for (int i = 0; i < Simulator.N; i++) {
			int x = (int) (Simulator.resX*Simulator.buffer.read(Simulator.view_frame.get(), i, "x")/Simulator.dimX);
			int y = (int) (Simulator.resY*Simulator.buffer.read(Simulator.view_frame.get(), i, "y")/Simulator.dimY);
			g2d.setColor(Simulator.colorTab[i]);
			g2d.fillOval(x-Simulator.rad, y-Simulator.rad, diam, diam);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}
	
	public void newFrame() {
		this.repaint();
		this.revalidate();
	}
	
	public void run() {
		while (Simulator.threadcontinue.get()) {
			try {
				TimeUnit.MILLISECONDS.sleep(15);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			synchronized (Simulator.view_frame) {
				if ((Simulator.buffer_frame.get() - Simulator.view_frame.incrementAndGet()) < (int) (Simulator.buffer_size/2)) {
					try {
						Simulator.view_frame.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				newFrame();
				Simulator.view_frame.notifyAll();
			}
		}
	}
}




