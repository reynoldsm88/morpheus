/*
 * Copyright 2014 websudos ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.websudos.morpheus.query

import com.websudos.morpheus.builder.{SQLBuiltQuery, AbstractSyntaxBlock, DefaultSQLSyntax, AbstractSQLSyntax}
import com.websudos.morpheus.column.AbstractColumn
import com.websudos.morpheus.dsl.BaseTable
import com.websudos.morpheus.sql.DefaultRow
import com.websudos.morpheus.{Row, SQLPrimitive}

import scala.annotation.implicitNotFound

private[morpheus] class RootInsertSyntaxBlock(query: String, tableName: String) extends AbstractSyntaxBlock {

  protected[this] val qb = SQLBuiltQuery(query)

  def into: SQLBuiltQuery = {
    qb.forcePad.append(syntax.into)
      .forcePad.appendEscape(tableName)
  }

  override def syntax: AbstractSQLSyntax = DefaultSQLSyntax
}

/**
 * This is the implementation of a root UPDATE query, a wrapper around an abstract syntax block.
 *
 * This is used as the entry point to an SQL query, and it requires the user to provide "one more method" to fully specify a SELECT query.
 * The implicit conversion from a RootSelectQuery to a SelectQuery will automatically pick the "all" strategy below.
 *
 * @param table The table owning the record.
 * @param st The Abstract syntax block describing the possible decisions.
 * @param rowFunc The function used to map a result to a type-safe record.
 * @tparam T The type of the owning table.
 * @tparam R The type of the record.
 */
private[morpheus] class RootInsertQuery[T <: BaseTable[T, _, TableRow], R, TableRow <: Row](val table: T, val st: RootInsertSyntaxBlock, val rowFunc:
TableRow => R) {

  def fromRow(r: TableRow): R = rowFunc(r)

  private[morpheus] def into: InsertQuery[T, R, TableRow, Ungroupped, Unordered, Unlimited, Unchainned, AssignUnchainned, Unterminated] = {
    new InsertQuery(table, st.into, rowFunc)
  }
}

private[morpheus] class DefaultRootInsertQuery[T <: BaseTable[T, _, DefaultRow], R]
(table: T, st: RootInsertSyntaxBlock, rowFunc: DefaultRow => R)
  extends RootInsertQuery[T, R, DefaultRow](table, st, rowFunc) {}



class InsertQuery[T <: BaseTable[T, _, TableRow],
  R,
  TableRow <: Row,
  Group <: GroupBind,
  Order <: OrderBind,
  Limit <: LimitBind,
  Chain <: ChainBind,
  AssignChain <: AssignBind,
  Status <: StatusBind
](table: T, query: SQLBuiltQuery, rowFunc: TableRow => R, val statements: List[(String, String)] = Nil, private[this] val added: Boolean = false) extends Query[T, R,
  TableRow, Group, Order, Limit, Chain, AssignChain, Status](table, query, rowFunc) {


  /**
   * At this point you may be reading and thinking "WTF", but fear not, it all makes sense. Every call to a "value method" will generate a new Insert Query,
   * but the list of statements in the new query will include a new (String, String) pair, where the first part is the column name and the second one is the
   * serialised value. This is a very simple accumulator that will eventually allow calling the "insert" method on a queryBuilder to produce the final
   * serialisation result, a hopefully valid MySQL insert query.
   *
   * @param insertion The insert condition is a pair of a column with the value to use for it. It looks like this: value(_.someColumn, someValue),
   *                  where the assignment is of course type safe.
   * @param obj The object is the value to use for the column.
   * @param primitive The primitive is the SQL primitive that converts the object into an SQL Primitive. Since the user cannot deal with types that are not
   *                  "marked" as SQL primitives for the particular database in use, we use a simple context bound to enforce this constraint.
   * @param ev1 The second evidence parameter is a restriction upon the status of a Query. Certain "exit" points mark the serialisation as Terminated with
   *            respect to the SQL syntax in use. It's a way of saying: there are no further options possible according to the DB you are using.
   * @tparam RR The SQL primitive or rather it's Scala correspondent to use at this time.
   * @return A new InsertQuery, where the list of statements in the Insert has been chained and updated for serialisation.
   */
  @implicitNotFound(msg = "To use the value method this query needs to be an insert query and the query needs to be unterminated. You probably have more " +
    "value calls than columns in your table, which would result in an invalid MySQL query.")
  final def value[RR](insertion: T => AbstractColumn[RR], obj: RR)(
    implicit primitive: SQLPrimitive[RR],
    ev1: Status =:= Unterminated
    ): InsertQuery[T, R, TableRow, Group, Order, Limit, Chain, AssignChain, Unterminated] = {

    new InsertQuery[T, R, TableRow, Group, Order, Limit, Chain, AssignChain, Unterminated](
        table, query, fromRow, Tuple2(insertion(table).name, primitive.toSQL(obj)) :: statements
    )
  }

  override def queryString: String = {
    if (added) {
      queryString
    } else {
      val columns = statements.reverse.map(_._1)
      val values = statements.reverse.map(_._2)
      table.queryBuilder.insert(query, columns, values).queryString
    }
  }

  /**
   * This is the final end point of an insert query, where through an implicit conversion in ModifyImplicts, the query is converted back to a normal Query
   * for execution or serialisation. Before any of that however, this method will carefully Terminate the query so that no further implicit conversions are
   * possible.
   *
   * E.g the user cannot go back and re-add value methods or any more things after the query is complete with respect to the MySQL syntax. Talk about making
   * illegal programming states unrepresentable.
   * @return A terminat Query, ready for execution.
   */
  private[morpheus] def toQuery: InsertQuery[T, R, TableRow, Group, Order, Limit, Chain, AssignChain, Terminated] = {
    val columns = statements.reverse.map(_._1)
    val values = statements.reverse.map(_._2)
    new InsertQuery(table, table.queryBuilder.insert(query, columns, values), fromRow, statements, true)
  }


}
