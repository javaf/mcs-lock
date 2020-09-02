import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

// MCS Queue Lock maintains a linked-list for
// threads waiting to enter critical section (CS).
// 
// Each thread that wants to enter CS joins at the
// end of the queue, and waits for the thread
// infront of it to finish its CS.
// 
// So, it locks itself and asks the thread infront
// of it, to unlock it when he's done. Atomics
// instructions are used when updating the shared
// queue. Corner cases are also takes care of.
// 
// As each thread waits (spins) on its own "locked"
// field, this type of lock is suitable for
// cache-less NUMA architectures.

class MCSLock implements Lock {
  AtomicReference<QNode> queue;
  ThreadLocal<QNode> node;
  // queue: points to the tail
  // node:  is unique for each thread

  public MCSLock() {
    queue = new AtomicReference<>(null);
    node = new ThreadLocal<>() {
      protected QNode initialValue() {
        return new QNode();
      }
    };
  }

  // 1. When thread wants to access critical
  //    section, it stands at the end of the
  //    queue (FIFO).
  // 2a. If there is no one in queue, it goes head
  //     with its critical section.
  // 2b. Otherwise, it locks itself and asks the
  //     thread infront of it to unlock it when its
  //     done with CS.
  @Override
  public void lock() {
    QNode n = node.get();         // 1
    QNode m = queue.getAndSet(n); // 1
    if (m != null) {   // 2b
      n.locked = true; // 2b
      m.next = n;      // 2b
      while(n.locked) Thread.yield(); // 2b
    } // 2a
  }

  // 1. When a thread is done with its critical
  //    section, it needs to unlock any thread
  //    standing behind it.
  // 2a. If there is a thread standing behind,
  //     then it unlocks him.
  // 2b. Otherwise it tries to mark queue as empty.
  //      If no one is joining, it leaves.
  // 2c. If there is a thread trying to join the
  //     queue, it waits until he is done, and then
  //     unlocks him, and leaves.
  @Override
  public void unlock() {
    QNode n = node.get(); // 1
    if (n.next == null) {               // 2b
      if (queue.compareAndSet(n, null)) // 2b
        return;                         // 2b
      while(n.next == null) Thread.yield(); // 2c
    }                      // 2a
    n.next.locked = false; // 2a
    n.next = null;         // 2a
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    lock();
  }

  @Override
  public boolean tryLock() {
    lock();
    return true;
  }

  @Override
  public boolean tryLock(long arg0, TimeUnit arg1) throws InterruptedException {
    lock();
    return true;
  }

  @Override
  public Condition newCondition() {
    return null;
  }
}
