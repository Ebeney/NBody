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
	static boolean optimize_speed = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		JFrame window = new JFrame();
		window.setSize(resX+16, resY+39);
		window.setTitle("N-body simulator");
//		window.setVisible(true);
		try {
			N = Integer.parseInt(JOptionPane.showInputDialog(window, "Number of particles to simulate"));
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(window, "Invalid input, defaulting to N = "+N, "Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
		
		threads = new Thread[N];		
		buffer_frame = new AtomicLong(0);
		view_frame = new AtomicLong(0);
		threadcontinue = new AtomicBoolean(true);
		threadsync = new Lock();		
		buffer = pointsGen(buffer_size, N);
		dataTab = new float[N][number_of_forces];
		colorTab = new Color[N];
		sim_speed = new AtomicInteger(1);
		
		Random r = new Random();
		for (int i = 0; i<N; i++) {
			dataTab[i][0] = 10;
			colorTab[i] = new Color(0.5f+(r.nextFloat())/2, 0.5f+(r.nextFloat())/2, 0.5f+(r.nextFloat())/2);
		}
//		Thread surface = new Thread(new Surface(window));
		Surface surface = new Surface(window);
		for(int i = 0; i<N; i++) {
			threads[i] = new Thread(new ParticleTask(i));
			threads[i].start();
		}

//		surface.start();
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
		timer = new Timer((int) 1/fps, taskperformer);
		timer.start();
		
		while (window.isShowing()) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		threadcontinue.set(false);
		threadsync.flush();
		timer.stop();
		window.dispose();
		
	}
	
	private static Buffer pointsGen(int size, int N) {
		Buffer res = new Buffer();
		Random rn = new Random();
		for (int i = 0; i<N; i++) {
			res.write(0, i, "x", dimX*rn.nextDouble());
			res.write(0, i, "y", dimY*rn.nextDouble());
			res.write(0, i, "vx", 2*(rn.nextDouble()-.5));
			res.write(0, i, "vy", 2*(rn.nextDouble()-.5));
			
		}
//		res.write(0, 0, "x", 125);
//		res.write(0, 0, "y", 100);
//		res.write(0, 0, "vy", .5);
//
//		res.write(0, 1, "x", 100);
//		res.write(0, 1, "y", 100);
//		res.write(0, 1, "vy", -.5);
		
		
		return res;
	}

}

@SuppressWarnings("serial")
class Surface extends JPanel implements Runnable{
	static int diam;
	
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


class Buffer{
	
	Double[][][] buff;
	
	public Buffer() {
		buff = new Double[Simulator.buffer_size][Simulator.N][4];
	}
	
	public void write(long cur_frame, int pointID, String attribute, double value) {
		cur_frame = cur_frame % Simulator.buffer_size;
		int buffpos = (int) cur_frame;
		switch (attribute) {
			case "x": 	buff[buffpos][pointID][0] = value;
						break;
			case "y": 	buff[buffpos][pointID][1] = value;
						break;
			case "vx":	buff[buffpos][pointID][2] = value;
						break;
			case "vy":	buff[buffpos][pointID][3] = value;
						break;
		}
	}
	
	public double read(long cur_frame, int pointID, String attribute) {
		cur_frame = cur_frame % Simulator.buffer_size;
		int buffpos = (int) cur_frame;
		switch (attribute) {
			case "x": 	return buff[buffpos][pointID][0];
			case "y": 	return buff[buffpos][pointID][1];
			case "vx":	return buff[buffpos][pointID][2];
		}
		return buff[buffpos][pointID][3]; //case "vy"
	}
	
}

