package mvasoft.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionsUtils {
    private CollectionsUtils() {}

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <Key, Value> Collection<List<Value>> group(Iterable<Value> sessions, Function<Value, ? extends Key> keyFunction) {
        Map<Key, List<Value>> map = new HashMap<>();
        for (Value v : sessions) {
            Key key = keyFunction.apply(v);
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<Value>());
            }
            map.get(key).add(v);
        }

        return map.values();
    }

    public static <T> Boolean contains(Iterable<? extends T> iterable, Function<T, Boolean> checkFunction) {
        if (iterable == null)
            return false;

        for (T item : iterable)
            if (checkFunction.apply(item))
                return true;
        return false;
    }


    public interface Function<V, R> {
        R apply(V value);
    }

}
