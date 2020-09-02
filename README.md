MCS Queue Lock maintains a linked-list for
threads waiting to enter critical section (CS).

Each thread that wants to enter CS joins at the
end of the queue, and waits for the thread
infront of it to finish its CS.
So, it locks itself and asks the thread infront
of it, to unlock it when he's done. Atomics
instructions are used when updating the shared
queue. Corner cases are also takes care of.

As each thread waits (spins) on its own "locked"
field, this type of lock is suitable for
cache-less NUMA architectures. The MCSLock is
due to [John Mellor-Crummey] and [Michael Scott].

[John Mellor-Crummey]: https://scholar.google.com/citations?user=wX0XpxMAAAAJ&hl=en
[Michael Scott]: https://scholar.google.com/citations?user=PzaBy-UAAAAJ&hl=en

```java
1. When thread wants to access critical
   section, it stands at the end of the
   queue (FIFO).
2a. If there is no one in queue, it goes head
    with its critical section.
2b. Otherwise, it locks itself and asks the
    thread infront of it to unlock it when its
    done with CS.
```

```java
1. When a thread is done with its critical
   section, it needs to unlock any thread
   standing behind it.
2a. If there is a thread standing behind,
    then it unlocks him.
2b. Otherwise it tries to mark queue as empty.
     If no one is joining, it leaves.
2c. If there is a thread trying to join the
    queue, it waits until he is done, and then
    unlocks him, and leaves.
```

See [MCSLock.java] for code, [Main.java] for test, and [repl.it] for output.

[MCSLock.java]: https://repl.it/@wolfram77/mcs-lock#MCSLock.java
[Main.java]: https://repl.it/@wolfram77/mcs-lock#Main.java
[repl.it]: https://mcs-lock.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
- [Algorithms for scalable synchronization on shared-memory multiprocessors :: John M. Mellor-Crummey, Michael L. Scott](https://dl.acm.org/doi/10.1145/103727.103729)
