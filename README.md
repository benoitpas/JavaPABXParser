Java PABX Parser
================

Simple test program that parse 'Command' strings coming from a pseudo PABX (private phone exchange).

* GenerateEventStream.java contain a program that generates a file with random PABX commands to test the parser 
 (the generation follows some rules that can be checked to validate that the parser is correctly implemented)
* the class ReadEventStream exercise the classes Tokenizer/Parser that form the parser 
  and checks that the stream is properly formed
* ReadEventStreamTest contains the unit tests for the classes Tokenizer/Parser

Next step is to implement the same in Scala using the integrated parser.
