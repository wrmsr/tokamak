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
      (FROM aliasedRelation (',' aliasedRelation)*)?
      (WHERE where=expression)?
    ;

selectItem
    : expression (AS? identifier)?  #selectExpression
    | '*'                           #selectAll
    ;

aliasedRelation
    : relation (AS? identifier)?
    ;

relation
    : qualifiedName   #tableName
    | '(' select ')'  #subqueryRelation
    ;

expression
    : NOT expression                                      #notExpression
    | qualifiedName                                       #qualifiedNameExpression
    | literal                                             #literalExpression
    | identifier '(' (expression (',' expression)*)? ')'  #functionCallExpression
    | parameter                                           #parameterExpression
    | expression comparisonOperator expression            #comparisonExpression
    ;

parameter
    : '$' IDENTIFIER  #nameParameter
    | '$' INTEGER     #numberParameter
    ;

literal
    : NULL                  #nullLiteral
    | TRIPLE_QUOTED_STRING  #tripleQuotedStringLiteral
    | SINGLE_QUOTED_STRING  #singleQuotedStringLiteral
    | NUMBER                #numberLiteral
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

identifier
    : IDENTIFIER         #unquotedIdentifier
    | QUOTED_IDENTIFIER  #quotedIdentifier
    ;

comparisonOperator
    : EQ | NEQ | LT | LTE | GT | GTE
    ;

AS: 'AS';
FALSE: 'FALSE';
FROM: 'FROM';
NOT: 'NOT';
NULL: 'NULL';
SELECT: 'SELECT';
TRUE: 'TRUE';
WHERE: 'WHERE';

IDENTIFIER
    : (LETTER | '_') (LETTER | DIGIT | '_' | '@' | ':')*
    ;

QUOTED_IDENTIFIER
    : '"' (~'"' | '""')* '"'
    ;

SINGLE_QUOTED_STRING
    : '\'' (~'\'' | '\\\'' | '\'\'')* '\''
    ;

TRIPLE_QUOTED_STRING
    : '\'\'\'' (~'\'' | '\\\'' | ('\'' ~'\'') | ('\'\'' ~'\''))* '\'\'\''
    ;

EQ: '=';
NE: '!=' | '<>';
LT: '<';
LE: '<=';
GT: '>';
GE: '>=';

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