package arrr;
import java.io.IOException;

import arrr.Environment;
import arrr.AST.*;

public class Interpreter {
	public static void main(String[] args) {
		
		// Create a new reader with the provided filename, exactly 1 argumnet must be passed when executing interpreter
		Reader reader;
		if(args.length == 1) {
			try {
				reader = new Reader(args[0]);
			} 
			catch (IOException e) {
				System.out.println(e);
				return;
			}
		} 
		else {
			System.out.println("Ye be speakin' gibberish lad! Grant me a file and one file only!");
			return;
		}

		// Create the evaluator for the AST
		Evaluator eval = new Evaluator();

		try {
			// Build the AST for the program
			Program p = reader.read();

			// Build the program environment by evaluating the program
			eval.buildProgramEnvironment(p);

			// Execute the program (call the entry point function "vessel")
			eval.executeProgram();

			return;

		} catch (NullPointerException e) {
			System.out.println("Error:" + e.getMessage());
			return;
		}
	}
}