package arrr;
import java.io.IOException;

import arrr.Env;
import arrr.Value;
import arrr.AST.*;

/**
 * This main class implements the Read-Eval-Print-Loop of the interpreter with
 * the help of Reader, Evaluator, and Printer classes. 
 * 
 * @author hridesh
 *
 */
public class Interpreter {
	public static void main(String[] args) {
		
		Reader reader;
		if(args.length == 0) {
			reader = new Reader();
		} else if(args.length == 1) {
			try {
				reader = new Reader(args[0]);
			} catch (IOException e) {
				System.out.println(e);
				return;
			}
		} else {
			System.out.println("Invalid invocation. Ye be speakin' gibberish lad!");
			return;
		}

		Evaluator eval = new Evaluator(reader);
		Printer printer = new Printer();
		Value vesselVal;
		REPL: while (true) { // Read-Eval-Print-Loop (also known as REPL)
			Program p = null;
			try {
				p = reader.read();
				if(p._e == null) continue REPL;
				if(p._e instanceof AST.UnitExp) {
					
					// End of file. Time to call the main function!
					vesselVal = eval.callVessel();
					printer.print(vesselVal);
					
					return;
				}
				Value val = eval.valueOf(p);
				printer.print(val);
			} catch (Env.LookupException e) {
				printer.print(e);
			} catch (IOException e) {
				System.out.println("Error reading input:" + e.getMessage());
			} catch (NullPointerException e) {
				System.out.println("Error:" + e.getMessage());
			}
		}
	}
}
