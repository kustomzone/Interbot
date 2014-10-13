/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.scriptduino;

import java.io.Console;

/**
 * Main class for Scriptduino test program.
 *
 * This program connects to a Scriptduino board and send commands typed on the console to the
 * board.
 * This program uses the scriptduino-config.json configuration file.
 */
public class ScriptduinoMain {

  /**
   * Main method.
   *
   * @param args Commandline arguments.
   */
  public static void main(String[] args) {
    Console console = System.console();
    if (console == null) return;
    console.printf("Scriptduino\n");
    console.printf("Commands:\n");
    console.printf("\tw: forward\n");
    console.printf("\ts: backward\n");
    console.printf("\ta: left\n");
    console.printf("\td: right\n");
    console.printf("\tx: stop\n");
    console.printf("\tq: quit\n");
    console.printf("\n");
    Scriptduino scriptduino = new Scriptduino();
    scriptduino.open();
    boolean stop = false;
    do {
      console.printf("> ");
      console.flush();
      String input = console.readLine();
      if (input.length() > 0) {
        switch (input.charAt(0)) {
          case 'w': scriptduino.moveTurn( 1.0,  0.0); break;
          case 's': scriptduino.moveTurn(-1.0,  0.0); break;
          case 'a': scriptduino.moveTurn( 0.0, -1.0); break;
          case 'd': scriptduino.moveTurn( 0.0,  1.0); break;
          case 'x': scriptduino.stop(); break;
          case 'q': stop = true; break;
        }
      }
    } while (!stop);
    scriptduino.close();
  }
}
