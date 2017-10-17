package RapidBareBones;

public class Main {
	public static void main(String[] args) {
		
		BareBonesCompiler compiler = new BareBonesCompiler();
		//compiler.debugMode = true;
		
		compiler.load("BareBonesProgram.txt");
		RapidBareBonesVM vm = new RapidBareBonesVM(compiler.compile());
		
		long initialTime = System.nanoTime();
		
		for (int i = 0; i < 1000000; i++) {
			vm.run();
			vm.resetState();
		}
		
		long timeTaken = System.nanoTime() - initialTime;
		System.out.println("It took " + timeTaken / 1000000.0 + "ms");
		
		
	}
}
