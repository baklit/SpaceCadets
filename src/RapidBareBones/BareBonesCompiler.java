package RapidBareBones;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Token {
	static HashMap<String, Byte> tokenMap = new HashMap<String, Byte>();
	static HashMap<String, Byte> comparisonMap = new HashMap<String, Byte>();
	
	static
	{
		tokenMap.put(" ",       (byte) 0x00);//Null operation, will halt VM (Error: Null operation) if this is executed
		
		tokenMap.put("clear",   (byte) 0x01);//clear operation, followed by two bytes 
											 //(stack pointer, variable pointer)
		
		tokenMap.put("incr",    (byte) 0x02);//incr operation, followed by two bytes 
											 //(stack pointer, variable pointer)
		
		tokenMap.put("decr",    (byte) 0x03);//decr operation, followed by two bytes 
											 //(stack pointer, variable pointer)
		
		tokenMap.put("while",   (byte) 0x04);//default while operation, will halt VM (Error: undefined while),
											 //only defined to assist with further processing
		
		tokenMap.put("whileim", (byte) 0x05);//while immediate-mode operation, followed by five bytes 
											 //(stack pointer, variable pointer, literal value, comparison method, end pointer)
		
		tokenMap.put("whilefm", (byte) 0x06);//while fetch-mode operation, followed by six bytes 
											 //(stack pointer, variable pointer, stack pointer, variable pointer, comparison method, end pointer)
		
		tokenMap.put("end",     (byte) 0x07);//end operation, followed by one byte (while pointer)  
		
		tokenMap.put("output",  (byte) 0x08);//output operation, followed by two bytes
											 //(stack pointer, variable pointer)
		
		//TODO: change end/while pointer to allow > +/- 128 byte jumps 
	}
	
	static
	{
		comparisonMap.put("not", (byte) 0x00);
		comparisonMap.put("!=",  (byte) 0x00);
		comparisonMap.put("==",  (byte) 0x01);
		comparisonMap.put(">",   (byte) 0x02);
		comparisonMap.put("<",   (byte) 0x03);
		comparisonMap.put(">=",  (byte) 0x04);
		comparisonMap.put("<=",  (byte) 0x05);
	}
}

public class BareBonesCompiler {
	private static final Pattern statementFragment = Pattern.compile("[\\w<>=\\!]+");
	private static final Pattern literalInt = Pattern.compile("\\d+");
	
	private String code = "";
	private Vector<Byte> compiledCode = new Vector<Byte>();
	private Stack<HashMap<String, Integer>> stack = new Stack<HashMap<String, Integer>>();
	private Stack<Byte> whileStack = new Stack<Byte>();
	
	public boolean debugMode = false;
	
	//Loads the code into memory ready to compile
	//Strips newline characters
	public void load(String filePath){
		code = "";
		
		try {
			BufferedReader file = new BufferedReader(new FileReader(filePath));
			
			String line;
			while((line = file.readLine()) != null){
				code += line;
			}
			
			file.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load file (Not found)");
			return;
		} catch (IOException e) {
			System.out.println("Failed to load file (Error during reading)");
			return;
		}		
	}
	
	//Compile the loaded code
	//Combined lexer, parser, AST generator and bytecode generator due to simplicity of barebones
	//(A true AST isn't necessary for this implementation)
	public Vector<Byte> compile() {
		createScope();
		
		if (code.equals("")){
			System.out.println("No code loaded to compile!");
			System.exit(1);
		}
		
		String[] statements = code.split(";");
		
		for (String statement: statements)
			processStatement(statement);
		
		System.out.println("\n\nCompiling done! Compiled to " + compiledCode.size() + " bytes.\n");
		
		return compiledCode;
	}
	
	private void processStatement(String statement) {

		Matcher m = statementFragment.matcher(statement);
		
		if (m.find()) {
			String operation = statement.substring(m.start(), m.end());
			
			if(debugMode)
				System.out.println("Attempting to add opcode " + Token.tokenMap.get(operation) + " (" + operation + ")");
			
			switch (operation) {
			
				case "clear":
					compiledCode.add(Token.tokenMap.get("clear"));
					
					if (m.find()) {
						String variable = statement.substring(m.start(), m.end());
						int[] varLocation = findVariable(variable);
						
						if (varLocation[0] != -1) {
							compiledCode.add((byte) varLocation[0]);
							compiledCode.add((byte) varLocation[1]);
						} else {
							compiledCode.add((byte) (stack.size() - 1));
							compiledCode.add((byte) declareVariable(variable));
						}
						
						if (m.find()) {
							System.out.println("\nError: Expected ; near " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
					} else {
						System.out.println("\nError: Expected variable for clear");
						System.out.println("Found in statement: " + statement);
					}
					
					break;
					
				case "incr":
					compiledCode.add(Token.tokenMap.get("incr"));
					
					if (m.find()) {
						String variable = statement.substring(m.start(), m.end());
						int[] varLocation = findVariable(variable);
						
						if (varLocation[0] != -1) {
							compiledCode.add((byte) varLocation[0]);
							compiledCode.add((byte) varLocation[1]);
						} else {
							System.out.println("\nError: Undeclared variable " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
						if (m.find()) {
							System.out.println("\nError: Expected ; near " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
					} else {
						System.out.println("\nError: Expected variable for incr");
						System.out.println("Found in statement: " + statement);
					}
					
					break;
					
				case "decr":
					compiledCode.add(Token.tokenMap.get("decr"));
					
					if (m.find()) {
						String variable = statement.substring(m.start(), m.end());
						int[] varLocation = findVariable(variable);
						
						if (varLocation[0] != -1) {
							compiledCode.add((byte) varLocation[0]);
							compiledCode.add((byte) varLocation[1]);
						} else {
							System.out.println("\nError: Undeclared variable " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
						if (m.find()) {
							System.out.println("\nError: Expected ; near " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
					} else {
						System.out.println("\nError: Expected variable for decr");
						System.out.println("Found in statement: " + statement);
					}
					
					break;
					
				case "while":
					
					int[] varLocation = {-1, -1};
					Byte condition = -1;
					
					if (m.find()) {
						String variable = statement.substring(m.start(), m.end());
						varLocation = findVariable(variable);
						
						if (varLocation[0] == -1) {
							System.out.println("\nError: Undeclared variable " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
					} else {
						System.out.println("\nError: Expected variable for while");
						System.out.println("Found in statement: " + statement);
					}
					
					
					if (m.find()) {
						String comparison = statement.substring(m.start(), m.end());
						
						if (Token.comparisonMap.get(comparison) != null) {
							condition = Token.comparisonMap.get(comparison);
						} else {
							System.out.println("\nError: Unknown comparison " + comparison);
							System.out.println("Found in statement: " + statement);
						}
						
					} else {
						condition = Token.comparisonMap.get("==");
					}
					
					if (m.find()) {
						String variable = statement.substring(m.start(), m.end());
						
						Matcher isLiteral = literalInt.matcher(variable);
						boolean useFetchMode = false;
						
						if (isLiteral.find()) {
							if (isLiteral.end() - isLiteral.start() == variable.length()) {
								
								whileStack.push((byte) compiledCode.size()); //To change end pointer when end op is reach
								
								compiledCode.add(Token.tokenMap.get("whileim"));
								compiledCode.add((byte) varLocation[0]);
								compiledCode.add((byte) varLocation[1]);
								compiledCode.add((byte) Integer.parseInt(variable));
						
							} else {
								useFetchMode = true;
							}
						} else {
							useFetchMode = true;
						}
						
						if (useFetchMode) {
							
							whileStack.push((byte) compiledCode.size()); //To change end pointer when end op is reach
							
							compiledCode.add(Token.tokenMap.get("whilefm"));
							compiledCode.add((byte) varLocation[0]);
							compiledCode.add((byte) varLocation[1]);
							
							if (m.find()) {
								String variable2 = statement.substring(m.start(), m.end());
								int[] varLocation2 = findVariable(variable2);
								
								if (varLocation[0] != -1) {
									compiledCode.add((byte) varLocation2[0]);
									compiledCode.add((byte) varLocation2[1]);
								} else {
									System.out.println("\nError: Undeclared variable " + variable);
									System.out.println("Found in statement: " + statement);
								}
								
							} else {
								System.out.println("\nError: Expected variable for while");
								System.out.println("Found in statement: " + statement);
							}
						}
					}

					compiledCode.add(condition);
					compiledCode.add((byte) 0x01); //placeholder end pointer; 
					
					createScope();
					
					break;
					
				case "end":
					compiledCode.add(Token.tokenMap.get("end"));
					Byte whilePointer = whileStack.pop();
					Byte endPointer = (byte) (compiledCode.size() + 1); //jump to next op
					
					compiledCode.add((byte) whilePointer);
					
					if (compiledCode.get(whilePointer) == Token.tokenMap.get("whileim")) {
						compiledCode.set(whilePointer + 5, endPointer);
					} else if (compiledCode.get(whilePointer) == Token.tokenMap.get("whilefm")) {
						compiledCode.set(whilePointer + 6, endPointer);
					} else {
						System.out.println("\n Error: End tried to link to non while operation (" + compiledCode.get(whilePointer) + ")");
						System.out.println("Found in statement: " + statement);
					}
					
					deleteScope();
					
					break;
				
				case "output":
					compiledCode.add(Token.tokenMap.get("output"));
					
					if (m.find()) {
						String variable = statement.substring(m.start(), m.end());
						varLocation = findVariable(variable);
						
						if (varLocation[0] != -1) {
							compiledCode.add((byte) varLocation[0]);
							compiledCode.add((byte) varLocation[1]);
						} else {
							System.out.println("\nError: Undeclared variable " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
						if (m.find()) {
							System.out.println("\nError: Expected ; near " + variable);
							System.out.println("Found in statement: " + statement);
						}
						
					} else {
						System.out.println("\nError: Expected variable for output");
						System.out.println("Found in statement: " + statement);
					}
					
					break;
					
				default:
					System.out.println("\nError: Unknown operation: " + operation);
					System.out.println("Found in statement: " + statement);
			}	
		}
	}
	
	//Returns (stack pointer, variable pointer) pair
	private int[] findVariable(String name) {
		int stackPointer = stack.size() - 1;

		while (stackPointer >= 0) {
			HashMap<String, Integer> variables = stack.get(stackPointer);
			if (variables.containsKey(name)) {
				int[] values =  {stackPointer, (int) variables.get(name)};
				return values;
			}
			
			stackPointer -= 1;
		}
		
		int[] notFound = {-1, -1};
		return notFound;
	}

	//Returns variable pointer inside current stack (stack pointer of stack.size() - 1)
	private int declareVariable(String name) {
		if (stack.peek().containsKey(name)) {
			return stack.peek().get(name);
		} else {
			stack.peek().put(name, stack.peek().size());
			return stack.peek().size() - 1;
		}
	}
	
	private void createScope() {
		stack.push(new HashMap<String, Integer>());
	}
	
	private void deleteScope() {
		stack.pop();
	}
}
