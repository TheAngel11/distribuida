import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        //Create a list of Process objects to store the heavyweight processes
        List<Process> processes = new ArrayList<>();

        try{
            // Create the two heavyweight processes
            String heavy1Socket = "localhost:3040";
            String heavy2Socket = "localhost:5020";
            ProcessBuilder heavy1 = new ProcessBuilder("java", "HeavyweightProcess.java", heavy1Socket, heavy2Socket, "A");
            ProcessBuilder heavy2 = new ProcessBuilder("java", "HeavyweightProcess.java", heavy2Socket, heavy1Socket, "B");

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
            while(true){
                Thread.sleep(1000);
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

    }
}