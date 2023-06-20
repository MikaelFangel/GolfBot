package routing.Algorithm.UnionFind;

import java.util.List;

public interface IUnionFind<T>{
    void init (List<T> pointers);
    boolean union(T i, T j) throws IndexOutOfBoundsException;
    T find(T i) throws IndexOutOfBoundsException;
}
