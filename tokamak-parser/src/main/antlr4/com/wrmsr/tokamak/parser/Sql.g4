grammar Sql;

tokens {
    DELIMITER
}

statement
    : query EOF
    ;

query
    : COMMIT
    | SELECT expression
    ;

expression
    : DIGIT+
    ;

COMMIT: 'commit';
SELECT: 'select';

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [A-Z]
    ;

SIMPLE_COMMENT
    : '--' ~[\r\n]* '\r'? '\n'? -> channel(2)
    ;

BRACKETED_COMMENT
    : '/*' .*? '*/' -> channel(2)
    ;

WS
    : [ \r\n\t]+ -> channel(HIDDEN)
    ;

UNRECOGNIZED
    : .
    ;