import java.util.Arrays;

public class ParallelArraySearch {

    private static volatile boolean found = false;

    public static int ParallelSearch(int target, int[] array, int numThreads){
        if(array.length%numThreads != 0){
            throw new RuntimeException("Length of the array is not divisible by numThreads");
        }

        int slice = array.length/numThreads;
        Thread[] threads = new Thread[numThreads];
        int[] results = new int[numThreads];

        for(int i = 0; i < numThreads; i++){
            final int threadNum = i;
            int[] split = Arrays.copyOfRange(array, slice*i, slice*(i+1));
            threads[i] = new Thread(() -> {
                results[threadNum] = search(target, split, threadNum + 1);
            });

            threads[i].start();
        }

        for(int i = 0; i < numThreads; i++){
            try {
                threads[i].join();
            } catch (InterruptedException ignored) {}

            if(results[i] != -1) return results[i] + (i * slice);
        }

        return -1;
    }

    public static int search(int target, int[] array, int threadNum){
        for(int i = 0; i < array.length; i++){
            if(found) break;

            if(array[i] == target){
                System.out.println("Thread " + threadNum + " found target at index: " + i);
                found = true;
                return i;
            }
        }

        System.out.println("Stopping thread " + threadNum);
        return -1;
    }
}
