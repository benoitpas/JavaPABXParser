package pabx.read;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.jmock.Expectations;
import org.jmock.Mockery;

import pabx.read.Parser.Command;
import pabx.read.ReadFile;
import pabx.read.Tokenizer;
import pabx.read.Tokenizer.Token;
import junit.framework.Assert;
import junit.framework.TestCase;

public class ReadEventStreamTest extends TestCase {

	public void readTest() throws UnsupportedEncodingException {
		byte[] bytes = "Corbata".getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		ReadFile rf = new ReadFile();

		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Integer> subscriber = context.mock(Input.class);
		rf.subscribe(subscriber);

		context.checking(new Expectations() {
			{
				one(subscriber).receive((int) 'C');
				one(subscriber).receive((int) 'o');
				one(subscriber).receive((int) 'r');
				one(subscriber).receive((int) 'b');
				one(subscriber).receive((int) 'a');
				one(subscriber).receive((int) 't');
				one(subscriber).receive((int) 'a');
			}
		});
		rf.read(br);
		
		context.assertIsSatisfied();
	}
	
	public void testTokenizerGoodCR() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Token> subscriber = context.mock(Input.class);
		
		Tokenizer t = new Tokenizer(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(Tokenizer.cr);
			}
		});
		
		t.receive((int) '<');
		t.receive((int) 'C');
		t.receive((int) 'R');
		t.receive((int) '>');
		
		context.assertIsSatisfied();
		
	}
	
	public void testTokenizerGoodLF() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Token> subscriber = context.mock(Input.class);
		
		Tokenizer t = new Tokenizer(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(Tokenizer.lf);
			}
		});
		
		t.receive((int) '<');
		t.receive((int) 'L');
		t.receive((int) 'F');
		t.receive((int) '>');
		
		context.assertIsSatisfied();
		
	}
	
	public void testTokenizerString() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Token> subscriber = context.mock(Input.class);
		
		Tokenizer t = new Tokenizer(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(Tokenizer.lf);
				one(subscriber).receive(Tokenizer.idle);
				one(subscriber).receive(Tokenizer.attempt);
				one(subscriber).receive(Tokenizer.connect);
				for(int i=0;i<10;i++) {
					one(subscriber).receive(Tokenizer.digits[i]);
				}
			}
		});
		
		t.receive((int) '<');
		t.receive((int) 'L');
		t.receive((int) 'F');
		t.receive((int) '>');
		t.receive((int) 'I');
		t.receive((int) 'A');
		t.receive((int) 'C');
		for(char c='0';c<='9';c++) {
			t.receive((int) c);
		}
		context.assertIsSatisfied();
		
	}
	
	public void testParserIdle() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Command> subscriber = context.mock(Input.class);
		Parser p = new Parser(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(with(equal(new Parser.Idle(3141))));
			}
		});
		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.idle);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.lf);
		context.assertIsSatisfied();
	}
	
	public void testParserAttempt() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Command> subscriber = context.mock(Input.class);
		Parser p = new Parser(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(with(equal(new Parser.Attempt(3141,2713))));
			}
		});
		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.attempt);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[2]);
		p.receive(Tokenizer.digits[7]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.lf);
		context.assertIsSatisfied();
	}
	
	public void testParserConnect() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Command> subscriber = context.mock(Input.class);
		Parser p = new Parser(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(with(equal(new Parser.Connect(3141,2713))));
			}
		});
		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.connect);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[2]);
		p.receive(Tokenizer.digits[7]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.lf);
		context.assertIsSatisfied();
	}
	
	public void testParserDial() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Command> subscriber = context.mock(Input.class);
		Parser p = new Parser(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(with(equal(new Parser.Dial(3141,"98"))));
			}
		});
		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.dial);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[9]);
		p.receive(Tokenizer.lf);
		
		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.dial);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[8]);
		p.receive(Tokenizer.lf);

		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.dial);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.sharp);
		p.receive(Tokenizer.lf);

		context.assertIsSatisfied();
	}
	
	public void testParserDialOnlySharp() {
		Mockery context = new Mockery();
		@SuppressWarnings("unchecked")
		final Input<Command> subscriber = context.mock(Input.class);
		Parser p = new Parser(subscriber);
		context.checking(new Expectations() {
			{
				one(subscriber).receive(with(equal(new Parser.Dial(3141,""))));
			}
		});
		p.receive(Tokenizer.cr);
		p.receive(Tokenizer.dial);
		p.receive(Tokenizer.digits[3]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.digits[4]);
		p.receive(Tokenizer.digits[1]);
		p.receive(Tokenizer.sharp);
		p.receive(Tokenizer.lf);
		
		context.assertIsSatisfied();
	}
	
	/**
	 * The parser parses strings coming from a simple PABX (private phone exchange) with 4 digits extensions. 
	 * 
	 * The possible strings are:
	 * <CR>Axxxxyyyy<LF> -> xxxx calls yyyy, yyyy phone rings
	 * <CR>Cxxxxyyyy<LF> -> yyyy picks up the phone
	 * <CR>Ixxxx<LF> -> xxxx hangs up
	 * 
	 * During a call it is possible to dial string that ends in '#'
	 * For example the following sequence will generate an event 'Dial xxxx 314'
	 * <CR>Dxxxx3<LF>
	 * <CR>Dxxxx1<LF>
	 * <CR>Dxxxx4<LF>
	 * <CR>Dxxxx#<LF>
	 * 
	 * 'Dial' events from multiple calls can be interleaved, hence the 'map' in Parser to keep track
	 * of the digits that have already been transmitted
	 */
	public void testParserAndTokenizer() {
		final StringBuilder output = new StringBuilder();
		Parser p = new Parser(new Input<Command>() {

			@Override
			public void receive(Command c) {
				if (output.length()>0) {
					output.append(' ');
				}
				output.append(c.toString());				
			}
			
		});
		Tokenizer t = new Tokenizer(p);

		String input = "<CR>A12349876<LF><CR>C12349876<LF><CR>D12341<LF><CR>D31140<LF><CR>D12342<LF><CR>D1234#<LF><CR>I1234<LF><CR>D3114#<LF>";
		for (int c : input.toCharArray()) {
			t.receive(c);
		}
		
		Assert.assertEquals("A12349876 C12349876 D123421 I1234 D31140", output.toString());				
	}
}
