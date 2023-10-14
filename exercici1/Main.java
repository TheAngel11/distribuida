import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final String MENU_MSG = """
                                  Select an option to run (1, 2 or 3): "
                            
                                      1. Exercise 3
                            
                                      2. Exercise 4 & 5
                             
                                      3. Exercise 7 & 8
                            
                                  Insert your option:\s""";

    public static void main(String[] args) {
        System.out.print(MENU_MSG);
        //int option = Integer.parseInt(new Scanner(System.in).nextLine());
        int option = 3;
        int ARRAY_POSITIONS, INT_TO_SEARCH, NUM_THREADS;
        long startTime, endTime;

        switch(option){
            case 1:
                //Exercise 3
                LinkedListSearch llSearch = new LinkedListSearch(10000);
                llSearch.startSearch();
                break;
            case 2:
                //Exercise 4 & 5
                ARRAY_POSITIONS = 140000000;
                INT_TO_SEARCH = 250000;
                NUM_THREADS = 4;
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < ARRAY_POSITIONS; i++)
                    list.add(i);

                //Collections.shuffle(list);
                System.out.println("Searching for " + INT_TO_SEARCH + " with " + NUM_THREADS + " threads on an array with " + ARRAY_POSITIONS + " positions\n");
                int[] array = list.stream().mapToInt(Integer::intValue).toArray();

                System.out.println("With Shared Memory --> ");
                startTime = System.currentTimeMillis();
                int box = ParallelArraySearch.ParallelSearchSharedMemory(INT_TO_SEARCH, array, NUM_THREADS);
                endTime = System.currentTimeMillis();
                System.out.println("Found the target at index " + box + " with " + (endTime-startTime) + "ms.");

                System.out.println("With Shared Nothing --> ");
                startTime = System.currentTimeMillis();
                box = ParallelArraySearch.ParallelSearch(INT_TO_SEARCH, array, NUM_THREADS);
                endTime = System.currentTimeMillis();
                System.out.println("Found the target at index " + box + " with " + (endTime-startTime) + "ms.");
                break;
            case 3:
                // Exercise 7 & 8
                ARRAY_POSITIONS = 10000000;
                ArrayList<Integer> unorderedList = new ArrayList<>();
                for (int i = 0; i < ARRAY_POSITIONS; i++)
                    unorderedList.add(i);

                Collections.shuffle(unorderedList);
                System.out.println("Sorting array with merge sort. The array has " + ARRAY_POSITIONS + " positions\n");

                System.out.println("With Shared Memory (Sequential Execution) --> ");
                startTime = System.currentTimeMillis();
                MergeSort.sort(unorderedList);
                endTime = System.currentTimeMillis();
                System.out.println("Array Ordered with " + (endTime-startTime) + "ms.");

                System.out.println("With Shared Nothing (Parallel Execution) --> ");
                startTime = System.currentTimeMillis();
                MultithreadedMergeSort.sort(unorderedList);
                endTime = System.currentTimeMillis();
                System.out.println("Array Ordered with " + (endTime-startTime) + "ms.");
                break;
            default:
                System.out.println("Select a valid option (1, 2 or 3)");
        }
    }
}