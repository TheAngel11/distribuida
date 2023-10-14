import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        //Create a list of Process objects to store the heavyweight processes
        List<Process> processes = new ArrayList<>();

        try{
            // Create the two heavyweight processes
            String heavy1Socket = "localhost:3050";
            String heavy2Socket = "localhost:3060";
            ProcessBuilder heavy1 = new ProcessBuilder("java", "exercici2/HeavyweightProcess.java", heavy1Socket, heavy2Socket, "A");
            ProcessBuilder heavy2 = new ProcessBuilder("java", "exercici2/HeavyweightProcess.java", heavy2Socket, heavy1Socket, "B");

            //Inherit the IO from the parent process (important to see the output of the heavyweight processes)
            heavy1.inheritIO();
            heavy2.inheritIO();

            // Start the heavyweight processes
            processes.add(heavy1.start());
            processes.add(heavy2.start());

            // Add a shutdown hook to destroy the heavyweight processes when this process is killed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (Process proc : processes) {
                    proc.destroy();
                }
            }));

            //Don't let the main process die. The heavyweight processes will be destroyed when this process is killed
            while(true);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

    }
}