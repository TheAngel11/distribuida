import java.util.Arrays;

public class ParallelArraySearch {

    public static int ParallelSearch(int target, int[] array, int numThreads){
        int slice = array.length/numThreads;

        for(int i = 0; i < numThreads; i++){
            int[] split = Arrays.copyOfRange(array, slice*i, slice*(i+1));
            Thread t = new Thread(() -> search(target, split));
            t.start();
        }

        return -1;
    }

    public static int search(int target, int[] array){
        for(int i = 0; i < array.length; i++){
            if(array[i] == target){
                return;
            }
        }
    }
}
