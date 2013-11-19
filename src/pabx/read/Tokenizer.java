package pabx.read;

import pabx.read.Input;

/**
 * Convert 'char' stream to 'Token' stream
 */
class Tokenizer implements Input<Integer> {
	/**
	 * States definitions for the tokenizer
	 */

	/**
	 * basic abstract state with logic to go to next state depending on the
	 * input char
	 * 
	 */
	abstract class State {
		Token token = null;

		State nextState(Integer i) {
			State ret = this;
			token = null;
			if (i != null) {
				char c = (char) i.intValue();
				State ns = nextState(c);
				if (ns == null) {
					System.err.println("Unexpected character " + c);
				} else {
					ret = ns;
				}
			}
			return ret;
		}

		abstract State nextState(char c);
	};

	/**
	 * First step, if something goes wrong the state machine goes back in this state
	 *
	 */
	class InitialState extends State {
		InitialState() {
		}

		InitialState(Token token) {
			this.token = token;
		}

		@Override
		public State nextState(char c) {
			State ret = null;
			if (c == '<') {
				ret = new LTState();
			} else if ('0' <= c && c <= '9') {
				token = digits[c - '0'];
			} else if (c == 'A') {
				token = attempt;
			} else if (c == 'C') {
				token = connect;
			} else if (c == 'I') {
				token = idle;
			} else if (c == 'D') {
				token = dial;
			} else if (c == '#') {
				token = sharp;
			}
			if (ret == null && token != null) {
				ret = new InitialState(token);
			}
			return ret;
		}

	}

	/**
	 * state to choose either <CR> or <CL> branch
	 */
	class LTState extends State {
		@Override
		State nextState(char c) {
			State ret = null;
			switch (c) {
			case 'C':
				ret = new CRCState();
				break;
			case 'L':
				ret = new LFLState();
				break;
			default:
			}
			return ret;
		}
	}

	/**
	 * <CR> branch
	 */
	class CRCState extends State {
		@Override
		State nextState(char c) {
			return c == 'R' ? new CRRState() : null;
		}
	}

	/**
	 * <CR> branch last state, the token can be created
	 */
	class CRRState extends State {
		@Override
		State nextState(char c) {
			State ret = null;
			if (c == '>') {
				ret = new InitialState();
				ret.token = cr;
			}
			return ret;
		}
	}

	/**
	 * <LF> branch
	 */
	class LFLState extends State {
		@Override
		State nextState(char c) {
			return c == 'F' ? new LFFState() : null;
		}
	}

	/**
	 * <LF> branch last state, the token can be created
	 */
	class LFFState extends State {
		@Override
		State nextState(char c) {
			State ret = null;
			if (c == '>') {
				ret = new InitialState();
				ret.token = lf;
			}
			return ret;
		}
	}

	Input<Token> p;

	/**
	 * Token definitions for output stream
	 * 
	 */
	static class Token {
	}

	static class CR extends Token {
	}

	static CR cr = new CR();

	static class LF extends Token {
	}

	static LF lf = new LF();

	static class Digit extends Token {
		final int d;

		Digit(int d) {
			this.d = d;
		}
	}

	static Digit[] digits = new Digit[10];
	static {
		for (int i = 0; i < digits.length; i++) {
			digits[i] = new Digit(i);
		}
	}

	static class Connect extends Token {
	}

	static Connect connect = new Connect();

	static class Attempt extends Token {
	}

	static Attempt attempt = new Attempt();

	static class Dial extends Token {
	}

	static Dial dial = new Dial();

	static class Idle extends Token {
	}

	static Idle idle = new Idle();

	static class Sharp extends Token {
	}

	static Sharp sharp = new Sharp();

	Tokenizer(Input<Token> p) {
		this.p = p;
	}

	State state = new InitialState();

	@Override
	public void receive(Integer c) {
		state = state.nextState(c);
		if (state.token != null) {
			p.receive(state.token);
		}

	}

}