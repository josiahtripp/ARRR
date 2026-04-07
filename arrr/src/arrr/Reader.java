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

	public Reader() {
		inputStream = null;
		p = null;
	}

	public Reader(String fileName) throws IOException {
		inputStream = new BufferedReader(new FileReader(fileName));
		ARRRLexer l = new ARRRLexer(new org.antlr.v4.runtime.ANTLRInputStream(inputStream));
		p = new ARRRParser(new org.antlr.v4.runtime.CommonTokenStream(l));
	}

	
	Program read() throws IOException {
		if(inputStream == null) {
			String programText = readNextProgram();
			ARRRLexer l = new ARRRLexer(new org.antlr.v4.runtime.ANTLRInputStream(programText));
			p = new ARRRParser(new org.antlr.v4.runtime.CommonTokenStream(l));
		}
		return parse();
	}

	Program parse() {
		Program program = p.program().ast;
		return program;
	}
	
	static String readFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(
				new FileReader(fileName))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
		}
	}
	
	private String readNextProgram() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("$ ");
		String programText = br.readLine();
		return runFile(programText);
	}
	
	protected String getProgramDirectory() { return "build"+File.separator+"arrr"+File.separator+"examples"+File.separator; }
	private String runFile(String programText) throws IOException {
		if(programText.startsWith("run ")){
			programText = readFile(getProgramDirectory() + programText.substring(4));
		}
		return programText; 
	}
}
