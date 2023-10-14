import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class MultithreadedMergeSort extends RecursiveTask<ArrayList<Integer>> {

    private final ArrayList<Integer> list;

    private MultithreadedMergeSort(ArrayList<Integer> list) {
        this.list = list;
    }

    @Override
    protected ArrayList<Integer> compute() {
        if (list.size() <= 1)
            return list;

        int mid = list.size() / 2;
        ArrayList<Integer> leftPart = new ArrayList<>(list.subList(0, mid));
        ArrayList<Integer> rightPart = new ArrayList<>(list.subList(mid, list.size()));

        MultithreadedMergeSort leftTask = new MultithreadedMergeSort(leftPart);
        MultithreadedMergeSort rightTask = new MultithreadedMergeSort(rightPart);

        // Forks the left task and waits for it to finish
        invokeAll(leftTask, rightTask);

        // Joins the results of the left and right tasks
        ArrayList<Integer> leftResult = leftTask.join();
        ArrayList<Integer> rightResult = rightTask.join();

        // Merges the results of the left and right tasks
        return MergeSort.merge(leftResult, rightResult);
    }


    /**
     * Sorts the given list using the merge sort algorithm, in parallel (using multiple threads).
     * @param list The list to be sorted.
     * @return The sorted list.
     */
    public static ArrayList<Integer> sort(ArrayList<Integer> list) {
        /*
         *
         * We tried with raw threads (normalThreadedMergeSort), but as the number of threads increased,
         *  the JVM run out of memory.
         * The ForkJoinPool does not create new threads recklessly. It has a controlled set of worker threads,
         *  and it schedules tasks on these threads. The pool uses a work-stealing algorithm, which allows idle
         *  threads to "steal" tasks from busy threads, optimizing CPU usage.
         * It uses as many threads concurrently as the number of cores in the CPU.
         *
         */

        return new ForkJoinPool().invoke(new MultithreadedMergeSort(list));
    }


    private static ArrayList<Integer> normalThreadedMergeSort(ArrayList<Integer> list) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        ArrayList<Integer> leftPart = new ArrayList<>(list.subList(0, mid));
        ArrayList<Integer> rightPart = new ArrayList<>(list.subList(mid, list.size()));

        ArrayList<Integer> leftResult = new ArrayList<>();
        ArrayList<Integer> rightResult = new ArrayList<>();

        // We create two threads, one for each part of the list, and wait for them to finish
        Thread leftThread = new Thread(() -> leftResult.addAll(normalThreadedMergeSort(leftPart)));
        Thread rightThread = new Thread(() -> rightResult.addAll(normalThreadedMergeSort(rightPart)));

        leftThread.start();
        rightThread.start();

        try {
            leftThread.join();
            rightThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return MergeSort.merge(leftResult, rightResult);
    }
}