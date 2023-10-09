import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        //Exercise 3
        LinkedListSearch llSearch = new LinkedListSearch(10000);
        llSearch.startSearch();

        //Exercise 4
        final int ARRAY_POSITIONS = 10000;
        final int INT_TO_SEARCH = 300;
        final int NUM_THREADS = 5;
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < ARRAY_POSITIONS; i++)
            list.add(i);

        Collections.shuffle(list);
        int[] array = list.stream().mapToInt(Integer::intValue).toArray();
        ParallelArraySearch.ParallelSearch(INT_TO_SEARCH, array, NUM_THREADS);
    }
}