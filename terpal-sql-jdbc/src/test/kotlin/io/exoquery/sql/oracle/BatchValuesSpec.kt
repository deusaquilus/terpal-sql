package io.exoquery.sql.oracle

import io.exoquery.sql.*
import io.exoquery.sql.jdbc.SqlBatch
import io.exoquery.sql.jdbc.TerpalDriver
import io.exoquery.sql.runOn
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class BatchValuesSpec: FreeSpec ({
  val ds = TestDatabases.oracle
  val ctx by lazy { TerpalDriver.Oracle(ds)  }

  beforeEach {
    ds.run("""
      DELETE FROM Product;
      ALTER TABLE Product MODIFY (id GENERATED BY DEFAULT ON NULL AS IDENTITY (START WITH 1));
    """.trimIndent())
  }

  "Ex 1 - Batch Insert Normal" {
    Ex1_BatchInsertNormal.op.runOn(ctx)
    Ex1_BatchInsertNormal.get.runOn(ctx) shouldBe Ex1_BatchInsertNormal.result
  }

  "Ex 2 - Batch Insert Mixed" {
    Ex2_BatchInsertMixed.op.runOn(ctx)
    Ex2_BatchInsertMixed.get.runOn(ctx) shouldBe Ex2_BatchInsertMixed.result
  }

  "Ex 3 - Batch Return Ids" {
    val op =
      SqlBatch { p: Product -> "INSERT INTO Product (description, sku) VALUES (${p.description}, ${p.sku})" }
        .values(Ex3_BatchReturnIds.products.asSequence()).actionReturning<Int>("id").runOn(ctx) shouldBe Ex3_BatchReturnIds.opResult
    Ex3_BatchReturnIds.get.runOn(ctx) shouldBe Ex3_BatchReturnIds.result
  }

  "Ex 4 - Batch Return Record" {
    SqlBatch { p: Product -> "INSERT INTO Product (description, sku) VALUES (${p.description}, ${p.sku})" }
      .values(Ex4_BatchReturnRecord.products.asSequence()).actionReturning<Product>("id", "description", "sku").runOn(ctx) shouldBe Ex4_BatchReturnRecord.opResult
    Ex4_BatchReturnRecord.get.runOn(ctx) shouldBe Ex4_BatchReturnRecord.result
  }
})
