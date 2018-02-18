package Trees;

public interface ITree<T> {
	Result<T> delete(int value);
    Result<T> insert(int value);
    T getRoot();
    Result<T> search(int value);
}