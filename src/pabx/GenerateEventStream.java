package pabx;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Generate random PABX events to test the parser
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
 * 
 * All dialed number are primes so that it is easy to check that the all the digits are properly 
 * retrieved after the parsing
 * 
 * @author bpasquereau
 *
 */
public class GenerateEventStream {

	public static class Primes {
		static int defaultNumber = 10000;
		List<Integer> primes = new ArrayList<Integer>();
		
		public Primes() {
			this(defaultNumber);
		}

		public Primes(int nb) {
			boolean sieve[] = new boolean[nb];
			for (int i = 2; i <= nb; i++) {
				for (int j = 2 * i; j <= nb; j = j + i) {
					sieve[j - 1] = true;
				}
			}
			for (int i = 0; i < nb; i++) {
				if (!sieve[i]) {
					primes.add(i + 1);
				}
			}
		}

		int getRandomPrime() {
			int i = (int) (primes.size()*Math.random());
			return primes.get(i);			
		}
		
		
		public boolean isPrime(Integer v) {
			assert (v <= defaultNumber);
			return primes.contains(v);
		}
	}
	
	public static class Writer implements Runnable {

		boolean finished = false;
		private BlockingQueue<String> q = new ArrayBlockingQueue<String>(16);
		private BufferedWriter w;
		
		Writer(BufferedWriter w) {
			this.w =w; 
		}

		void output(String s) {
			try {
				q.put(s);
			} catch (InterruptedException e) {
				finished = true;
			}
		}
		@Override
		public void run() {
			do {
				String item;
				try {
					item = q.poll(1, TimeUnit.SECONDS);
					if (item != null) {
						w.write(item);
					}
				} catch (IOException|InterruptedException e) {
					finished = true;
					e.printStackTrace();
				}
				
			} while (!finished || q.size()>0);
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public static class Phone implements Runnable {
		Writer w;
		int index;
		int[] numbers;
		Primes primes;
		public Phone(Writer w, int i, int[] numbers, Primes primes) {
			this.w = w;
			index = i;
			this.numbers = numbers;
			this.primes = primes;
		}
		
		int getOtherNumber() {
			int i = index;
			while (i==index) {
				i=(int) (Math.random()*numbers.length);
			}
			return numbers[i];
		}
		
		List<String> getEvent(int eventType) {
			List<String> ret = new LinkedList<String>();
			switch(eventType) {
			case 0:
				ret.add("<CR>I"+numbers[index]+"<LF>");
				break;
			case 1:
				ret.add("<CR>A"+numbers[index]+getOtherNumber()+"<LF>");
				break;
			case 2:
				ret.add("<CR>C"+numbers[index]+getOtherNumber()+"<LF>");
				break;
			case 3:
				String digits = new Integer(primes.getRandomPrime()).toString();
				for(int k=0;k<digits.length();k++) {
					ret.add("<CR>D"+numbers[index]+digits.charAt(k)+"<LF>");
				}
				ret.add("<CR>D"+numbers[index]+"#<LF>");
				break;
			}
			return ret;
		}

		@Override
		public void run() {
			int nbEvents = (int) (Math.random() * 20 + 5);
			for (int i = 0; i < nbEvents; i++) {
				int eventType = (int) (Math.random() * 4);
				for (String event : getEvent(eventType)) {
					try {
						Thread.sleep((long) (Math.random() * 10));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					w.output(event);
				}
			}
		}
		
	}
	
	public static void main(String[] argv) throws InterruptedException, IOException {
		int nbPhones = 10;
		int[] numbers = new int[nbPhones];
		for(int i=0;i<numbers.length;i++) {
			numbers[i] = (int) (Math.random()*9000+1000);
		}
		
		FileWriter fw = new FileWriter("a.txt");
		ExecutorService pool = Executors.newFixedThreadPool(10);
		Primes primes = new Primes();
		BufferedWriter bw = new BufferedWriter(fw);
		Writer w = new Writer(bw);
		Thread wThread = new Thread(w);
		wThread.start();
		for (int i = 0; i < 10; i++) {
			Runnable command = new Phone(w, i, numbers, primes);
			pool.execute(command);
		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
		w.finished = true;
		wThread.join();

	}
}
