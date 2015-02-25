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

package com.websudos.morpheus.column

import com.websudos.morpheus.Row
import com.websudos.morpheus.builder.SQLBuiltQuery
import com.websudos.morpheus.dsl.BaseTable
import com.websudos.morpheus.query.QueryAssignment

import scala.reflect.runtime.{currentMirror => cm}

private[morpheus] trait SchemaSerializer {
  def qb: SQLBuiltQuery
}

trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] extends SchemaSerializer {

  type Value = T

  lazy val name: String = cm.reflect(this).symbol.name.toTypeName.decoded

  def qb: SQLBuiltQuery

  def sqlType: String

  def table: BaseTable[_, _, _]

  def toQueryString(v: T): String

  /**
   * This method, overridden by the Null and NotNull Key mixins, tells if a column is allowed to be null with respect to the SQL schema.
   * Using the stackable trait pattern it's easier for the end DSL users to mentally map a Scala schema to an SQL schema with 0 effort and inversely,
   * go from SQL to Morpheus DSL code.
   * @return A Boolean value.
   */
  def notNull: Boolean = false

}


private[morpheus] abstract class SelectColumn[T](val qb: SQLBuiltQuery) {
  def apply(r: Row): T

  def queryString: String = qb.queryString
}

private[morpheus] abstract class Column[Owner <: BaseTable[Owner, Record, TableRow], Record, TableRow <: Row, T](val table: BaseTable[Owner, Record, TableRow])
  extends AbstractColumn[T] {

  def optional(r: Row): Option[T]

  def apply(r: Row): T = optional(r).getOrElse(throw new Exception(s"can't extract required value for column '$name'"))
}


private[morpheus] abstract class AbstractModifyColumn[RR](col: AbstractColumn[RR]) {

  def setTo(value: RR): QueryAssignment = QueryAssignment(col.table.queryBuilder.setTo(col.name, col.toQueryString(value)))
}
