import java.util.ArrayList;

public class MergeSort {

    /**
     * Sorts the given list using the merge sort algorithm, sequentially.
     * @param list The list to be sorted.
     * @return The sorted list.
     */
    public static ArrayList<Integer> sort(ArrayList<Integer> list) {
        if (list.size() <= 1)
            return list;

        int mid = list.size() / 2;
        ArrayList<Integer> leftPart = new ArrayList<>(list.subList(0, mid));
        ArrayList<Integer> rightPart = new ArrayList<>(list.subList(mid, list.size()));

        leftPart = sort(leftPart);
        rightPart = sort(rightPart);

        return merge(leftPart, rightPart);
    }

    /**
     * Merges two ordered lists into one ordered list.
     * @param leftPart The left part of the list.
     * @param rightPart The right part of the list.
     * @return The merged, ordered list.
     */
    protected static ArrayList<Integer> merge(ArrayList<Integer> leftPart, ArrayList<Integer> rightPart) {
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
