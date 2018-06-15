package Trees;

public interface ITree<T> {
	Result<T> insert(int value);
	Result<T> delete(int value);    
    Result<T> search(int value);
    T getRoot();
	void disableBalance();
	void enableBalance();    
}