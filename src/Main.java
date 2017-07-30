/**
 * Created by sandulmv on 28.07.17.
 */
import java.util.*;
public class Main {
    public static void main(String[] args) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(10, 10);
        removeFromMap(map, 1);
    }

    static <K> int removeFromMap(Map<K , Integer> map, K key) {
        return map.remove(key);
    }
}
