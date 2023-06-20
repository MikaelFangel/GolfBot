package routing.Algorithm.UnionFind;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class QuickFind<T> implements IUnionFind<T>{
    //List of own type and parrent
    private List<T> data;
    private List<T> parent;
    @Override
    public void init(List<T> pointers) {
        data = new ArrayList<>(pointers);
        parent = new ArrayList<>(pointers);
    }

    /**
     * tries and union 2 objects if they are not already joined.
     *
     * @param i object 1 reference
     * @param j object 2 reference
     * @return whether the union were possible
     * @throws IndexOutOfBoundsException If the object doesn't exsist
     */
    @Override
    public boolean union(T i, T j) throws IndexOutOfBoundsException {
        if (find(i).equals(find(j))) return false;
        parent.set(data.indexOf(find(j)), find(i));
        return true;
    }

    /**
     * Recursive search for the parrent of the given object, until no parrent is found
     *
     * @param i object to find the parrent off
     * @return the parrent of the object
     * @throws IndexOutOfBoundsException if the object doesn't exsist in the list
     */
    @Override
    public T find(T i) throws IndexOutOfBoundsException{
        T myParrent;
        try {
            myParrent = parent.get(data.indexOf(i));
        } catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("element not found");
        }
        if (!myParrent.equals(i)) return find(myParrent);
        return i;
    }
}
