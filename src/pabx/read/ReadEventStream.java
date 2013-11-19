package pabx.read;

import pabx.GenerateEventStream.Primes;
import pabx.read.Parser.Command;
import pabx.read.Parser.Dial;

public class ReadEventStream {
	
	/** 
	 * 
	 * Verify that the stream is correct (i,e. 'Dialed' numbers are prime)
	 * 
	 * @param argv
	 */
	public static void main(String[] argv){
		
		final Primes primes = new Primes();
		ReadFile rf = new ReadFile();
		Parser p = new Parser(new Input<Command>() {

			@Override
			public void receive(Command c) {
				if (c instanceof Dial) {

					Dial d = (Dial) c;
					if (d.getDigits().length() > 0) {
						Integer digits = null;
						digits = Integer.parseInt(d.getDigits());
						assert (primes.isPrime(digits));
					}
				}
				System.out.println(c.toString());				
			}
		});
		
		Tokenizer t = new Tokenizer(p);		
		rf.subscribe(t);
		String name = argv.length == 0  ? "a.txt" : argv[0];
		rf.read(name);
	}
}
