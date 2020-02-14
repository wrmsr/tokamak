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
    : expression (AS? identifier)?  #expressionSelectItem
    | '*'                           #allSelectItem
    ;

relation
    : left=relation JOIN right=relation (ON criteria=booleanExpression)  #joinRelation
    | aliasedRelation                                                    #singleRelation
    ;

aliasedRelation
    : baseRelation (AS? identifier)?
    ;

baseRelation
    : qualifiedName   #tableNameBaseRelation
    | '(' select ')'  #subqueryBaseRelation
    ;

expression
    : booleanExpression
    ;

booleanExpression
    : valueExpression predicate[$valueExpression.ctx]?                         #predicateBooleanExpression
    | NOT booleanExpression                                                    #logicalNotBooleanExpression
    | left=booleanExpression operator=booleanOperator right=booleanExpression  #logicalBinaryBooleanExpression
    ;

// workaround for https://github.com/antlr/antlr4/issues/780
predicate[ParserRuleContext value]
    : comparisonOperator right=valueExpression                      #comparisonOperatorPredicate
    | NOT? BETWEEN lower=valueExpression AND upper=valueExpression  #betweenPredicate
    | NOT? IN '(' expression (',' expression)* ')'                  #inPredicate
    | IS NOT? NULL                                                  #isNullPredicate
    ;

valueExpression
    : primaryExpression                                                             #primaryExpressionValueExpression
    | operator=arithmeticUnaryOperator valueExpression                              #arithmeticUnaryValueExpression
    | left=valueExpression operator=arithmeticBinaryOperator right=valueExpression  #arithmeticBinaryValueExpression
    | left=valueExpression '||' right=valueExpression                               #concatenationValueExpression
    ;

primaryExpression
    : NULL                                                #nullPrimaryExpression
    | literal                                             #literalPrimaryExpression
    | parameter                                           #parameterPrimaryExpression
    | identifier '(' (expression (',' expression)*)? ')'  #functionCallPrimaryExpression
    | qualifiedName                                       #qualifiedNamePrimaryExpression
    | '(' expression ')'                                  #parenthesizedPrimaryExpression
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

booleanOperator
    : AND | OR
    ;

comparisonOperator
    : '=' | '!=' | '<>' | '<' | '<=' | '>' | '>='
    ;

arithmeticUnaryOperator
    : '-' | '+'
    ;

arithmeticBinaryOperator
    : '*' | '/' | '%' | '+' | '-'
    ;

AND: 'AND';
AS: 'AS';
BETWEEN: 'BETWEEN';
FALSE: 'FALSE';
FROM: 'FROM';
ILIKE: 'ILIKE';
IN: 'IN';
IS: 'IS';
JOIN: 'JOIN';
LIKE: 'LIKE';
NOT: 'NOT';
NULL: 'NULL';
OR: 'OR';
ON: 'ON';
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