# jsonparser
A JSON parser that generates an AST and supports ${...} placeholder expression expansion in values

# Building

You need Maven 3.x and JDK 1.8

```
mvn package
```

# Parsing JSON 

To get the abstract syntax tree (AST) for an arbitrary JSON string:

```
  ASTNode ast = new Parser().parse( "{}" );
```

By default the parser accepts placeholder expressions ${identifier} as values ; do 

```
   Parser p = new Parser();
   p.setSupportsPlaceholders(false);
```

to make the parser fail on these instead. Note that placeholder expressions inside string literals are *not* separate AST nodes and you need to call StringLiteral#getPlaceholderNames() to get them.

# Printing JSON

This library comes with a very basic pretty-printer.

Do 

```
   String json = new ASTPrinter().print( ast ); 
```

to turn an AST into a JSON string. You may want to override ASTPrinter#resolvePlaceholder(Identifier) to resolve placeholders.
