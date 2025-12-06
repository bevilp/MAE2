package stone.mae2.logic.expression;

public enum TokenType {
    IDENTIFIER, // minecraft:stone, mine*:*logs
    NUMBER, // 10, 100
    GT, // >
    LT, // <
    GTE, // >=
    LTE, // <=
    EQ, // ==
    NEQ, // !=
    AND, // &&
    OR, // ||
    XOR, // ^
    NOT, // !
    LPAREN, // (
    RPAREN, // )
    EOF
}
