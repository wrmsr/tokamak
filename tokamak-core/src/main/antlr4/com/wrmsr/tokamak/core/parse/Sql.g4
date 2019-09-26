grammar Sql;

tokens {
    DELIMITER
}

singleStatement
    : statement EOF
    ;

standaloneExpression
    : expression EOF
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
    | variable                                               #variableExpression
    ;

variable
    : DOLLAR IDENTIFIER  #nameVariable
    | DOLLAR INTEGER     #numberVariable
    ;

literal
    : NULL    #nullLiteral
    | STRING  #stringLiteral
    | NUMBER  #numberLiteral
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
DOLLAR: '$';

IDENTIFIER
    : (LETTER | '_') (LETTER | DIGIT | '_' | '@' | ':')*
    ;

QUOTED_IDENTIFIER
    : '"' (~'"' | '""')* '"'
    ;

STRING
    : '\'' (~'\'' | '\'\'')* '\''
    ;

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [A-Z]
    ;

NUMBER
   : '-'? INTEGER '.' [0-9]+ EXPONENT?
   | '-'? INTEGER EXPONENT
   | '-'? INTEGER
   ;

INTEGER
   : '0'
   | [1-9] [0-9]*
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