
import java.util.List;

public class SJF {
    public static void runSJF(List<process> processes, int contextSwitch) {
        int currentTime = 0;
        int completed = 0;
        process lastProcess = null;
        while (completed < processes.size()) {

            // STEP 1: Find the process with the shortest remaining time
            // among all arrived and unfinished processes.
            process shortest = null; //yet there is no process selected
            for (process p : processes) {
                if (p.arrive_time <= currentTime &&
                    p.remain_burst_time > 0) {   //to check if the process has arrived and is not finished yet

                    if (shortest == null ||
                        p.remain_burst_time < shortest.remain_burst_time) {

                        shortest = p;
                    }
                }
            }
            // STEP 2: If no process is available, CPU is idle.
            if (shortest == null) {
                lastProcess = null;
                currentTime++;
                continue;
            }

            // STEP 3: Handle context switching.

            if (lastProcess != null && lastProcess != shortest) {

                for (int i = 0; i < contextSwitch; i++) {
                    currentTime++;
                }
                lastProcess = null; //inorder to recheck the shortest process after context switching

                // Recheck the shortest process after context switching.
                continue;
            }

            // STEP 4: Record the first time the process gets the CPU.

            if (shortest.first_start_time == -1) {
                shortest.first_start_time = currentTime;
            }

            // STEP 5: Execute the selected process for one time unit.
            shortest.remain_burst_time--;
            currentTime++;
            lastProcess = shortest;
            // STEP 6: Check whether the process has finished.

            if (shortest.remain_burst_time == 0) {

                shortest.finish_time = currentTime;
                completed++;
            }
        }

    }   
}
