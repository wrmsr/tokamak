grammar Sql;

tokens {
    DELIMITER
}

singleStatement
    : statement EOF
    ;

statement
    : select
    ;

select
    : SELECT selectItem (',' selectItem)*
      (FROM relation (',' relation)*)?
      (WHERE where=expression)?
    ;

selectItem
    : expression (AS? identifier)?  #selectExpression
    | ASTERISK                      #selectAll
    ;

relation
    : qualifiedName   #tableName
    | '(' select ')'  #subqueryRelation
    ;

expression
    : NOT expression                                      #notExpression
    | qualifiedName                                       #qualifiedNameExpression
    | literal                                             #literalExpression
    | qualifiedName '(' expression (',' expression)* ')'  #functionCallExpression
    ;

literal
    : NULL           #nullLiteral
    | STRING_VALUE   #stringLiteral
    | INTEGER_VALUE  #integerLiteral
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

identifier
    : IDENTIFIER         #unquotedIdentifier
    | QUOTED_IDENTIFIER  #quotedIdentifier
    ;

AS: 'AS';
FROM: 'FROM';
NOT: 'NOT';
NULL: 'NULL';
SELECT: 'SELECT';
WHERE: 'WHERE';

ASTERISK: '*';

IDENTIFIER
    : (LETTER | '_') (LETTER | DIGIT | '_' | '@' | ':')*
    ;

QUOTED_IDENTIFIER
    : '"' (~'"' | '""')* '"'
    ;

STRING_VALUE
    : '\'' (~'\'' | '\'\'')* '\''
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