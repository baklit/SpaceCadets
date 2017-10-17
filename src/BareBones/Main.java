package BareBones;

public class Main {
	public static void main(String[] args){
		
		BareBonesVM vm = new BareBonesVM();
		
		vm.load("BareBonesProgram.txt");
		
		long initialTime = System.nanoTime();
		
		for (int i = 0; i < 1000000; i++) {
			vm.execute();
			vm.resetState();
		}
		
		long timeTaken = System.nanoTime() - initialTime;
		System.out.println("It took " + timeTaken / 1000000.0 + "ms");
	}
}
