package mainClass;

public class MainDemo {

	public static void main(String[] args) {
		System.out.println(2);
		System.out.println(max(1,3));
	}

	static int max(int a,int b) {
		if(a>b) return a;
		else return b;
	}
}
