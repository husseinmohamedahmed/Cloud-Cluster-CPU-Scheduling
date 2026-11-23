
import java.util.*;
public class NonPreemptivePriorityScheduler implements RoundRobinScheduler.TierTwoHandler{

    ArrayList<process> queue = new ArrayList<>();



    @Override
    public void receiveProcess(process p) {
        queue.add(p);

    }
      //t => the clock time where Tier 1 stopped

    public int run (int t , int cs) {

        while (!queue.isEmpty()) {


            String tier2Line = "Tier 2: [";
            for (int i = 0; i <queue.size(); i++) {
                tier2Line = tier2Line + queue.get(i) .name + " (remaining: " + queue.get(i) .remain_burst_time
                        + ", Priority: " + queue.get(i).priority+ ")";
                if (i <queue.size() - 1) {
                    tier2Line = tier2Line + ", ";
                }
            }
            tier2Line = tier2Line + "]";



            process p = pickNext();


            if (cs > 0) {
                System.out.println(t + "   Context switch loads " + p.name);
                System.out.println("     Tier 1: [Empty]");
                System.out.println("     " + tier2Line);
                t = t + cs;
                System.out.println(t + "   " + p.name);
            } else {
                System.out.println(t + "   " + p.name);
                System.out.println("     Tier 1: [Empty]");
                System.out.println("     " + tier2Line);
            }


            t = t + p.remain_burst_time;
            p.remain_burst_time = 0;

            p.finish_time = t;


        }
        System.out.println(t + "   Tier 1: [Empty]");
        System.out.println("     Tier 2: [Empty]");
        return t;
    }

    public process pickNext(){
        int index = 0;

        for (int i=1 ;i<queue.size();i++){ //Go through slot 1, 2, 3 and so on, up to the last waiting process.
                                    //Is this one's priority number smaller than the current best?
            if (queue.get(i).priority<queue.get(index).priority){


                index=i;
            }
        }
        process best = queue.remove(index);


        return best ;

    }







}
