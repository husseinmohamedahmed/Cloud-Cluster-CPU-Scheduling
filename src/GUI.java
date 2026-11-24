
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GUI extends JFrame {

    private JComboBox<String> algorithmBox;

    private JTextField quantumField;
    private JTextField contextSwitchField;

    private JTextArea processInput;
    private JTextArea outputArea;

    private JButton runButton;

    // ================= CONSTRUCTOR =================

    public GUI() {

        setTitle("Cloud Cluster CPU Scheduling");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        // ================= TOP PANEL =================

        JPanel topPanel = new JPanel(
                new GridLayout(3, 2, 10, 10)
        );

        // Algorithm Selection
        topPanel.add(new JLabel("Select Scheduling Algorithm:"));

        algorithmBox = new JComboBox<>(new String[]{
                "Round Robin + Tier 2 Priority",
                "SJF (SRTF)",
                "Adaptive Container Scheduler (ACS)"
        });

        topPanel.add(algorithmBox);

        // Time Quantum
        topPanel.add(new JLabel("Time Quantum:"));

        quantumField = new JTextField("2");
        topPanel.add(quantumField);

        // Context Switch
        topPanel.add(new JLabel("Context Switch Time:"));

        contextSwitchField = new JTextField("1");
        topPanel.add(contextSwitchField);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ================= CENTER PANEL =================

        JPanel centerPanel = new JPanel(
                new GridLayout(2, 1, 10, 10)
        );

        // Process Input Panel
        JPanel inputPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        JLabel inputLabel = new JLabel(
                "Process Input (Name, Arrival Time, Burst Time, Priority):"
        );

        processInput = new JTextArea(8, 40);

        processInput.setText(
                "P1,0,5,2\n" +
                "P2,1,3,1\n" +
                "P3,2,4,3\n" +
                "P4,1,2,8"
        );

        JScrollPane inputScroll = new JScrollPane(processInput);

        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Output Panel
        JPanel outputPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        JLabel outputLabel = new JLabel("Scheduling Output:");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane outputScroll = new JScrollPane(outputArea);

        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        centerPanel.add(inputPanel);
        centerPanel.add(outputPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ================= BOTTOM PANEL =================

        runButton = new JButton("Run Scheduling");

        runButton.addActionListener(e -> runScheduling());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(runButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // ================= READ PROCESSES =================

    private List<process> readProcesses() {

        List<process> processes = new ArrayList<>();

        String input = processInput.getText().trim();

        if (input.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter processes."
            );
        }

        String[] lines = input.split("\\R");

        for (String line : lines) {

            if (line.trim().isEmpty()) {
                continue;
            }

            String[] values = line.trim().split(",");

            if (values.length != 4) {
                throw new IllegalArgumentException(
                        "Invalid input format:\n" + line +
                        "\nExpected: Name,Arrival,Burst,Priority"
                );
            }

            String name = values[0].trim();

            if (name.isEmpty()) {
                throw new IllegalArgumentException(
                        "Process name cannot be empty."
                );
            }

            int arrivalTime = Integer.parseInt(values[1].trim());
            int burstTime = Integer.parseInt(values[2].trim());
            int priority = Integer.parseInt(values[3].trim());

            if (arrivalTime < 0 || burstTime <= 0) {
                throw new IllegalArgumentException(
                        "Arrival time must be >= 0 and Burst time must be > 0."
                );
            }

            processes.add(
                    new process(
                            name,
                            arrivalTime,
                            burstTime,
                            priority
                    )
            );
        }

        if (processes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid processes found."
            );
        }

        return processes;
    }

    // ================= COPY PROCESSES =================

    private List<process> copyProcesses(List<process> original) {

        List<process> copiedProcesses = new ArrayList<>();

        for (process p : original) {

            copiedProcesses.add(
                    new process(
                            p.name,
                            p.arrive_time,
                            p.burst_time,
                            p.priority
                    )
            );
        }

        return copiedProcesses;
    }

    // ================= RUN SCHEDULING =================

    private void runScheduling() {

        try {

            // Read Processes
            List<process> originalProcesses = readProcesses();

            // Create independent process objects
            List<process> processes = copyProcesses(originalProcesses);

            // Read Context Switch
            int contextSwitch = Integer.parseInt(
                    contextSwitchField.getText().trim()
            );

            if (contextSwitch < 0) {
                throw new IllegalArgumentException(
                        "Context Switch Time cannot be negative."
                );
            }

            // Read Selected Algorithm
            String selectedAlgorithm =
                    (String) algorithmBox.getSelectedItem();

            // Read Quantum for RR and ACS
            int quantum = 0;

            if ("Round Robin + Tier 2 Priority".equals(selectedAlgorithm)
                    || "Adaptive Container Scheduler (ACS)".equals(selectedAlgorithm)) {

                quantum = Integer.parseInt(
                        quantumField.getText().trim()
                );

                if (quantum <= 0) {
                    throw new IllegalArgumentException(
                            "Time Quantum must be greater than zero."
                    );
                }
            }

            // Disable Button
            runButton.setEnabled(false);

            outputArea.setText("Running scheduling...\n");

            final int finalQuantum = quantum;
            final int finalContextSwitch = contextSwitch;
            final String finalAlgorithm = selectedAlgorithm;

            // ================= BACKGROUND EXECUTION =================

            SwingWorker<String, Void> worker =
                    new SwingWorker<String, Void>() {

                @Override
                protected String doInBackground() throws Exception {

                    ByteArrayOutputStream outputStream =
                            new ByteArrayOutputStream();

                    PrintStream originalOut = System.out;
                    PrintStream originalErr = System.err;

                    PrintStream capturedOutput =
                            new PrintStream(outputStream, true);

                    try {

                        System.setOut(capturedOutput);
                        System.setErr(capturedOutput);

                        // ================= ROUND ROBIN =================

                        if ("Round Robin + Tier 2 Priority"
                                .equals(finalAlgorithm)) {

                            System.out.println(
                                    "========== ROUND ROBIN + TIER 2 PRIORITY ==========\n"
                            );

                            NonPreemptivePriorityScheduler tierTwo =
                                    new NonPreemptivePriorityScheduler();

                            RoundRobinScheduler tierOne =
                                    new RoundRobinScheduler(
                                            finalQuantum,
                                            finalContextSwitch,
                                            processes,
                                            tierTwo
                                    );

                            // Execute Tier 1
                            tierOne.executeScheduling();

                            // Execute Tier 2
                            int finalTime = tierTwo.run(
                                    tierOne.getCurrentTime(),
                                    finalContextSwitch
                            );

                            System.out.println(
                                    "\nFinal Completion Time: " + finalTime
                            );
                        }

                        // ================= SJF (SRTF) =================

                        else if ("SJF (SRTF)".equals(finalAlgorithm)) {

                            System.out.println(
                                    "========== SJF (SRTF) ==========\n"
                            );

                            SJF.runSJF(
                                    processes,
                                    finalContextSwitch
                            );

                            System.out.println(
                                    "\nScheduling completed successfully."
                            );
                        }

                        // ================= ACS =================

                        else if ("Adaptive Container Scheduler (ACS)"
                                .equals(finalAlgorithm)) {

                            System.out.println(
                                    "========== ADAPTIVE CONTAINER SCHEDULER (ACS) ==========\n"
                            );

                            ACS acs = new ACS(
                                    finalQuantum,
                                    finalContextSwitch
                            );

                            // Run Tier 1 (Round Robin)
                            // Run Tier 2 (Non-Preemptive Priority)
                            // Apply Tier 3 Aging
                            // Print final metrics
                            acs.startSimulation(processes);
                        }

                    } finally {

                        capturedOutput.flush();

                        System.setOut(originalOut);
                        System.setErr(originalErr);

                        capturedOutput.close();
                    }

                    return outputStream.toString();
                }

                // ================= DISPLAY OUTPUT =================

                @Override
                protected void done() {

                    try {

                        String schedulingOutput = get();

                        if (schedulingOutput.trim().isEmpty()) {

                            schedulingOutput =
                                    "No execution trace was printed by the schedulers.\n";
                        }

                        outputArea.setText(schedulingOutput);

                        // ACS already prints its own metrics
                        // Avoid displaying metrics twice

                        if (!"Adaptive Container Scheduler (ACS)"
                                .equals(finalAlgorithm)) {

                            displayMetrics(processes);
                        }

                    } catch (InterruptedException ex) {

                        Thread.currentThread().interrupt();

                        outputArea.setText(
                                "Scheduling was interrupted."
                        );

                    } catch (ExecutionException ex) {

                        Throwable cause = ex.getCause();

                        String errorMessage =
                                cause.getMessage() != null
                                        ? cause.getMessage()
                                        : cause.toString();

                        outputArea.setText(
                                "Scheduling Error:\n" + errorMessage
                        );

                        JOptionPane.showMessageDialog(
                                GUI.this,
                                errorMessage,
                                "Scheduling Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } finally {

                        runButton.setEnabled(true);
                    }
                }
            };

            worker.execute();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid numeric values.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= DISPLAY METRICS =================

    private void displayMetrics(List<process> processes) {

        StringBuilder result = new StringBuilder();

        result.append(
                "\n\n========== PROCESS METRICS ==========\n\n"
        );

        result.append(
                String.format(
                        "%-12s %-15s %-15s %-15s\n",
                        "Process",
                        "Waiting Time",
                        "Turnaround",
                        "Response Time"
                )
        );

        result.append(
                "---------------------------------------------------------------\n"
        );

        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;

        for (process p : processes) {

            int waitingTime = p.getwaiting_time();
            int turnaroundTime = p.getturnaround_time();
            int responseTime = p.getresponse_time();

            result.append(
                    String.format(
                            "%-12s %-15d %-15d %-15d\n",
                            p.name,
                            waitingTime,
                            turnaroundTime,
                            responseTime
                    )
            );

            totalWaiting += waitingTime;
            totalTurnaround += turnaroundTime;
            totalResponse += responseTime;
        }

        int numberOfProcesses = processes.size();

        result.append(
                "\n========== AVERAGE METRICS ==========\n"
        );

        result.append(
                String.format(
                        "Average Waiting Time: %.2f\n",
                        totalWaiting / numberOfProcesses
                )
        );

        result.append(
                String.format(
                        "Average Turnaround Time: %.2f\n",
                        totalTurnaround / numberOfProcesses
                )
        );

        result.append(
                String.format(
                        "Average Response Time: %.2f\n",
                        totalResponse / numberOfProcesses
                )
        );

        outputArea.append(result.toString());
    }

    // ================= MAIN =================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            GUI gui = new GUI();

            gui.setVisible(true);
        });
    }
}
