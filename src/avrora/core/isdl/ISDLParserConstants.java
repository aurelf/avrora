/* Generated By:JavaCC: Do not edit this line. ISDLParserConstants.java */
package avrora.core.isdl;

public interface ISDLParserConstants {

    int EOF = 0;
    int SINGLE_LINE_COMMENT = 9;
    int FORMAL_COMMENT = 10;
    int MULTI_LINE_COMMENT = 11;
    int INTEGER_LITERAL = 13;
    int DECIMAL_LITERAL = 14;
    int HEX_LITERAL = 15;
    int BIN_LITERAL = 16;
    int OCTAL_LITERAL = 17;
    int CHARACTER_LITERAL = 18;
    int STRING_LITERAL = 19;
    int INSTRUCTION = 20;
    int ARCHITECTURE = 21;
    int FORMAT = 22;
    int OPERAND = 23;
    int WHERE = 24;
    int REGISTER = 25;
    int IMMEDIATE = 26;
    int ADDRESS = 27;
    int ENCODING = 28;
    int EXECUTE = 29;
    int LOCAL = 30;
    int IF = 31;
    int ELSE = 32;
    int AND = 33;
    int OR = 34;
    int XOR = 35;
    int SUBROUTINE = 36;
    int INLINE = 37;
    int EXTERNAL = 38;
    int RETURN = 39;
    int BOOLEAN_LITERAL = 40;
    int CYCLES = 41;
    int LBRACKET = 42;
    int RBRACKET = 43;
    int EQUALS = 44;
    int COMMA = 45;
    int LPAREN = 46;
    int RPAREN = 47;
    int SEMI = 48;
    int SHIFTLEFT = 49;
    int SHIFTRIGHT = 50;
    int ADD = 51;
    int SUB = 52;
    int MUL = 53;
    int DIV = 54;
    int B_AND = 55;
    int B_OR = 56;
    int B_XOR = 57;
    int NOT = 58;
    int B_COMP = 59;
    int EQUAL = 60;
    int NOTEQUAL = 61;
    int LESS = 62;
    int LESSEQ = 63;
    int GREATER = 64;
    int GREATEREQ = 65;
    int DOLLAR = 66;
    int IDENTIFIER = 67;
    int LETTER = 68;
    int DIGIT = 69;

    int DEFAULT = 0;
    int IN_SINGLE_LINE_COMMENT = 1;
    int IN_FORMAL_COMMENT = 2;
    int IN_MULTI_LINE_COMMENT = 3;

    String[] tokenImage = {
        "<EOF>",
        "\" \"",
        "\"\\t\"",
        "\"\\n\"",
        "\"\\r\"",
        "\"\\f\"",
        "\"//\"",
        "<token of kind 7>",
        "\"/*\"",
        "<SINGLE_LINE_COMMENT>",
        "\"*/\"",
        "\"*/\"",
        "<token of kind 12>",
        "<INTEGER_LITERAL>",
        "<DECIMAL_LITERAL>",
        "<HEX_LITERAL>",
        "<BIN_LITERAL>",
        "<OCTAL_LITERAL>",
        "<CHARACTER_LITERAL>",
        "<STRING_LITERAL>",
        "\"instruction\"",
        "\"architecture\"",
        "\"format\"",
        "\"operand\"",
        "\"where\"",
        "\"register\"",
        "\"immediate\"",
        "\"address\"",
        "\"encoding\"",
        "\"execute\"",
        "\"local\"",
        "\"if\"",
        "\"else\"",
        "\"and\"",
        "\"or\"",
        "\"xor\"",
        "\"subroutine\"",
        "\"inline\"",
        "\"external\"",
        "\"return\"",
        "<BOOLEAN_LITERAL>",
        "\"cycles\"",
        "\"{\"",
        "\"}\"",
        "\"=\"",
        "\",\"",
        "\"(\"",
        "\")\"",
        "\";\"",
        "\"<<\"",
        "\">>\"",
        "\"+\"",
        "\"-\"",
        "\"*\"",
        "\"/\"",
        "\"&\"",
        "\"|\"",
        "\"^\"",
        "\"!\"",
        "\"~\"",
        "\"==\"",
        "\"!=\"",
        "\"<\"",
        "\"<=\"",
        "\">\"",
        "\">=\"",
        "\"$\"",
        "<IDENTIFIER>",
        "<LETTER>",
        "<DIGIT>",
        "\":\"",
        "\"[\"",
        "\"]\"",
    };

}
