import java.util.Arrays;

public class ParallelArraySearch {

    public static int ParallelSearch(int target, int[] array, int numThreads){
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
        int threadID = -1;

        for(int i = 0; i < numThreads; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(results[i] != -1){
                threadID = results[i];
                break;
            }
        }

        return threadID;
    }

    public static int search(int target, int[] array, int threadNum){
        for(int i = 0; i < array.length; i++){
            if(array[i] == target){
                System.out.println("Thread found target at index: " + i);
                return threadNum;
            }
        }

        return -1;
    }
}
