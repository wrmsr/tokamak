grammar Sql;

tokens {
    DELIMITER
}

statement
    : query EOF
    ;

query
    : COMMIT
    | SELECT selectItem (',' selectItem)*
      (FROM relation (',' relation)*)?
      (WHERE where=booleanExpression)?
    ;

selectItem
    : expression
    ;

relation
    : qualifiedName
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

identifier
    : IDENTIFIER             #unquotedIdentifier
    | QUOTED_IDENTIFIER      #quotedIdentifier
    ;

expression
    : booleanExpression
    | qualifiedName
    ;

booleanExpression
    : NOT booleanExpression
    | primaryExpression
    ;

primaryExpression
    : NULL
    | INTEGER_VALUE
    ;

COMMIT: 'COMMIT';
FROM: 'FROM';
NOT: 'NOT';
NULL: 'NULL';
SELECT: 'SELECT';
WHERE: 'WHERE';

IDENTIFIER
    : (LETTER | '_') (LETTER | DIGIT | '_' | '@' | ':')*
    ;

QUOTED_IDENTIFIER
    : '"' ( ~'"' | '""' )* '"'
    ;

INTEGER_VALUE
    : DIGIT+
    ;

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