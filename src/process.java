public class process implements IProcess{

 // SHARED CLASS:
// Members 1,2,3,4 need this class

 public String name;
 public int arrive_time;
 public int burst_time;
 public int remain_burst_time;
 public int priority;

 public int first_start_time = -1;
 public int finish_time = -1;

 // Constructor
 public process(String name,int arrive_time,int burst_time,int priority) {

 this.name = name;
 this.arrive_time = arrive_time;
 this.burst_time = burst_time;
 this.remain_burst_time = burst_time;
 this.priority = priority;
 }

 // Waiting Time Calculation
 // Used by the main scheduler and performance calculations.
 public int getwaiting_time() {

 if (finish_time == -1) {
 return -1;
 }

 return finish_time - arrive_time - burst_time;
 }

 // Turnaround Time Calculation
 public int getturnaround_time() {

 if (finish_time == -1) {
 return -1;
 }

 return finish_time - arrive_time;
 }

 // Response Time Calculation
 public int getresponse_time() {

 if (first_start_time == -1) {
 return -1;
 }

 return first_start_time - arrive_time;
 }
}
 
interface IProcess{
    
    public int getwaiting_time();
    public int getturnaround_time();
    public int getresponse_time();
}