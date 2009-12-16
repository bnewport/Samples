package utils;

public class Test {

	static <K> void test(K x)
	{
		Class theClass = x.getClass();
		System.out.println(theClass.toString());
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Integer i = new Integer(0);
		String s = "";
		
		test(i);
		test(s);
	}

}
