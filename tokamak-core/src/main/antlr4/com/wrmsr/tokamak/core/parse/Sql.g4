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

/*
expression
    : '(' expression ')'                                   #parenthesizedExpression
    | NOT expression                                       #notExpression
    | left=expression booleanOperator right=expression     #booleanExpression
    | left=expression comparisonOperator right=expression  #comparisonExpression
    | valueExpression                                      #valueExpressionLabel
    ;

valueExpression
    : qualifiedName                                        #qualifiedNameExpression
    | literal                                              #literalExpression
    | identifier '(' (expression (',' expression)*)? ')'   #functionCallExpression
    | parameter                                            #parameterExpression
    ;
*/

expression
    : booleanExpression
    ;

booleanExpression
    : valueExpression predicate[$valueExpression.ctx]?                         #predicated
    | NOT booleanExpression                                                    #logicalNot
    | left=booleanExpression operator=booleanOperator right=booleanExpression  #logicalBinary
    ;

// workaround for https://github.com/antlr/antlr4/issues/780
predicate[ParserRuleContext value]
    : comparisonOperator right=valueExpression                      #comparison
    | NOT? BETWEEN lower=valueExpression AND upper=valueExpression  #between
    | NOT? IN '(' expression (',' expression)* ')'                  #inList
    | IS NOT? NULL                                                  #nullPredicate
    ;

valueExpression
    : primaryExpression                                                      #valueExpressionDefault
    | operator=('-' | '+') valueExpression                                   #arithmeticUnary
    | left=valueExpression operator=('*' | '/' | '%') right=valueExpression  #arithmeticBinary
    | left=valueExpression operator=('+' | '-') right=valueExpression        #arithmeticBinary
    | left=valueExpression '||' right=valueExpression                        #concatenation
    ;

primaryExpression
    : NULL                                                   #nullLiteralPrimaryExpression
    | literal                                                #literalPrimaryExpression
    | parameter                                              #parameterPrimaryExpression
    | qualifiedName '(' (expression (',' expression)*)? ')'  #functionCall
    | value=primaryExpression '[' index=valueExpression ']'  #subscript
    | identifier                                             #identifierPrimaryExpression
    | base=primaryExpression '.' fieldName=identifier        #dereference
    | '(' expression ')'                                     #parenthesizedExpression
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

AND: 'AND';
AS: 'AS';
BETWEEN: 'BETWEEN';
FALSE: 'FALSE';
FROM: 'FROM';
ILIKE: 'ILIKE';
IN: 'IN';
IS: 'IS';
LIKE: 'LIKE';
NOT: 'NOT';
NULL: 'NULL';
OR: 'OR';
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