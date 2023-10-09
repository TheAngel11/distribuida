import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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
        int option = 2;

        switch(option){
            case 1:
                //Exercise 3
                LinkedListSearch llSearch = new LinkedListSearch(10000);
                llSearch.startSearch();
                break;
            case 2:
                //Exercise 4 & 5
                final int ARRAY_POSITIONS = 140000000;
                final int INT_TO_SEARCH = 250000;
                final int NUM_THREADS = 4;
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < ARRAY_POSITIONS; i++)
                    list.add(i);

                //Collections.shuffle(list);
                System.out.println("Searching for " + INT_TO_SEARCH + " with " + NUM_THREADS + " threads on an array with " + ARRAY_POSITIONS + " positions\n");
                int[] array = list.stream().mapToInt(Integer::intValue).toArray();

                System.out.println("With Shared Memory --> ");
                long startTime = System.currentTimeMillis();
                int box = ParallelArraySearch.ParallelSearchSharedMemory(INT_TO_SEARCH, array, NUM_THREADS);
                long endTime = System.currentTimeMillis();
                System.out.println("Found the target at index " + box + " with " + (endTime-startTime) + "ms.");

                System.out.println("With Shared Nothing --> ");
                startTime = System.currentTimeMillis();
                box = ParallelArraySearch.ParallelSearch(INT_TO_SEARCH, array, NUM_THREADS);
                endTime = System.currentTimeMillis();
                System.out.println("Found the target at index " + box + " with " + (endTime-startTime) + "ms.");
                break;

            case 3:
                // Exercise 7 & 8
                break;
            default:
                System.out.println("Select a valid option (1, 2 or 3)");
        }
    }
}