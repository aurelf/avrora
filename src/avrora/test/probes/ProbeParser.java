/* Generated By:JavaCC: Do not edit this line. ProbeParser.java */
package avrora.test.probes;
import avrora.core.isdl.ast.*;
import avrora.core.isdl.*;
import java.util.List;
import java.util.LinkedList;

public class ProbeParser implements ProbeParserConstants {

/* Begin GRAMMAR */
  final public ProbeTest ProbeTest() throws ParseException {
                          ProbeTest pt = new ProbeTest();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PROBE:
      case EVENT:
      case WATCH:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      Declaration(pt);
    }
    Main(pt);
    Result(pt);
      {if (true) return pt;}
    throw new Error("Missing return statement in function");
  }

  final public void Main(ProbeTest pt) throws ParseException {
                           List l;
    jj_consume_token(MAIN);
    jj_consume_token(LBRACKET);
    l = Body(pt);
    jj_consume_token(RBRACKET);
      pt.addMainCode(l);
  }

  final public void Result(ProbeTest pt) throws ParseException {
    jj_consume_token(RESULT);
    jj_consume_token(LBRACKET);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INTEGER_LITERAL:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      Event(pt);
      jj_consume_token(SEMI);
    }
    jj_consume_token(RBRACKET);
  }

  final public void Event(ProbeTest pt) throws ParseException {
                            Token t, n;
    t = jj_consume_token(INTEGER_LITERAL);
    label_3:
    while (true) {
      n = jj_consume_token(IDENTIFIER);
                                              pt.addResultEvent(t, n);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENTIFIER:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_3;
      }
    }
  }

  final public void Declaration(ProbeTest pt) throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PROBE:
      ProbeDeclaration(pt);
      break;
    case WATCH:
      WatchDeclaration(pt);
      break;
    case EVENT:
      EventDeclaration(pt);
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public List Body(ProbeTest pt) throws ParseException {
                            List l = new LinkedList();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INSERT:
      case REMOVE:
      case ADVANCE:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_4;
      }
      Statement(pt, l);
      jj_consume_token(SEMI);
    }
      {if (true) return l;}
    throw new Error("Missing return statement in function");
  }

  final public void Statement(ProbeTest pt, List l) throws ParseException {
                                         Token n, v;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INSERT:
      jj_consume_token(INSERT);
      n = jj_consume_token(IDENTIFIER);
      v = jj_consume_token(INTEGER_LITERAL);
                                                      pt.addInsert(l, n, v);
      break;
    case REMOVE:
      jj_consume_token(REMOVE);
      n = jj_consume_token(IDENTIFIER);
      v = jj_consume_token(INTEGER_LITERAL);
                                                      pt.addRemove(l, n, v);
      break;
    case ADVANCE:
      jj_consume_token(ADVANCE);
      v = jj_consume_token(INTEGER_LITERAL);
                                      pt.addAdvance(l, v);
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void ProbeDeclaration(ProbeTest pt) throws ParseException {
                                       Token n; List b, a;
    jj_consume_token(PROBE);
    n = jj_consume_token(IDENTIFIER);
    jj_consume_token(LBRACKET);
    b = Body(pt);
    jj_consume_token(PIPE);
    a = Body(pt);
    jj_consume_token(RBRACKET);
      pt.newProbe(n, b, a);
  }

  final public void WatchDeclaration(ProbeTest pt) throws ParseException {
                                       Token n; List b1, a1, b2, a2;
    jj_consume_token(WATCH);
    n = jj_consume_token(IDENTIFIER);
    jj_consume_token(LBRACKET);
    b1 = Body(pt);
    jj_consume_token(PIPE);
    a1 = Body(pt);
    jj_consume_token(PIPE);
    b2 = Body(pt);
    jj_consume_token(PIPE);
    a2 = Body(pt);
    jj_consume_token(RBRACKET);
      pt.newWatch(n, b1, a1, b2, a2);
  }

  final public void EventDeclaration(ProbeTest pt) throws ParseException {
                                       Token n; List b;
    jj_consume_token(EVENT);
    n = jj_consume_token(IDENTIFIER);
    jj_consume_token(LBRACKET);
    b = Body(pt);
    jj_consume_token(RBRACKET);
      pt.newEvent(n, b);
  }

  public ProbeParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[6];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x1c000,0x200,0x4000000,0x1c000,0x1c0000,0x1c0000,};
   }

  public ProbeParser(java.io.InputStream stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ProbeParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  public ProbeParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ProbeParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  public ProbeParser(ProbeParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  public void ReInit(ProbeParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[29];
    for (int i = 0; i < 29; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 6; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 29; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
