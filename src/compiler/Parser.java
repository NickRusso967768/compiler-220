package compiler;

import compiler.ast.*;
import java.util.List;

/**
 * Parser - Phase 2 of compilation.
 * Uses recursive descent parsing to build an Abstract Syntax Tree (AST).
 * 
 * Grammar (with operator precedence):
 *   expression  ::= term ((PLUS | MINUS) term)*
 *   term        ::= factor ((MULTIPLY | DIVIDE) factor)*
 *   factor      ::= (PLUS | MINUS) factor | NUMBER | LPAREN expression RPAREN
 */
public class Parser {
    private List<Token> tokens;
    private int pos;
    private Token current;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.current = tokens.size() > 0 ? tokens.get(0) : new Token(TokenType.EOF);
    }
    
    // Move to the next token
    private void advance() {
        pos++;
        current = (pos < tokens.size()) ? tokens.get(pos) : new Token(TokenType.EOF);
    }
    
    // Verify current token matches expected type and consume it
    private void expect(TokenType type) {
        if (current.getType() == type) {
            advance();
        } else {
            throw new RuntimeException("Expected " + type + " but got " + current.getType());
        }
    }
    
    // Parse a factor: handles numbers, parentheses, and unary operators
    // factor ::= (PLUS | MINUS) factor | NUMBER | LPAREN expression RPAREN
    private ASTNode parseFactor() {
        Token tok = current;
        
        // Handle unary + or -
        if (tok.getType() == TokenType.PLUS) {
            advance();
            return new UnaryOpNode("+", parseFactor());
        }
        
        if (tok.getType() == TokenType.MINUS) {
            advance();
            return new UnaryOpNode("-", parseFactor());
        }
        
        // Handle numbers
        if (tok.getType() == TokenType.NUMBER) {
            advance();
            return new NumberNode(Double.parseDouble(tok.getValue()));
        }
        
        // Handle parenthesized expressions
        if (tok.getType() == TokenType.LPAREN) {
            advance();
            ASTNode expr = parseExpression();
            if (current.getType() != TokenType.RPAREN) {
                throw new RuntimeException("Expected closing parenthesis");
            }
            advance();
            return expr;
        }
        
        throw new RuntimeException("Unexpected token: " + tok);
    }
    
    // Parse a term: handles * and / operations
    // term ::= factor ((MULTIPLY | DIVIDE) factor)*
    private ASTNode parseTerm() {
        ASTNode left = parseFactor();
        
        while (current.getType() == TokenType.MULTIPLY || current.getType() == TokenType.DIVIDE) {
            String op = current.getValue();
            advance();
            ASTNode right = parseFactor();
            left = new BinaryOpNode(left, op, right);
        }
        
        return left;
    }
    
    // Parse an expression: handles + and - operations
    // expression ::= term ((PLUS | MINUS) term)*
    private ASTNode parseExpression() {
        ASTNode left = parseTerm();
        
        while (current.getType() == TokenType.PLUS || current.getType() == TokenType.MINUS) {
            String op = current.getValue();
            advance();
            ASTNode right = parseTerm();
            left = new BinaryOpNode(left, op, right);
        }
        
        return left;
    }
    
    // Main parsing method - returns the root of the AST
    public ASTNode parse() {
        ASTNode tree = parseExpression();
        
        if (current.getType() != TokenType.EOF) {
            throw new RuntimeException("Unexpected tokens after expression");
        }
        
        return tree;
    }
}
