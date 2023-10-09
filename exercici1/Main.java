import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        //Exercise 3
        //LinkedListSearch llSearch = new LinkedListSearch(10000);
        //llSearch.startSearch();

        //Exercise 4
        final int ARRAY_POSITIONS = 12000000;
        final int INT_TO_SEARCH = 2500;
        final int NUM_THREADS = 6;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < ARRAY_POSITIONS; i++)
            list.add(i);

        Collections.shuffle(list);
        System.out.println("Searching for " + INT_TO_SEARCH + " with " + NUM_THREADS + " threads on an array with " + ARRAY_POSITIONS + " positions");
        int[] array = list.stream().mapToInt(Integer::intValue).toArray();
        long startTime = System.currentTimeMillis();
        int box = ParallelArraySearch.ParallelSearch(INT_TO_SEARCH, array, NUM_THREADS);
        long endTime = System.currentTimeMillis();
        System.out.println("Found the target at index " + box + " with " + (endTime-startTime) + "ms.");
    }
}