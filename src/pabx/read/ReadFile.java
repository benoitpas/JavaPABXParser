package pabx.read;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import pabx.read.Input;

/**
 * Read a file and send its content to the subscribers
 * char by char (from an efficiency point of view that would 
 * probably be better to send line by line)
 * 
 * @author bpasquereau
 *
 */
class ReadFile {
	List<Input<Integer>> subscribers = new LinkedList<Input<Integer>>();

	boolean read(String filename) {
		boolean ret = false;
		FileReader fr;
		try {
			fr = new FileReader(filename);
			BufferedReader reader = new BufferedReader(fr);
			read(reader);
			ret = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}

	void read(Reader reader) {
		try {
			Integer i;
			do {
				i = reader.read();
				if (i >= 0) {
					for (Input<Integer> subscriber : subscribers) {
						subscriber.receive(i);
					}
				}
			} while (i > 0);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void subscribe(Input<Integer> subscriber) {
		subscribers.add(subscriber);
	}
}