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
    : NOT expression                                         #notExpression
    | qualifiedName                                          #qualifiedNameExpression
    | literal                                                #literalExpression
    | qualifiedName '(' (expression (',' expression)*)? ')'  #functionCallExpression
    ;

literal
    : NULL           #nullLiteral
    | STRING_VALUE   #stringLiteral
    | NUMBER_VALUE   #numberLiteral
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

identifier
    : IDENTIFIER         #unquotedIdentifier
    | QUOTED_IDENTIFIER  #quotedIdentifier
    ;

AS: 'AS';
FALSE: 'FALSE';
FROM: 'FROM';
NOT: 'NOT';
NULL: 'NULL';
SELECT: 'SELECT';
TRUE: 'TRUE';
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

NUMBER_VALUE
    : NUMBER
    ;

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [A-Z]
    ;

NUMBER
   : '-'? INTEGER '.' [0-9]+ EXPONENT? | '-'? INTEGER EXPONENT | '-'? INTEGER
   ;

fragment INTEGER
   : '0' | [1-9] [0-9]*
   ;

fragment EXPONENT
   : [Ee] [+\-]? INTEGER
   ;

SIMPLE_COMMENT
    : ('--' | '//') ~[\r\n]* '\r'? '\n'? -> channel(2)
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