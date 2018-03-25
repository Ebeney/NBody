public class SurfaceLock {
	volatile boolean locked;
	
	public SurfaceLock() {
		locked = false;
	}
	
	public synchronized void attempt() {
		if (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void setlock(boolean lock) {
		locked = lock;
	}
}