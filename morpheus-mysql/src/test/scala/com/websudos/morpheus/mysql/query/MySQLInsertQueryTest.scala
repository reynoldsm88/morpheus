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

package com.websudos.morpheus.mysql.query

import org.scalatest.{FlatSpec, Matchers}

import com.websudos.morpheus.mysql.tables.BasicTable

class  MySQLInsertQueryTest extends FlatSpec with Matchers {
  it should "serialise an INSERT IGNORE INTO query to the correct query" in {
    BasicTable.insert.ignore.queryString shouldEqual "INSERT IGNORE INTO BasicTable"
  }

  it should "serialise an INSERT HIGH_PRIORITY INTO query to the correct query" in {
    BasicTable.insert.highPriority.queryString shouldEqual "INSERT HIGH_PRIORITY INTO BasicTable"
  }

  it should "serialise an INSERT LOW_PRIORITY INTO query to the correct query" in {
    BasicTable.insert.lowPriority.queryString shouldEqual "INSERT LOW_PRIORITY INTO BasicTable"
  }

  it should "serialise an INSERT DELAYED INTO query to the correct query" in {
    BasicTable.insert.delayed.queryString shouldEqual "INSERT DELAYED INTO BasicTable"
  }
}