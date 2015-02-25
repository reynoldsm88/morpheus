/*
 * Copyright 2014 websudos ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.websudos.morpheus.mysql.db

import org.scalatest.FlatSpec

import com.websudos.util.testing._
import com.websudos.morpheus.mysql.tables.BasicTable
import com.websudos.morpheus.mysql._

class CreateQueryDBTest extends FlatSpec with MySQLSuite {

  it should "create a new table in the MySQL database" in {
    BasicTable.create.temporary.engine(InnoDB).execute.successful {
      res =>
    }
  }

  it should "create a new table in the database if the table doesn't exist" in {

    Console.println(BasicTable.create.ifNotExists.engine(InnoDB).queryString)


    BasicTable.create.ifNotExists.engine(InnoDB).execute.successful { _ => }
  }

}
