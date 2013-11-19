package pabx.read;

/**
 * Interface to implement by services which want to subscribe to receive a stream of 'T'
 * 
 */
interface Input<T> {
	void receive(T c);
}
