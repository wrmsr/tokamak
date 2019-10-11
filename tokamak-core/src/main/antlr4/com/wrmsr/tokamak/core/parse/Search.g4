grammar Search;

searchExpression
    : expression EOF
    ;

expression
    : expression '.' chainedExpression  #chainExpression
    | expression bracketSpecifier       #bracketedExpression
    | bracketSpecifier                  #bracketExpression
    | expression COMPARATOR expression  #comparisonExpression
    | expression '&&' expression        #andExpression
    | expression '||' expression        #orExpression
    | identifier                        #identifierExpression
    | '!' expression                    #notExpression
    | '(' expression ')'                #parenExpression
    | wildcard                          #wildcardExpression
    | multiSelectList                   #multiSelectListExpression
    | multiSelectHash                   #multiSelectHashExpression
    | literal                           #literalExpression
    | functionExpression                #functionCallExpression
    | expression '|' expression         #pipeExpression
    | RAW_STRING                        #rawStringExpression
    | currentNode                       #currentNodeExpression
    | parameterNode                     #parameterExpression
    ;

chainedExpression
    : identifier
    | multiSelectList
    | multiSelectHash
    | functionExpression
    | wildcard
    ;

wildcard
    : '*'
    ;

bracketSpecifier
    : '[' SIGNED_INT ']'   #bracketIndex
    | '[' '*' ']'          #bracketStar
    | '[' slice ']'        #bracketSlice
    | '[' ']'              #bracketFlatten
    | '[?' expression ']'  #select
    ;

multiSelectList
    : '[' expression (',' expression)* ']'
    ;

multiSelectHash
    : '{' keyvalExpr (',' keyvalExpr)* '}'
    ;

keyvalExpr
    : identifier ':' expression
    ;

slice
    : start=SIGNED_INT? ':' stop=SIGNED_INT? (':' step=SIGNED_INT?)?
    ;

parameterNode
    : '$' NAME  #nameParameter
    | '$' INT   #numberParameter
    ;

functionExpression
  : NAME '(' functionArg (',' functionArg)* ')'
  | NAME '(' ')'
  ;

functionArg
  : expression
  | expressionType
  ;

currentNode
    : '@'
    ;

expressionType
    : '&' expression
    ;

literal
    : '`' jsonValue '`'
    ;

identifier
    : NAME
    | STRING
    | JSON_CONSTANT
    ;

jsonObject
    : '{' jsonObjectPair (',' jsonObjectPair)* '}'
    | '{' '}'
    ;

jsonObjectPair
    : STRING ':' jsonValue
    ;

jsonArray
    : '[' jsonValue (',' jsonValue)* ']'
    | '[' ']'
    ;

jsonValue
    : STRING                                  #jsonStringValue
    | (REAL_OR_EXPONENT_NUMBER | SIGNED_INT)  #jsonNumberValue
    | jsonObject                              #jsonObjectValue
    | jsonArray                               #jsonArrayValue
    | JSON_CONSTANT                           #jsonConstantValue
    ;

COMPARATOR
    : '<'
    | '<='
    | '=='
    | '>='
    | '>'
    | '!='
    ;

RAW_STRING:
    '\'' (RAW_ESC | ~['\\])* '\''
    ;

fragment RAW_ESC
    : '\\' .
    ;

JSON_CONSTANT
    : 'true'
    | 'false'
    | 'null'
    ;

NAME
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

STRING
    : '"' (ESC | ~ ["\\])* '"'
    ;

fragment ESC
    : '\\' (["\\/bfnrt`] | UNICODE)
    ;

fragment UNICODE
    : 'u' HEX HEX HEX HEX
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

REAL_OR_EXPONENT_NUMBER
    : '-'? INT '.' [0-9] + EXP?
    | '-'? INT EXP
    ;

SIGNED_INT
    : '-'? INT
    ;

INT
    : '0'
    | [1-9] [0-9]*
    ;

fragment EXP
    : [Ee] [+\-]? INT
    ;

WS
    : [ \t\n\r] + -> skip
    ;
