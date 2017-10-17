package RapidBareBones;

import java.util.Stack;
import java.util.Vector;

public class RapidBareBonesVM {
	//private Vector<Byte> bytecode = new Vector<Byte>();
	private final Byte[] bytecode;
	private int pc = 0;
	private Stack<Vector<Integer>> stack = new Stack<Vector<Integer>>();
	private int stackdepth = 0;
	private boolean running = false;
	
	public RapidBareBonesVM(Vector<Byte> code) {
		bytecode = code.toArray(new Byte[0]);
	}
	
	public void run() {
		running = true;
		stack.push(new Vector<Integer>());
		
		while (running)
			executeop();
		
		//System.out.println("RapidBareBones VM has stopped.");
	}
	
	public void resetState() {
		pc = 0;
		stack = new Stack<Vector<Integer>>();
		stackdepth = 0;
		running = false;
	}
	
	private void genScope() {
		stack.push(new Vector<Integer>());
		++stackdepth;
	}
	
	private void delScope() {
		stack.pop();
		--stackdepth;
	}
	
	private void executeop() {
		Integer var;
		Byte status;
		Byte b1;
		Byte b2;
		
		switch(bytecode[pc]) {
			case 0x00: //null op
				System.out.println("Error: Null operation");
				running = false;
				break;
				
			case 0x01: //clear
				b1 = bytecode[pc+1];
				b2 = bytecode[pc+2];
				Vector<Integer> targetFrame = stack.get(b1);
				if (targetFrame.size() == b2)
					targetFrame.add(0);
				else if (targetFrame.size()-1 == b2)
					targetFrame.set(b2, 0);
				else {
					System.out.println("Error: Invalid variable pointer!");
					running = false;
				}
				pc += 3;
				break;
			
			case 0x02: //incr
				b1 = bytecode[pc+1];
				b2 = bytecode[pc+2];
				var = stack.get(b1).get(b2);
				stack.get(b1).set(b2, var + 1);
				pc += 3;
				break;
				
			case 0x03: //decr
				b1 = bytecode[pc+1];
				b2 = bytecode[pc+2];
				var = stack.get(b1).get(b2);
				stack.get(b1).set(b2, var - 1);
				pc += 3;
				break;
				
			case 0x04: //while op
				System.out.println("Error: Undefined while");
				running = false;
				break;
			
			case 0x05: //whileim
				status = evalWhile(pc);
				if (status == pc + 6) {
					genScope();
				}
				pc = status;
				break;
			
			case 0x06: //whilefm
				status = evalWhile(pc);
				if (status == pc + 7) {
					genScope();
				}
				pc = status;
				break;
				
			case 0x07: //end
				Byte whilePointer = bytecode[pc+1];
				status = evalWhile(whilePointer);
				
				if (status == pc+2) {
					delScope();
					pc += 2;
				}else {
					if (bytecode[whilePointer] == 0x05) {
						pc = whilePointer + 6;
					} else {
						pc = whilePointer + 7;
					}
				}
				
				break;
			
			case 0x08: //output
				System.out.println(stack.get(bytecode[pc+1]).get(bytecode[pc+2]));
				pc += 3;
				
				break;
			default:
				System.out.println("Error: Unknown operation " + pc);
				running = false;
		}

		if (pc >= bytecode.length) {
			running = false;
			//System.out.println("RapidBareBones VM has completed.");
		}
		
	}
	
	private final Byte evalWhile(int whilePointer) {
		
		Integer var = 0;
		Integer literal = 0;
		Byte comparison = 0;
		Byte endPointer = 0;
		Byte truePointer = 0;
		
		switch(bytecode[whilePointer]) {
			case 0x05: //whileim
				var = stack.get(bytecode[whilePointer+1]).get(bytecode[whilePointer+2]);
				literal = (int) bytecode[whilePointer+3];
				comparison = bytecode[whilePointer+4];
				endPointer = bytecode[whilePointer+5];
				truePointer = (byte) (whilePointer+6);
				
				break;
				
			case 0x06: //whilefm
				var = stack.get(bytecode[whilePointer+1]).get(bytecode[whilePointer+2]);
				literal =  stack.get(stackdepth - bytecode[whilePointer+3]).get(bytecode[whilePointer+4]);
				comparison = bytecode[whilePointer+5];
				endPointer = bytecode[whilePointer+6];
				truePointer = (byte) (whilePointer+7);
				
				break;
			
			default:
				System.out.println("Error: Undefined while at " + whilePointer);
				running = false;
		}
		
		switch(comparison) {
			case 0x00:
				return var != literal ? truePointer : endPointer;
			case 0x01:
				return var == literal ? truePointer : endPointer;
			case 0x02:
				return var >  literal ? truePointer : endPointer;
			case 0x03:
				return var <  literal ? truePointer : endPointer;
			case 0x04:
				return var >= literal ? truePointer : endPointer;
			case 0x05:
				return var <= literal ? truePointer : endPointer;
			
			default:
				System.out.println("Error: Unknown comparison method " + comparison);
				running = false;
				return 0;
				
		}
	}
}
