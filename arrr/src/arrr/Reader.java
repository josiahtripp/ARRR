package arrr;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;

import arrr.AST.Program;
import arrr.parser.ARRRLexer;
import arrr.parser.ARRRParser;


public class Reader {

	private BufferedReader inputStream;   
	private ARRRParser p;

	// Create a new reader for reading in a file
	public Reader(String fileName) throws IOException {
		inputStream = new BufferedReader(new FileReader(fileName));
		ARRRLexer l = new ARRRLexer(new org.antlr.v4.runtime.ANTLRInputStream(inputStream));
		p = new ARRRParser(new org.antlr.v4.runtime.CommonTokenStream(l));
	}

	Program read() {
		Program program = p.program().ast;
		return program;
	}
}