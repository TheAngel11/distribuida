import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MergeSort {

    public static ArrayList<Integer> parallelMergeSort(ArrayList<Integer> list){
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ArrayList<Integer> result = null;

        try {
            result = parallelMergeSortFacade(list, executor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        return result;
    }


    /**
     * Sorts an array of integers using the merge sort algorithm, in parallel (using multiple threads)
     * @param list The list to sort
     * @return The sorted list
     */
    public static ArrayList<Integer> parallelMergeSortFacade(ArrayList<Integer> list, ExecutorService executor)
            throws InterruptedException, ExecutionException {
        if (list.size() <= 1) {
            return list;
        }

        // Split the list in two parts
        int mid = list.size() / 2;
        ArrayList<Integer> leftPart = new ArrayList<>(list.subList(0, mid));
        ArrayList<Integer> rightPart = new ArrayList<>(list.subList(mid, list.size()));

        // Create two threads to sort the two parts in parallel
        Future<ArrayList<Integer>> leftResult = executor.submit(() -> parallelMergeSortFacade(leftPart, executor));
        Future<ArrayList<Integer>> rightResult = executor.submit(() -> parallelMergeSortFacade(rightPart, executor));

        // Merge the results from the two threads
        return merge(leftResult.get(), rightResult.get());
    }


    /**
     * Sorts an array of integers using the merge sort algorithm, sequentially
     * @param list The list to sort
     * @return The sorted list
     */
    public static ArrayList<Integer> sequentialMergeSort(ArrayList<Integer> list) {
        if (list.size() <= 1)
            return list;

        int mid = list.size() / 2;
        ArrayList<Integer> leftPart = new ArrayList<>(list.subList(0, mid));
        ArrayList<Integer> rightPart = new ArrayList<>(list.subList(mid, list.size()));

        leftPart = sequentialMergeSort(leftPart);
        rightPart = sequentialMergeSort(rightPart);

        return merge(leftPart, rightPart);
    }


    /**
     * Merge two ordered lists into one ordered list
     * @param leftPart The left part of the list
     * @param rightPart The right part of the list
     * @return The merged, ordered list
     */
    private static ArrayList<Integer> merge(ArrayList<Integer> leftPart, ArrayList<Integer> rightPart) {
        ArrayList<Integer> arrayOrdered = new ArrayList<>();
        int l = 0, r = 0;

        while (l < leftPart.size() || r < rightPart.size()) {
            if (l == leftPart.size()) {
                arrayOrdered.add(rightPart.get(r++));
            } else if (r == rightPart.size()) {
                arrayOrdered.add(leftPart.get(l++));
            } else if (leftPart.get(l) < rightPart.get(r)) {
                arrayOrdered.add(leftPart.get(l++));
            } else {
                arrayOrdered.add(rightPart.get(r++));
            }
        }

        return arrayOrdered;
    }
}