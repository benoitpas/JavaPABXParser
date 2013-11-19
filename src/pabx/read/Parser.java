package pabx.read;

import java.util.HashMap;
import java.util.Map;

import pabx.read.Input;
import pabx.read.Tokenizer.Token;

/**
 * Reads a stream of 'Token' and outputs a stream of PABX 'Command'
 * @author bpasquereau
 *
 */
class Parser implements Input<Token> {

	/**
	 * Base state
	 * 
	 */
	abstract class State {
		Command command = null;

		abstract State nextState(Token t);
	}

	class InitialState extends State {
		InitialState() {
		}

		InitialState(Command command) {
			this.command = command;
		}

		@Override
		State nextState(Token t) {
			State ret = null;
			if (t == Tokenizer.cr) {
				ret = new CRState();
			}
			return ret;
		}
	}

	/**
	 * All commands start with <CR>,
	 * This state directs the state machine in the right
	 * branch depending on the command
	 *
	 */
	class CRState extends State {

		@Override
		State nextState(Token t) {
			State ret = new InitialState();
			if (t == Tokenizer.attempt) {
				ret = new AttemptDigitsState();
			} else if (t == Tokenizer.connect) {
				ret = new ConnectDigitsState();
			} else if (t == Tokenizer.idle) {
				ret = new IdleDigitsState();
			} else if (t == Tokenizer.dial) {
				ret = new DialDigitsState();
			}

			return ret;
		}
	}

	/** 
	 * Attempt and Connect states inherit from this class
	 * as they both a 'source' and destination address
	 */
	abstract class TwoAddressState extends State {
		private int totalDigits = 8;
		private int index;
		protected int source = 0;
		protected int destination = 0;

		TwoAddressState(int index) {
			this.index = index;
		}

		TwoAddressState() {
			this(0);
		}

		@Override
		State nextState(Token t) {
			State ret = null;
			if (t instanceof Tokenizer.Digit && index < totalDigits) {
				int digit = ((Tokenizer.Digit) t).d;
				if (index < (totalDigits / 2)) {
					source = source * 10 + digit;
				} else {
					destination = destination * 10 + digit;
				}
				index++;
				ret = this;
			} else if (index == totalDigits && t == Tokenizer.lf) {
				ret = new InitialState(getCommand());
			}
			return ret;
		}

		abstract Command getCommand();

	}

	/**
	 * Attempt command
	 * <CR>A12349865<LF>
	 *
	 */
	class AttemptDigitsState extends TwoAddressState {
		@Override
		Command getCommand() {
			return new Attempt(source, destination);
		}
	}

	/**
	 * Connect command
	 * <CR>C12349865<LF>
	 */
	class ConnectDigitsState extends TwoAddressState {
		@Override
		Command getCommand() {
			return new Connect(source, destination);
		}
	}

	/**
	 * Idle command
	 * <CR>I1234<LF>
	 *
	 */
	class IdleDigitsState extends State {
		private int totalDigits = 4;
		private int index;
		protected int source = 0;

		IdleDigitsState(int index) {
			this.index = index;
		}

		IdleDigitsState() {
			this(0);
		}

		@Override
		State nextState(Token t) {
			State ret = null;
			if (t instanceof Tokenizer.Digit && index < totalDigits) {
				int digit = ((Tokenizer.Digit) t).d;
				source = source * 10 + digit;
				index++;
				ret = this;
			} else if (index == totalDigits && t == Tokenizer.lf) {
				ret = new InitialState(new Idle(source));
			}
			return ret;
		}
	}
	
	/**
	 * Dial command
	 * <CR>D12349<LF>
	 * 
	 * Note: it is probably possible to share code with IdleDigitsState
	 */
	Map<Integer,String> dialedNumbers = new HashMap<Integer,String>();

	class DialDigitsState extends State {

		private int totalDigits = 4;
		private int index;
		protected int source = 0;
		private String allDigits = null;
		private String newDigit = null;

		DialDigitsState(int index) {
			this.index = index;
		}

		DialDigitsState() {
			this(0);
		}

		@Override
		State nextState(Token t) {
			State ret = null;
			if (t instanceof Tokenizer.Digit) {
				int digit = ((Tokenizer.Digit) t).d;
				if (index < totalDigits) {
					source = source * 10 + digit;
					index++;
					ret = this;
				} else if (index == totalDigits) {
					index++;
					newDigit = new Integer(digit).toString();
					ret = this;
				}
			} else if (index == totalDigits && t == Tokenizer.sharp) {
				// All digits have been received
				if (dialedNumbers.containsKey(source)) {
					allDigits = dialedNumbers.get(source);
					dialedNumbers.remove(source);
				} else {
					allDigits = "";
				}
				index++;
				ret = this;
			} else if (index == (totalDigits + 1) && t == Tokenizer.lf) {
				if (newDigit != null) {
					String digits = newDigit;
					if (dialedNumbers.containsKey(source)) {
						digits = dialedNumbers.get(source) + digits;
					}
					dialedNumbers.put(source, digits);
					ret = new InitialState();
				} else if (allDigits != null) {
					ret = new InitialState(new Dial(source, allDigits));
				}
			}
			return ret;
		}

	}

	/**
	 * Class used for the output stream
	 * Output commands
	 */
	static class Command {
	};

	static abstract class SourceOnly extends Command {
		Integer source;

		SourceOnly(Integer source) {
			this.source = source;
		}

		@Override
		public String toString() {
			return String.format("%4d", source);
		}

		@Override
		public boolean equals(Object o) {
			boolean ret = false;
			if (o != null && o.getClass() == getClass() && source != null) {
				ret = source.equals(((SourceOnly) o).source);
			}
			return ret;
		}
	}

	static abstract class SourceDestination extends SourceOnly {
		Integer destination;

		SourceDestination(Integer source, Integer destination) {
			super(source);
			this.destination = destination;
		}

		@Override
		public String toString() {
			return String.format("%4d%4d", source, destination);
		}

		@Override
		public boolean equals(Object o) {
			boolean ret = false;
			if (o != null && o.getClass() == getClass() && source != null
					&& destination != null) {
				ret = source.equals(((SourceOnly) o).source)
						&& destination.equals(((SourceDestination) o).destination);
			}
			return ret;
		}
	}

	static class Idle extends SourceOnly {
		Idle(Integer source) {
			super(source);
		}

		@Override
		public String toString() {
			return "I" + super.toString();
		}
	}

	static class Attempt extends SourceDestination {
		Attempt(Integer source, Integer destination) {
			super(source, destination);
		}

		@Override
		public String toString() {
			return "A" + super.toString();
		}
	}

	static class Connect extends SourceDestination {
		Connect(Integer source, Integer destination) {
			super(source, destination);
		}

		@Override
		public String toString() {
			return "C" + super.toString();
		}
	}
	
	static class Dial extends SourceOnly {
		private String digits;

		Dial(Integer source, String digits) {
			super(source);
			this.digits = digits;
		}

		@Override
		public String toString() {
			return "D" + super.toString() + digits;
		}
		
		public String getDigits() {
			return digits;
		}
	}

	Input<Command> output;

	Parser(Input<Command> output) {
		this.output = output;
	}

	State state = new InitialState();

	@Override
	public void receive(Token t) {
		if (t != null) {
			State newState = state.nextState(t);
			if (newState == null) {
				System.out.println("Unexpected Token "+t.toString());
				state = new InitialState();
			} else {
				state = newState;
			}
			if (state.command != null) {
				output.receive(state.command);
			}
		}
	}

}