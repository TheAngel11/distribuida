import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class MultithreadedMergeSort extends RecursiveTask<ArrayList<Integer>> {

    private final ArrayList<Integer> list;

    public MultithreadedMergeSort(ArrayList<Integer> list) {
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
        return merge(leftResult, rightResult);
    }

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

    public static ArrayList<Integer> parallelMergeSort(ArrayList<Integer> list) {
        return new ForkJoinPool().invoke(new MultithreadedMergeSort(list));
    }

}