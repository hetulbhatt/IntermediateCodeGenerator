
/**
* IntermediateCodeGenerator program converts an infix expression to postfix,
* constructs a binary expression tree out of it, 
* and makes a list of quadruples 
* where each quadruple denotes a 3-address code generated from the binary expression tree.
*
* @author  Hetul Bhatt
* @version 1.0
* @since   2020-09-22 
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

class Quadruple {
	char operator;
	String operand1;
	String operand2;
	String result;

	Quadruple(char operator, String operand1, String operand2, String result) {
		this.operator = operator;
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.result = result;
	}

	@Override
	public String toString() {
		return (this.operator+"\t\t"+this.operand1+"\t\t"+this.operand2+"\t\t"+this.result+"\n");
	}
}

class Node {
	char symbol;
	Node left;
	Node right;
	int result;

	Node(char symbol) {
		this.symbol = symbol;
	}
}

public class IntermediateCodeGenerator {
	ArrayList<Character> infix;
	Stack<Character> operatorStack;
	List<Character> postfix;
	Map<Character,Integer> precedence;
	Node binaryExpressionTree;
	List<Quadruple> quadruples;
	
	IntermediateCodeGenerator(String infix) {
		this.infix = new ArrayList<>(infix.length()+2);
		for(char c : infix.toCharArray()) {
			if(!Character.isWhitespace(c)) {
				this.infix.add(c);
			}
		}
		this.infix.add(')'); // end parenthesis
		this.operatorStack = new Stack<>();
		this.operatorStack.push('('); // start parenthesis
		this.postfix = new ArrayList<>(infix.length());
		this.precedence = new HashMap<>() {{
			put('(', 1);
			put(')', 1);
			put('+', 2);
			put('-', 2);
			put('*', 3);
			put('/', 3);
			put('^', 4);
		}};
		this.quadruples = new ArrayList<>((this.postfix.size()-1)/2);

		try {
			this.convertToPostfix();
		} catch(ExpresssionParsingException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		this.constructBinaryExpressionTree();
		this.constructQuadrupleTable(this.binaryExpressionTree);
	}
	
	public final void convertToPostfix() throws ExpresssionParsingException {
		for(char c : this.infix) {
			if(Character.isLetterOrDigit(c)) {
				this.postfix.add(c);
			} else if(c == ')') {
				while(this.operatorStack.peek() > c) {
					this.postfix.add(this.operatorStack.pop());
				}
				this.operatorStack.pop();
			} else if(c == '(') {
				this.operatorStack.push(c);
			} else if(c == '^') {
				if(this.precedence.get(c) >= this.precedence.get(this.operatorStack.peek())) {
					this.operatorStack.push(c);
				} else {
					while(this.precedence.get(c) < this.precedence.get(this.operatorStack.peek())) {
						this.postfix.add(this.operatorStack.pop());
					}
					this.operatorStack.push(c);
				}
			} else if(this.precedence.containsKey(c)) {
				if(this.precedence.get(c) > this.precedence.get(this.operatorStack.peek())) {
					this.operatorStack.push(c);
				} else {
					while(this.precedence.get(c) <= this.precedence.get(this.operatorStack.peek())) {
						this.postfix.add(this.operatorStack.pop());
					}
					this.operatorStack.push(c);
				}
			} else {
				throw new ExpresssionParsingException("Character "+c+" is neither an operand nor an operator");
			}
		}
	}

	public final void flush(List<Character> stack, String message) {
		System.out.print(message);
		for(Character c : stack) {
			System.out.print(c);
		}
		System.out.println();
	}

	public final void constructBinaryExpressionTree() {
		Stack<Node> intermediateNodes = new Stack<>();
		int result = 1;
		for(char c : this.postfix) {
			if(this.precedence.containsKey(c)) {
				Node temp = new Node(c);
				temp.right = intermediateNodes.pop();
				temp.left = intermediateNodes.pop();
				temp.result = result++;
				intermediateNodes.push(temp);
			} else {
				intermediateNodes.push(new Node(c));
			}
		}
		this.binaryExpressionTree = intermediateNodes.pop();
	}

	public final void constructQuadrupleTable(Node node) {
		if(node.left != null) {
			this.constructQuadrupleTable(node.left);
		}
		if(node.result != 0) {
			String operand1, operand2;
			if(node.left.result != 0) {
				operand1 = "rslt"+node.left.result;
			} else {
				operand1 = Character.toString(node.left.symbol);
			}
			if(node.right.result != 0) {
				operand2 = "rslt"+node.right.result;
			} else {
				operand2 = Character.toString(node.right.symbol);
			}
			Quadruple quadruple = new Quadruple(node.symbol, operand1, operand2, "rslt"+node.result);
			this.quadruples.add(quadruple);
		}
		if(node.right != null) {
			this.constructQuadrupleTable(node.right);
		}
	}

	public static void main(String[] args) {
		System.out.print("\nEnter infix expression with character-long operands: ");
		Scanner sc = new Scanner(System.in);
		String infix = sc.nextLine();
		sc.close();
		IntermediateCodeGenerator icg = new IntermediateCodeGenerator(infix);
	
		icg.flush(icg.postfix, "\nReverse polish notation: ");
		System.out.println("\nQuadruple table (ordered as per the inorder traversal of the binary expression tree):\n");
		System.out.println("Operator\tOperand1\tOperand2\tResult\n");
		for(Quadruple q : icg.quadruples) {
			System.out.println(q);
		}
	}
}

class ExpresssionParsingException extends Exception {
	public ExpresssionParsingException(String errorMessage) {
        super(errorMessage);
    }
}