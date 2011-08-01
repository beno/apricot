/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */

package org.eclipse.ecr.core.query.sql.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.eclipse.ecr.core.query.sql.SQLQueryParser;

/**
 * Simple test of the visitor using a dumb printer.
 * <p>
 * Also tests SQLQuery.toString() while we're at it.
 *
 * @author Florent Guillaume
 */
public class TestQueryVisitor extends TestCase {

    private static void check(String sql, String expected) {
        PrintVisitor v = new PrintVisitor();
        SQLQuery query = SQLQueryParser.parse(sql);
        assertEquals(sql, query.toString());
        v.visitQuery(query);
        assertEquals(expected, v.toString());
    }

    public void testRemoveTZSuffixes() {
        assertEquals("000", removeTzSuffix("000+00:00')"));
        assertEquals("000", removeTzSuffix("000Z')"));
    }


    public void testVisitor() throws Exception {
        String sql;
        String expected;

        sql = "select p as p from t where title=\"%test\"";
        expected = "SELECT p FROM t WHERE (title = '%test')";
        check(sql, expected);

        sql = "select p from t where foo in (1, 2)";
        expected = "SELECT p FROM t WHERE (foo IN (1, 2))";
        check(sql, expected);

        sql = "SELECT p, q AS qq, f(x) FROM t, u, v" + //
                " WHERE title = 'ab' AND des = 'cd'" + //
                " ORDER BY x DESC,y,z  DESC" + //
                " LIMIT 8   OFFSET 43";
        expected = "SELECT p, q AS qq, f(x) FROM t, u, v" + //
                " WHERE ((title = 'ab') AND (des = 'cd'))" + //
                " ORDER BY x DESC, y, z DESC" + //
                " LIMIT 8 OFFSET 43";
        check(sql, expected);

        sql = "select foo from docs";
        expected = "SELECT foo FROM docs";
        check(sql, expected);

        sql = "select * from d where foo <> DATE '2008-01-01'";
        expected = "SELECT * FROM d WHERE (foo <> DATE '2008-01-01')";
        check(sql, expected);

        sql = "select * from d where foo between DATE '2008-01-01' and DATE '2008-02-01'";
        expected = "SELECT * FROM d WHERE (foo BETWEEN DATE '2008-01-01' AND DATE '2008-02-01')";
        check(sql, expected);

        // workaround on timezone variations for this test
        sql = "select * from d where foo = TIMESTAMP '2008-08-08 12:34:56'";
        expected = "SELECT * FROM d WHERE (foo = TIMESTAMP '2008-08-08T12:34:56.000+00:00')";

        PrintVisitor v = new PrintVisitor();
        v.visitQuery(SQLQueryParser.parse(sql));
        String got = v.toString();
        assertEquals(removeTzSuffix(expected), removeTzSuffix(got));

        sql = "select * from d where a = 2 OR NOT b = 5";
        expected = "SELECT * FROM d WHERE ((a = 2) OR (NOT (b = 5)))";
        check(sql, expected);

        sql = "select * from d where NOT (a = 2 OR b = 5)";
        expected = "SELECT * FROM d WHERE (NOT ((a = 2) OR (b = 5)))";
        check(sql, expected);

        sql = "select foo from docs where x = 1 AND x=2 AND x = 3";
        expected = "SELECT foo FROM docs WHERE AND(x = 1, x = 2, x = 3)";
        SQLQuery query = SQLQueryParser.parse(sql);
        // AndExpression is not generated by the parser, build it by hand
        Predicate pred = query.where.predicate;
        List<Operand> operands = new LinkedList<Operand>();
        operands.add(((Expression) pred.lvalue).lvalue);
        operands.add(((Expression) pred.lvalue).rvalue);
        operands.add(pred.rvalue);
        query = new SQLQuery(query.select, query.from, new WhereClause(
                new MultiExpression(Operator.AND, operands)), query.groupBy,
                query.having, query.orderBy);
        v = new PrintVisitor();
        v.visitQuery(query);
        assertEquals(expected, v.toString());

        sql = "select * from d where b IS NULL or b IS NOT NULL";
        expected = "SELECT * FROM d WHERE ((IS NULL b) OR (IS NOT NULL b))";
        check(sql, expected);
    }

    private static final Pattern REMOVE_TZ_PATTERN = Pattern.compile("(.*)((\\+|-).*|Z)'\\)$");

    private String removeTzSuffix(String value) {
        Matcher matcher = REMOVE_TZ_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AssertionError(REMOVE_TZ_PATTERN + " pattern does not match " + value);
        }
        return matcher.group(1);
    }

}

class PrintVisitor extends DefaultQueryVisitor {

    private static final long serialVersionUID = 1L;

    public StringBuilder buf = new StringBuilder();

    @Override
    public String toString() {
        return buf.toString();
    }

    @Override
    public void visitQuery(SQLQuery node) {
        super.visitQuery(node);
        if (node.limit != 0) {
            buf.append(" LIMIT ");
            buf.append(node.limit);
            if (node.offset != 0) {
                buf.append(" OFFSET ");
                buf.append(node.offset);
            }
        }
    }

    @Override
    public void visitSelectClause(SelectClause node) {
        buf.append("SELECT ");
        SelectList elements = node.elements;
        if (elements.isEmpty()) {
            buf.append("*");
        } else {
            for (int i = 0; i < elements.size(); i++) {
                if (i != 0) {
                    buf.append(", ");
                }
                Operand op = elements.get(i);
                String alias = elements.getKey(i);
                elements.get(i).accept(this);
                if (!alias.equals(op.toString())) {
                    buf.append(" AS ");
                    buf.append(alias);
                }
            }
        }
    }

    @Override
    public void visitFromClause(FromClause node) {
        buf.append(" FROM ");
        FromList elements = node.elements;
        for (int i = 0; i < elements.size(); i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(elements.get(i));
        }
    }

    @Override
    public void visitWhereClause(WhereClause node) {
        buf.append(" WHERE ");
        super.visitWhereClause(node);
    }

    @Override
    public void visitGroupByClause(GroupByClause node) {
        String[] elements = node.elements;
        if (elements.length == 0) {
            return;
        }
        buf.append(" GROUP BY ");
        for (int i = 0; i < elements.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(elements[i]);
        }
    }

    @Override
    public void visitHavingClause(HavingClause node) {
        if (node.predicate != null) {
            buf.append(" HAVING ");
            super.visitHavingClause(node);
        }
    }

    @Override
    public void visitOrderByClause(OrderByClause node) {
        if (node.elements.size() == 0) {
            return;
        }
        buf.append(" ORDER BY ");
        super.visitOrderByClause(node);
    }

    @Override
    public void visitOrderByList(OrderByList node) {
        for (int i = 0; i < node.size(); i++) {
            if (i != 0) {
                buf.append(", ");
            }
            node.get(i).accept(this);
        }
    }

    @Override
    public void visitOrderByExpr(OrderByExpr node) {
        super.visitOrderByExpr(node);
        if (node.isDescending) {
            buf.append(" DESC");
        }
    }

    @Override
    public void visitExpression(Expression node) {
        buf.append('(');
        if (node.rvalue == null) {
            // NOT
            node.operator.accept(this);
            buf.append(' ');
            node.lvalue.accept(this);
        } else if (node.operator == Operator.BETWEEN) {
            LiteralList l = (LiteralList) node.rvalue;
            node.lvalue.accept(this);
            buf.append(' ');
            node.operator.accept(this);
            buf.append(' ');
            l.get(0).accept(this);
            buf.append(" AND ");
            l.get(1).accept(this);
        } else {
            node.lvalue.accept(this);
            buf.append(' ');
            node.operator.accept(this);
            buf.append(' ');
            node.rvalue.accept(this);
        }
        buf.append(')');
    }

    @Override
    public void visitMultiExpression(MultiExpression node) {
        node.operator.accept(this);
        buf.append('(');
        for (Iterator<Operand> it = node.values.iterator(); it.hasNext();) {
            StringBuilder bak = buf;
            buf = new StringBuilder();
            it.next().accept(this);
            if (buf.charAt(0) == '(' && buf.charAt(buf.length() - 1) == ')') {
                buf.deleteCharAt(0);
                buf.deleteCharAt(buf.length() - 1);
            }
            bak.append(buf);
            buf = bak;
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(')');
    }

    @Override
    public void visitOperator(Operator node) {
        buf.append(node.toString());
    }

    @Override
    public void visitReference(Reference node) {
        buf.append(node.name);
    }

    @Override
    public void visitReferenceList(ReferenceList node) {
        for (int i = 0; i < node.size(); i++) {
            if (i != 0) {
                buf.append(", ");
            }
            node.get(i).accept(this);
        }
    }

    @Override
    public void visitLiteral(Literal node) {
    }

    @Override
    public void visitLiteralList(LiteralList node) {
        buf.append('(');
        for (Iterator<Literal> it = node.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(')');
    }

    @Override
    public void visitDateLiteral(DateLiteral node) {
        buf.append(node.toString());
    }

    @Override
    public void visitStringLiteral(StringLiteral node) {
        buf.append(node.toString());
    }

    @Override
    public void visitDoubleLiteral(DoubleLiteral node) {
        buf.append(node.toString());
    }

    @Override
    public void visitIntegerLiteral(IntegerLiteral node) {
        buf.append(node.toString());
    }

    @Override
    public void visitFunction(Function node) {
        buf.append(node.name);
        buf.append("(");
        for (Iterator<Operand> it = node.args.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
    }

    @Override
    public void visitOperandList(OperandList node) {
        for (Iterator<Operand> it = node.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
    }

}
