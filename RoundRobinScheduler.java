
//library responsible for 
import java.util.*;


//class responsible for tier 1 RR
public class RoundRobinScheduler {

    //static (final) no one can change for QT
    private final int timeQuantum;

    //contexet switch time 
    private final int contextSwitchTime;

    //ready queue for tier 1 to make FIFO for processes
    //<take project of type process>
    private Queue<process> readyQueue;


    
    //all processes entring the scheduler
    private List<process> processList;

    //member3 connection <---------
    //send to tier 2 if quatum eneded and you still have time 
    // member 3 must receive processes came from Tier 1.
    private TierTwoHandler tierTwo;

    // Stime in schedular
    private int currentTime = 0;

    // process which we will sart with index 0 where first p exsist
    private int nextProcessIndex = 0;


    // Constructor
    // member 3: take un finished process in tier 1 <--------
    // member 4: CS and global time handling must be same <---------

    public RoundRobinScheduler(int quantum,int cs,List<process> processes,TierTwoHandler tierTwo) {

        timeQuantum = quantum;
        contextSwitchTime = cs;

        //we willuse linked list in order to add ...

        readyQueue = new LinkedList<>();

        processList = new ArrayList<>(processes);

        // sort el process acrorrding to time from small to big 
        processList.sort(
            Comparator.comparingInt(p -> p.arrive_time)
        );

        // make sure that tier 2 is not empty 
        //you must have an object ready to take what is comming from tier 1 
        if (tierTwo == null) {
            throw new IllegalArgumentException(
                "Tier 2 handler cannot be null "
            );
        }
        //store in the created object of tier 2 

        this.tierTwo = tierTwo;
    }


    // add all the process to the ready queue
    
    private void addNewArrivals() {
        //loop according to the size 
        while (nextProcessIndex < processList.size()) {

            process p = processList.get(nextProcessIndex);
            // if condition to know if process arrived or not 

            if (p.arrive_time > currentTime) {
                break;// if it didnt not arrive ar=4 and cur=2 didnt arrive break 
            }

            // add it to queue
            readyQueue.add(p);

            System.out.println(
                p.name + " arrived at time " + currentTime
            );

            nextProcessIndex++;
        }
    }



    //member 4 azon will need this<--------
   //exectution for tier 1 
    public void executeScheduling() {

        //execute prcess didnt came or in ready queue
        // if any condition gave true complete 

        while (nextProcessIndex < processList.size()|| !readyQueue.isEmpty()) {

            // if any condition gave true complete (add)
            addNewArrivals();


            // CPU is idle if the queue is empty
            if (readyQueue.isEmpty()) {
                //move time until you find a process in ready queue

                currentTime =processList.get(nextProcessIndex).arrive_time;
               // add process which arrives
                addNewArrivals();

                continue;
            }


            // get the first process from the ready queue
            process currentProcess = readyQueue.poll();



            // -1 meaning no one has started 
            if (currentProcess.first_start_time == -1) {
                currentProcess.first_start_time = currentTime;// set current time = process time 
            }


            // calculate execution 
            //  process executes for the smaller value between the Time Quantum and its remaining burst time.
            int executionTime = Math.min(
                timeQuantum,
                currentProcess.remain_burst_time
            );


            System.out.println(
                currentProcess.name + " starts at " + currentTime
            );



            // update the remaining burst time in the shared process object.
            currentProcess.remain_burst_time -= executionTime;// subtract el time process did (execute) mn el reamining burst 


            // change current time acoording to execution if process satrted from zero till 3 current =3  
            currentTime =currentTime+ executionTime;


            // Check for processes arriving during execution
            addNewArrivals();


            // Check if the process has finished
            if (currentProcess.remain_burst_time == 0) {
                //wont go to tier 2 

                // Stores the process finishing time.
                currentProcess.finish_time = currentTime;

                System.out.println(
                    currentProcess.name + " finished at " + currentTime
                );
            }

            // Quantum expired but process is not complete
            //go to tier 2 
            else {

                System.out.println(currentProcess.name+ " quantum expired. Remaining = " + currentProcess.remain_burst_time
                );


                // transfere the unfinished process to Tier 2.(memebr 3)<---------
                tierTwo.receiveProcess(currentProcess);
            }


            // context Switching before the next Tier 1 process
            //if cond to make sure thet there is some thing in queue and you have a context switch timing 
            if (!readyQueue.isEmpty() && contextSwitchTime > 0) {

                System.out.println(
                    "Context Switching: "
                    + contextSwitchTime + " time units"
                );

                currentTime = currentTime + contextSwitchTime;


                addNewArrivals();
            }
        }
    }


    // member 4 will use this <------------
    // 4 will use it to know CT 

    public int getCurrentTime() {
        return currentTime;
    }


    //memebr 3 will use it <-----------
    public interface TierTwoHandler {

        // must have an object to recive whta is un finished in tier 1 
        void receiveProcess(process p);
    }
}
