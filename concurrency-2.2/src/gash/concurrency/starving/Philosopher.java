package gash.concurrency.starving;

import java.util.Random;
import java.util.concurrent.Semaphore;
 
  /**
   * Five Philosopher problem: How to model limited shared resources
   * http://en.wikipedia.org/wiki/Five_philosophers
   * This is an update of the original version posted on WikipediA
   * JEB 11-04-2010
   */
public class Philosopher extends Thread
{
    // Shared by each instance
    private static final Random rand = new Random();
    private static int event=0;
 
    // My local stuff
    private int id;                  // Who am I
    private Semaphore myFork;        // Resource locks
    private Semaphore myNeighborsFork;
    private int meals = 10;          // Max meals
 
    /**
     * Constructor: an ID# and two shared resources
     * @param i
     * @param fork1
     * @param fork2
     */
    public Philosopher(int i, Semaphore fork1, Semaphore fork2)
    {
        id = i;
        myFork = fork1;
        myNeighborsFork = fork2;
    }
 
    /**
     * "Lazy" message queue.  Original program used a Vector<String> to
     * queue the events and displayed them at the end.  I like having
     * feedback while the program is running, but the messages are
     * sometimes displayed out of order - no biggie.
     * 
     * @param str
     */
    private void postMsg(String str) {
        System.out.printf("%d %d Philosopher %d %s\n",
                System.currentTimeMillis(), ++event, id, str);
    }
 
    /**
     * Pause - waits a bit (random fraction of a second)
     */
    private void pause()
    {
        try
        {
            sleep(rand.nextInt(1000));
        } catch (InterruptedException e){}
    }
 
    /**
     * Tell philosopher to think - he waits a bit
     *
     */
    private void think()
    {
        postMsg("is thinking");
        pause();
    }
 
    /**
     * Tell philosopher to eat.  Tries to acquire resources (forks)
     * 
     * Possible modification: Doesn't change a state
     * (hungry, starving, etc.) if they can't get a fork
     * 
     * Possible modification: could return a boolean indicating success
     */
    private void trytoeat()
    {
        postMsg("is hungry and is trying to pick up his forks");
        pause();
        try {
            // Semaphore - waits on his own fork if necessary
            myFork.acquire();
 
            // He's picked up his own fork, now try and grab his neighbor's fork
            // (does not wait)
            if (!myNeighborsFork.tryAcquire()) {
                // Unsuccessful, guess he's fasting today
                postMsg(">>>> was not able to get his neighbor's fork");
                return;
            };
 
            // Success! begins to eat
            postMsg("picked up his forks and is eating meal #" + (10 - --meals));
            pause();
 
            // Now put down the forks
            postMsg("puts down his forks");
            myNeighborsFork.release();
 
        } catch (InterruptedException e) {
            // In case the thread is interrupted
            postMsg("was interrupted while waiting for his fork");
        }
        finally { // always puts his own fork back down 
            myFork.release();
        }
    }
 
    /**
     * philosophise until all meals are consumed
     */
    @Override
    public void run()
    {
        while (meals > 0)
        {
            think();
            trytoeat();
        }
    }
 
    /**
     *  Main program
     *  * Create resouces (forks) as semaphores
     *  * create philosophers
     *  * start philosophers
     *  * wait for completion 
     */
 
    // Main program
    public static void main(String[] args)
    {
        System.out.println("Begin");
 
        final int N = 5; // five philosophers, five forks
 
        // Create the forks, 1 fork per philosopher
        Semaphore[] fork = new Semaphore[N];
        for (int f = 0; f < N; f++) {
            // each fork is a single resource
            fork[f] = new Semaphore(1, true);
        }
 
        // Create the philosophers, pass in their forks
        Philosopher[] philosopher = new Philosopher[N];
        for (int me = 0; me < N; me++) {
            // determine my right-hand neighbor's ID
            int myneighbor = me + 1;
            if (myneighbor == N) myneighbor = 0;
 
            // Initialize each philosopher (no pun intended)
            philosopher[me] = new Philosopher(me, fork[me], fork[myneighbor]); // :)
        }
 
        // Start the philosophers
        for (int i = 0; i < N; i++) {
              philosopher[i].start();
        }
 
        // Wait for them to finish
        for (int i = 0; i < N; i++) {
          try {
            philosopher[i].join();
          } catch(InterruptedException ex) { }
        }
 
        // All done
        System.out.println("Done");
    }
}