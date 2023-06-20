package routing.Algorithm.UnionFind;

import java.util.List;

public interface IUnionFind<T>{
    void init (List<T> pointers);
    boolean union(T i, T j);
    T find(T i);
}
