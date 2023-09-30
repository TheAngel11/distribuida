import java.util.Collections;
import java.util.LinkedList;

public class LinkedListSearch {
    private final LinkedList<Integer> list;

    public LinkedListSearch(int numElements){
        list = new LinkedList<>();

        // Add numElements elements to the list
        for(int i = 0; i < numElements; i++)
            list.add(i);

        // Shuffle the list so that the target is in a random position
        Collections.shuffle(list);
    }

    public void startSearch() {
        int target = (int)(Math.random() * list.size()); // Target is a random number in the list
        System.out.println("The list has " + list.size() + " elements");
        System.out.println("Target to find: " + target);

        Thread searchFromStart = new Thread(() -> search(target, true));
        Thread searchFromEnd = new Thread(() -> search(target, false));

        searchFromStart.start();
        searchFromEnd.start();
    }

    public void search(int target, boolean fromStart) {
        // Search from the start of the list or the end of the list
        int index = fromStart ? 0 : list.size() - 1;

        // While the index is within the bounds of the list
        while(fromStart ? index < list.size() : index >= 0) {
            // If the element at the current index is the target, print the index and return
            if(list.get(index) == target) {
                System.out.println("Thread searching from " + (fromStart ? "start" : "end") + " found target at index: " + index);
                return;
            }

            // Increment or decrement the index depending on which direction we're searching
            index = fromStart ? index + 1 : index - 1;
        }
    }
}