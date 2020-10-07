import java.util.concurrent.*;
import java.util.concurrent.locks.*;

abstract class AbstractLock implements Lock {
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
