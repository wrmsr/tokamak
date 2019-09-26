grammar Type;


type
    : NAME ('<' argOrKwarg (',' argOrKwarg)* '>')?
    ;

argOrKwarg
    : typeOrInt           #arg
    | NAME '=' typeOrInt  #kwarg
    ;

typeOrInt
    : type
    | INT
    ;

NAME
    : [a-zA-Z_@][0-9a-zA-Z_]+
    ;

INT
    : '0'
    | [1-9][0-9]*
    ;

WS
    : [ \r\n\t]+ -> channel(HIDDEN)
    ;

UNRECOGNIZED
    : .
    ;
