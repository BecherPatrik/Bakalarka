package trees;

public interface ITree {
	Result insert(int value);
	Result delete(int value);    
    Result search(int value);
    INode getRoot();
	void disableBalance();
	void enableBalance();    
}