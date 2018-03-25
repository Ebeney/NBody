public class UnsafeCounterDemo {
	
	static counter c = new counter();
	
	public static void main(String[] args) throws InterruptedException {
		Thread[] threads = new Thread[2];
		for (int i = 0; i<2; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i<10000000; i++) {
						c.increment();
					}
				}
			});
			threads[i].start();
		}
		
		for (int i = 0; i<2; i++) {
			threads[i].join();
		}
		System.out.println(c.get());
//		System.out.println("Hello world");
	}
}

