/*
 * Copyright 2013 - 2017 Outworkers, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.outworkers.morpheus

import com.outworkers.morpheus.dsl.DefaultImportsDefinition
import com.outworkers.morpheus.operators.MySQLOperatorSet
import com.outworkers.morpheus.query.AssignUnchainned
import com.outworkers.morpheus.column.{AbstractColumn, DefaultForeignKeyConstraints}
import com.outworkers.morpheus.mysql.query.{MySQLRootSelectQuery, MySQLSelectQuery}
import com.outworkers.morpheus.query.{Unchainned, Ungroupped, Unlimited, Unordered}
import shapeless.HNil

import scala.util.Try

package object mysql extends DefaultImportsDefinition
  with MySQLImplicits
  with DataTypes
  with MySQLOperatorSet
  with MySQLColumns
  with MySQLKeys
  with MySQLPrimitiveColumns
  with DefaultForeignKeyConstraints {

  override implicit def columnToQueryColumn[T : DataType](col: AbstractColumn[T]): MySQLQueryColumn[T] = new MySQLQueryColumn[T](col)

  implicit def rootSelectQueryToQuery[T <: Table[T, _], R](
    root: MySQLRootSelectQuery[T, R]
  ): MySQLSelectQuery[T, R, Ungroupped, Unordered, Unlimited, Unchainned, AssignUnchainned, HNil] = {
    new MySQLSelectQuery(
      root.table,
      root.st.*,
      root.rowFunc
    )
  }

  type SQLTable[Owner <: BaseTable[Owner, Record, MySQLRow], Record] = MySQLTable[Owner, Record]

  type Row = MySQLRow
  type Result = MySQLResult

  type Table[Owner <: BaseTable[Owner, Record, MySQLRow], Record] = MySQLTable[Owner, Record]

  def enumToQueryConditionPrimitive[T <: Enumeration](enum: T)(implicit ev: DataType[String]): DataType[T#Value] = {
    new DataType[T#Value] {

      override def sqlType: String = ev.sqlType

      override def deserialize(row: com.outworkers.morpheus.Row, name: String): Try[T#Value] = {
        row.string(name) map { s => enum.withName(s) }
      }

      override def serialize(value: T#Value): String = ev.serialize(value.toString)
    }
  }
}
