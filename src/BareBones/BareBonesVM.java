package BareBones;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;


class WhileFrame{
	
	private static Pattern primtiveInt = Pattern.compile("\\d+");
	
	int start;
	int end;
	int literal;
	String comparison;
	String variable;
	String comparedTo;
	boolean usingLiteral;
	
	WhileFrame(int start, int end, String comparison, String variable, String comparedTo){
		this.start = start;
		this.end = end;
		this.comparison = comparison;
		this.variable = variable;
		this.comparedTo = comparedTo;
		
		Matcher m = primtiveInt.matcher(comparedTo);
		
		if (m.find()){
			if (m.end() - m.start() == comparedTo.length()){
				//System.out.println("Using literal int for while loop");
				usingLiteral = true;
				literal = Integer.parseInt(comparedTo);
			} else {
				usingLiteral = false;
				//System.out.println("Using variable for while loops");
			}
		} else {
			usingLiteral = false;
			//System.out.println("Using variable for while loops");
		}
	}
}



public class BareBonesVM {
	
	private static Pattern statementFragment = Pattern.compile("[\\w<>=\\!]+");
	
	boolean running = false;
	boolean debugMode = false;
	private int pc = 0;
	
	private Vector<Vector<String>> program        = new Vector<Vector<String>>();
	private Stack<HashMap<String, Integer>> stack = new Stack<HashMap<String, Integer>>();
	private Stack<WhileFrame> whileStack          = new Stack<WhileFrame>();
	
	
	//Resets stack space and program counter but not the loaded program
	public void resetState() {
		running = false;
		pc = 0;
		
		stack = new Stack<HashMap<String, Integer>>();
		whileStack          = new Stack<WhileFrame>();
	}
	
	public void load(String filePath){
		try {
			BufferedReader file = new BufferedReader(new FileReader(filePath));
			
			String line;
			while((line = file.readLine()) != null){
				Vector<String> statement = new Vector<String>();
				
				Matcher m = statementFragment.matcher(line);
				
				while (m.find())
					statement.add(line.substring(m.start(), m.end()));
				
				if (!statement.isEmpty())
					program.add(statement);
				else
					System.out.println("Null statement found in: " + line);
			}
			
			file.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load file (Not found)");
			return;
		} catch (IOException e) {
			System.out.println("Failed to load file (Error during reading)");
			return;
		}
		
		//TODO primitive parse
		
	}
	
	public void execute(){
		running = true;
		genScope();
		
		while (running){
			execStatement();
		}
		
		if(debugMode)
			System.out.println("BareBones VM has stopped.");
	}
	
	public void genScope(){
		stack.push(new HashMap<String, Integer>());
	}
	
	private void execStatement(){
		
		Vector<String> statement = program.get(pc);
		String op = statement.get(0);
		
		String operand;
		
		//System.out.println(pc);
		
		//TODO enable cross scope variable lookup
		//TODO better error messages
		
		switch(op){
			case "clear":
				operand = statement.get(1);
				stack.peek().put(operand, 0);
				break;
				
			case "incr":
				operand = statement.get(1);
				if (stack.peek().containsKey(operand))
					stack.peek().put(operand, stack.peek().get(operand) + 1);
				else
					unknownVariable(operand);
				break;
				
			case "decr":
				operand = statement.get(1);
				if (stack.peek().containsKey(operand))
					stack.peek().put(operand, stack.peek().get(operand) - 1);
				else
					unknownVariable(operand);
				break;
			
			case "while":
				operand = statement.get(1);
				String comparison = statement.get(2);
				int end = findEndForWhile();
				//System.out.println("End found for while statement: " + (pc+1) + "->" + end);
				WhileFrame frame = new WhileFrame(pc, end, comparison, operand, statement.get(3));
				
				if (evaluateWhile(frame))
					whileStack.push(frame);
				else
					pc = frame.end;
				
				
				break;
				
			case "end":
				if (evaluateWhile(whileStack.peek()))
					pc = whileStack.peek().start;
				else
					whileStack.pop();
				
				break;
				
			case "output":
				System.out.println(stack.peek().get(statement.get(1)));
				break;
		}
		
		
		pc+=1;
		if (pc >= program.size()){
			running = false;
		}
	}
	
	private void unknownVariable(String name){
		System.out.println("Error: unknown variable " + name + " on line " + (pc + 1));
		running = false;
	}
	
	private int findEndForWhile(){
		int testpc = pc+1;
		int embedded = 0; //for detecting embedded while loops and finding the correct end statement
		
		while (testpc < program.size()){
			switch(program.get(testpc++).get(0)){
				case "end":
					if (embedded == 0)
						return testpc;
					else
						embedded--;
					break;
					
				case "while":
					embedded++;
					break;
			}

		}
		
		System.out.println("Error: Could not find end for while statement on line " + (pc+1));
		running = false;
		
		return 0;
	}
	
	private boolean evaluateWhile(WhileFrame frame){
		
		int var = stack.peek().get(frame.variable);
		int com = frame.usingLiteral ? frame.literal : stack.peek().get(frame.comparedTo);
		
		switch(frame.comparison){
			case "not":
				return var != com;
			case "!=":
				return var != com;
			case "==":
				return var == com;
			case ">":
				return var > com;
			case "<":
				return var < com;
			case ">=":
				return var >= com;
			case "<=":
				return var <= com;
			default:
				System.out.println("Error: Unknown comparison " + frame.comparison + " on line " + (frame.start+1));
		}
		return false;
	}
}
