grammar Type;


type
    : NAME ('<' arg_or_kwarg (',' arg_or_kwarg)* '>')?
    ;

arg_or_kwarg
    : type_or_int           #arg
    | NAME '=' type_or_int  #kwarg
    ;

type_or_int
    : type
    | INT
    ;

NAME
    : [a-zA-Z_@][0-9a-zA-Z_]+
    ;

INT
    : '9'
    | [1-9][0-9]*
    ;