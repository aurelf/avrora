/* Generated By:JavaCC: Do not edit this line. ISDLParserTokenManager.java */
package avrora.core.isdl;

public class ISDLParserTokenManager implements ISDLParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x3f00000L) != 0L)
         {
            jjmatchedKind = 33;
            return 27;
         }
         if ((active0 & 0x140L) != 0L)
            return 2;
         return -1;
      case 1:
         if ((active0 & 0x100L) != 0L)
            return 0;
         if ((active0 & 0x3f00000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 1;
            return 27;
         }
         return -1;
      case 2:
         if ((active0 & 0x3f00000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 2;
            return 27;
         }
         return -1;
      case 3:
         if ((active0 & 0x3f00000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 3;
            return 27;
         }
         return -1;
      case 4:
         if ((active0 & 0x2f00000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 4;
            return 27;
         }
         if ((active0 & 0x1000000L) != 0L)
            return 27;
         return -1;
      case 5:
         if ((active0 & 0x2b00000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 5;
            return 27;
         }
         if ((active0 & 0x400000L) != 0L)
            return 27;
         return -1;
      case 6:
         if ((active0 & 0x800000L) != 0L)
            return 27;
         if ((active0 & 0x2300000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 6;
            return 27;
         }
         return -1;
      case 7:
         if ((active0 & 0x300000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 7;
            return 27;
         }
         if ((active0 & 0x2000000L) != 0L)
            return 27;
         return -1;
      case 8:
         if ((active0 & 0x300000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 8;
            return 27;
         }
         return -1;
      case 9:
         if ((active0 & 0x300000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 9;
            return 27;
         }
         return -1;
      case 10:
         if ((active0 & 0x200000L) != 0L)
         {
            jjmatchedKind = 33;
            jjmatchedPos = 10;
            return 27;
         }
         if ((active0 & 0x100000L) != 0L)
            return 27;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 40:
         return jjStopAtPos(0, 30);
      case 41:
         return jjStopAtPos(0, 31);
      case 44:
         return jjStopAtPos(0, 29);
      case 47:
         return jjMoveStringLiteralDfa1_0(0x140L);
      case 58:
         return jjStopAtPos(0, 36);
      case 59:
         return jjStopAtPos(0, 32);
      case 61:
         return jjStopAtPos(0, 28);
      case 91:
         return jjStopAtPos(0, 37);
      case 93:
         return jjStopAtPos(0, 38);
      case 97:
         return jjMoveStringLiteralDfa1_0(0x200000L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x400000L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x800000L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x2000000L);
      case 119:
         return jjMoveStringLiteralDfa1_0(0x1000000L);
      case 123:
         return jjStopAtPos(0, 26);
      case 125:
         return jjStopAtPos(0, 27);
      default :
         return jjMoveNfa_0(3, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(1, 8, 0);
         break;
      case 47:
         if ((active0 & 0x40L) != 0L)
            return jjStopAtPos(1, 6);
         break;
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000L);
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000000L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x100000L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x400000L);
      case 112:
         return jjMoveStringLiteralDfa2_0(active0, 0x800000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa3_0(active0, 0x200000L);
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x1800000L);
      case 103:
         return jjMoveStringLiteralDfa3_0(active0, 0x2000000L);
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0x400000L);
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 104:
         return jjMoveStringLiteralDfa4_0(active0, 0x200000L);
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x2000000L);
      case 109:
         return jjMoveStringLiteralDfa4_0(active0, 0x400000L);
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x1800000L);
      case 116:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa5_0(active0, 0xc00000L);
      case 101:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(4, 24, 27);
         break;
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x200000L);
      case 114:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000L);
      case 115:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000000L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 110:
         return jjMoveStringLiteralDfa6_0(active0, 0x800000L);
      case 116:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(5, 22, 27);
         return jjMoveStringLiteralDfa6_0(active0, 0x2200000L);
      case 117:
         return jjMoveStringLiteralDfa6_0(active0, 0x100000L);
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa7_0(active0, 0x100000L);
      case 100:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(6, 23, 27);
         break;
      case 101:
         return jjMoveStringLiteralDfa7_0(active0, 0x2200000L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa8_0(active0, 0x200000L);
      case 114:
         if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(7, 25, 27);
         break;
      case 116:
         return jjMoveStringLiteralDfa8_0(active0, 0x100000L);
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(6, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0);
      return 8;
   }
   switch(curChar)
   {
      case 105:
         return jjMoveStringLiteralDfa9_0(active0, 0x100000L);
      case 116:
         return jjMoveStringLiteralDfa9_0(active0, 0x200000L);
      default :
         break;
   }
   return jjStartNfa_0(7, active0);
}
private final int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(7, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(8, active0);
      return 9;
   }
   switch(curChar)
   {
      case 111:
         return jjMoveStringLiteralDfa10_0(active0, 0x100000L);
      case 117:
         return jjMoveStringLiteralDfa10_0(active0, 0x200000L);
      default :
         break;
   }
   return jjStartNfa_0(8, active0);
}
private final int jjMoveStringLiteralDfa10_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(8, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(9, active0);
      return 10;
   }
   switch(curChar)
   {
      case 110:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(10, 20, 27);
         break;
      case 114:
         return jjMoveStringLiteralDfa11_0(active0, 0x200000L);
      default :
         break;
   }
   return jjStartNfa_0(9, active0);
}
private final int jjMoveStringLiteralDfa11_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(9, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(10, active0);
      return 11;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(11, 21, 27);
         break;
      default :
         break;
   }
   return jjStartNfa_0(10, active0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 33;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
                  if ((0x3fe000000000000L & l) != 0L)
                  {
                     if (kind > 13)
                        kind = 13;
                     jjCheckNAdd(5);
                  }
                  else if (curChar == 48)
                  {
                     if (kind > 13)
                        kind = 13;
                     jjCheckNAddStates(0, 2);
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(3, 5);
                  else if (curChar == 39)
                     jjAddStates(6, 7);
                  else if (curChar == 36)
                     jjCheckNAdd(7);
                  else if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 0:
                  if (curChar == 42)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if ((0xffff7fffffffffffL & l) != 0L && kind > 7)
                     kind = 7;
                  break;
               case 2:
                  if (curChar == 42)
                     jjstateSet[jjnewStateCnt++] = 0;
                  break;
               case 4:
                  if ((0x3fe000000000000L & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjCheckNAdd(5);
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjCheckNAdd(5);
                  break;
               case 6:
                  if (curChar == 36)
                     jjCheckNAdd(7);
                  break;
               case 7:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjCheckNAdd(7);
                  break;
               case 8:
                  if (curChar == 39)
                     jjAddStates(6, 7);
                  break;
               case 9:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAdd(10);
                  break;
               case 10:
                  if (curChar == 39 && kind > 18)
                     kind = 18;
                  break;
               case 12:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAdd(10);
                  break;
               case 13:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(14, 10);
                  break;
               case 14:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(10);
                  break;
               case 15:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 16;
                  break;
               case 16:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(14);
                  break;
               case 17:
                  if (curChar == 34)
                     jjCheckNAddStates(3, 5);
                  break;
               case 18:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 20:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 21:
                  if (curChar == 34 && kind > 19)
                     kind = 19;
                  break;
               case 22:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(8, 11);
                  break;
               case 23:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 24:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 25;
                  break;
               case 25:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(23);
                  break;
               case 27:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 28:
                  if (curChar != 48)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjCheckNAddStates(0, 2);
                  break;
               case 31:
                  if ((0x3000000000000L & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjstateSet[jjnewStateCnt++] = 31;
                  break;
               case 32:
                  if ((0xff000000000000L & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjCheckNAdd(32);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
               case 27:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAdd(27);
                  break;
               case 1:
                  if (kind > 7)
                     kind = 7;
                  break;
               case 7:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 13)
                     kind = 13;
                  jjCheckNAdd(7);
                  break;
               case 9:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAdd(10);
                  break;
               case 11:
                  if (curChar == 92)
                     jjAddStates(12, 14);
                  break;
               case 12:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAdd(10);
                  break;
               case 18:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 19:
                  if (curChar == 92)
                     jjAddStates(15, 17);
                  break;
               case 20:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 29:
                  if ((0x100000001000000L & l) != 0L)
                     jjCheckNAdd(7);
                  break;
               case 30:
                  if ((0x400000004L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 31;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 7)
                     kind = 7;
                  break;
               case 9:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 18:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(3, 5);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 33 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_3()
{
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_3(0x800L);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_3(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 47:
         if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(1, 11);
         break;
      default :
         return 2;
   }
   return 2;
}
private final int jjMoveStringLiteralDfa0_1()
{
   return jjMoveNfa_1(0, 0);
}
private final int jjMoveNfa_1(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x2400L & l) != 0L)
                  {
                     if (kind > 9)
                        kind = 9;
                  }
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if (curChar == 10 && kind > 9)
                     kind = 9;
                  break;
               case 2:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_2(0x400L);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_2(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 47:
         if ((active0 & 0x400L) != 0L)
            return jjStopAtPos(1, 10);
         break;
      default :
         return 2;
   }
   return 2;
}
static final int[] jjnextStates = {
   29, 30, 32, 18, 19, 21, 9, 11, 18, 19, 23, 21, 12, 13, 15, 20, 
   22, 24, 
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, 
"\151\156\163\164\162\165\143\164\151\157\156", "\141\162\143\150\151\164\145\143\164\165\162\145", 
"\146\157\162\155\141\164", "\157\160\145\162\141\156\144", "\167\150\145\162\145", 
"\162\145\147\151\163\164\145\162", "\173", "\175", "\75", "\54", "\50", "\51", "\73", null, null, null, "\72", 
"\133", "\135", };
public static final String[] lexStateNames = {
   "DEFAULT", 
   "IN_SINGLE_LINE_COMMENT", 
   "IN_FORMAL_COMMENT", 
   "IN_MULTI_LINE_COMMENT", 
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, 1, 2, 3, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0x73fffc2001L, 
};
static final long[] jjtoSkip = {
   0xe3eL, 
};
static final long[] jjtoSpecial = {
   0xe00L, 
};
static final long[] jjtoMore = {
   0x11c0L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[33];
private final int[] jjstateSet = new int[66];
StringBuffer image;
int jjimageLen;
int lengthOfMatch;
protected char curChar;
public ISDLParserTokenManager(SimpleCharStream stream)
{
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public ISDLParserTokenManager(SimpleCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 33; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 4 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }
   image = null;
   jjimageLen = 0;

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         try { input_stream.backup(0);
            while (curChar <= 32 && (0x100003600L & (1L << curChar)) != 0L)
               curChar = input_stream.BeginToken();
         }
         catch (java.io.IOException e1) { continue EOFLoop; }
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 12)
         {
            jjmatchedKind = 12;
         }
         break;
       case 2:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_2();
         if (jjmatchedPos == 0 && jjmatchedKind > 12)
         {
            jjmatchedKind = 12;
         }
         break;
       case 3:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_3();
         if (jjmatchedPos == 0 && jjmatchedKind > 12)
         {
            jjmatchedKind = 12;
         }
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken.specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == null)
                 specialToken = matchedToken;
              else
              {
                 matchedToken.specialToken = specialToken;
                 specialToken = (specialToken.next = matchedToken);
              }
              SkipLexicalActions(matchedToken);
           }
           else 
              SkipLexicalActions(null);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
        MoreLexicalActions();
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
  }
}

void SkipLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      default :
         break;
   }
}
void MoreLexicalActions()
{
   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);
   switch(jjmatchedKind)
   {
      case 7 :
         if (image == null)
              image = new StringBuffer(new String(input_stream.GetSuffix(jjimageLen)));
         else
            image.append(new String(input_stream.GetSuffix(jjimageLen)));
         jjimageLen = 0;
                   input_stream.backup(1);
         break;
      default : 
         break;
   }
}
}
