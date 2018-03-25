public class Lock {
	private volatile int counter;
	public Lock() {
		counter = 0;
	}
	public synchronized void attempt() {
		counter = counter + 1;
		if (counter == Simulator.N) {
			counter = 0;
			synchronized (Simulator.view_frame) {
				if (Simulator.buffer_frame.incrementAndGet() - Simulator.view_frame.get() > Simulator.buffer_size - 2){
					if (Simulator.optimize_speed) Simulator.sim_speed.incrementAndGet();
					try {
						Simulator.view_frame.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Simulator.view_frame.notifyAll();
			}
			notifyAll();
				
		} else {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void flush() {
		notifyAll();
	}
}
