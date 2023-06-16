package routing.Algorithm.UnionFind;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class QuickFind<T> implements IUnionFind<T>{
    //List of own type and parrent
    List<T> data;
    List<T> parent;
    @Override
    public void init(List<T> pointers) {
        data = new ArrayList<>(pointers);
        parent = new ArrayList<>(pointers);
    }

    @Override
    public boolean union(T i, T j) {
        if (find(i).equals(find(j))) return false;
        parent.set(data.indexOf(j), i);
        return true;
    }

    @Override
    public T find(T i) {
        T myParrent = parent.get(data.indexOf(i));
        if (!myParrent.equals(i)) return find(myParrent);
        return i;
    }
}
