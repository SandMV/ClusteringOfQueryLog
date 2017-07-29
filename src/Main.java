/**
 * Created by sandulmv on 28.07.17.
 */
import java.util.*;
public class Main {
    public static void main(String[] args) {
        Set<Integer> set = new HashSet<>();
        set.add(1); set.add(2); set.add(3);
        Set<Integer> unmodifiableSet = Collections.unmodifiableSet(set);
        Set<Integer> anotherSet = new HashSet<>();
        anotherSet.add(2); anotherSet.add(3); anotherSet.add(4);
        String result = unmodifiableSet.retainAll(anotherSet) ? "modified" : "not modified";
        System.out.print(result);
    }
}
