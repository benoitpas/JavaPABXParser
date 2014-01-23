Java PABX Parser
================

Simple test program that parse 'Command' strings coming from a pseudo PABX (private phone exchange).

* GenerateEventStream.java contain a program that generates a file with random PABX commands to test the parser 
 (the generation follows some rules that can be checked to validate that the parser is correctly implemented)
* the class ReadEventStream exercise the classes Tokenizer/Parser that form the parser 
  and checks that the stream is properly formed
* ReadEventStreamTest contains the unit tests for the classes Tokenizer/Parser

Next step is to implement the same in Scala using the integrated parser.

Note: This is not meant to be efficient, I wanted to organise the code so that it follows a structure where 'char' are exchanged. A more efficient implementation would pass 'String' to the tokenizer and not each char individually.
