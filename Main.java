class Main {
  static MCSLock lock;
  static double[] sharedData;

  // Critical section updated shared data
  // with a random value. If all values
  // dont match then it was not executed
  // atomically!
  static void criticalSection() {
    double v = 2*Math.random();
    for(int i=0; i<sharedData.length; i++)
      sharedData[i] += v*sharedData[i];
  }

  // Checks to see if all values match. If not,
  // then critical section was not executed
  // atomically.
  static boolean criticalSectionWasAtomic() {
    double v = sharedData[0];
    for(int i=0; i<sharedData.length; i++)
      if(sharedData[i]!=v) return false;
    return true;
  }

  // Unsafe thread executes CS 100 times, without
  // holding a lock. This can cause CS to be
  // executed non-atomically which can be detected.
  static Thread unsafeThread(int n) {
    Thread t = new Thread(() -> {
      for(int i=0; i<100; i++) {
        criticalSection();
      }
    });
    t.start();
    return t;
  }

  // Safe thread executes CS 100 times, while
  // holding a lock. This allows CS to always be
  // executed atomically which can be verified.
  static Thread safeThread(int n) {
    Thread t = new Thread(() -> {
      for(int i=0; i<100; i++) {
        lock.lock();
        criticalSection();
        lock.unlock();
      }
      log(n+": done");
    });
    t.start();
    return t;
  }

  public static void main(String[] args) {
    try {
    lock = new MCSLock();
    sharedData = new double[1000];
    log("Starting 100 unsafe threads ...");
    Thread[] threads = new Thread[100];
    for(int i=0; i<threads.length; i++)
      threads[i] = unsafeThread(i);
    for(int i=0; i<threads.length; i++)
      threads[i].join();
    boolean atomic = criticalSectionWasAtomic();
    log("Critical Section was atomic? "+atomic);
    log("");
    log("Starting 100 safe threads ...");
    for(int i=0; i<threads.length; i++)
      threads[i] = safeThread(i);
    for(int i=0; i<threads.length; i++)
      threads[i].join();
    atomic = criticalSectionWasAtomic();
    log("Critical Section was atomic? "+atomic);
    }
    catch(InterruptedException e) {}
  }

  static void log(String x) {
    System.out.println(x);
  }
}