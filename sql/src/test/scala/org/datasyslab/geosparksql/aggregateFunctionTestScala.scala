/*
 * FILE: aggregateFunctionTestScala.scala
 * Copyright (c) 2015 - 2018 GeoSpark Development Team
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.datasyslab.geosparksql

import com.vividsolutions.jts.geom.{Coordinate, Geometry, GeometryFactory}
import org.opensphere.geometry.algorithm.ConcaveHull


class aggregateFunctionTestScala extends TestBaseScala {

  describe("GeoSpark-SQL Aggregate Function Test") {

    it("Passed ST_Envelope_aggr") {
      var pointCsvDF = sparkSession.read.format("csv").option("delimiter", ",").option("header", "false").load(csvPointInputLocation)
      pointCsvDF.createOrReplaceTempView("pointtable")
      var pointDf = sparkSession.sql("select ST_Point(cast(pointtable._c0 as Decimal(24,20)), cast(pointtable._c1 as Decimal(24,20))) as arealandmark from pointtable")
      pointDf.createOrReplaceTempView("pointdf")
      var boundary = sparkSession.sql("select ST_Envelope_Aggr(pointdf.arealandmark) from pointdf")
      val coordinates: Array[Coordinate] = new Array[Coordinate](5)
      coordinates(0) = new Coordinate(1.1, 101.1)
      coordinates(1) = new Coordinate(1.1, 1100.1)
      coordinates(2) = new Coordinate(1000.1, 1100.1)
      coordinates(3) = new Coordinate(1000.1, 101.1)
      coordinates(4) = coordinates(0)
      val geometryFactory = new GeometryFactory()
      geometryFactory.createPolygon(coordinates)
      assert(boundary.take(1)(0).get(0) == geometryFactory.createPolygon(coordinates))
    }

    it("Passed ST_Union_aggr") {

      var polygonCsvDf = sparkSession.read.format("csv").option("delimiter", ",").option("header", "false").load(unionPolygonInputLocation)
      polygonCsvDf.createOrReplaceTempView("polygontable")
      polygonCsvDf.show()
      var polygonDf = sparkSession.sql("select ST_PolygonFromEnvelope(cast(polygontable._c0 as Decimal(24,20)),cast(polygontable._c1 as Decimal(24,20)), cast(polygontable._c2 as Decimal(24,20)), cast(polygontable._c3 as Decimal(24,20))) as polygonshape from polygontable")
      polygonDf.createOrReplaceTempView("polygondf")
      polygonDf.show()
      var union = sparkSession.sql("select ST_Union_Aggr(polygondf.polygonshape) from polygondf")
      assert(union.take(1)(0).get(0).asInstanceOf[Geometry].getArea == 10100)
    }

    it("Passed ST_Concave_aggr") {

      var polygonCsvDf = sparkSession.read.format("csv").option("delimiter", ",").option("header", "false").load(unionPolygonInputLocation)
      polygonCsvDf.createOrReplaceTempView("polygontable")
      polygonCsvDf.show()
      var polygonDf = sparkSession.sql("select ST_PolygonFromEnvelope(cast(polygontable._c0 as Decimal(24,20)),cast(polygontable._c1 as Decimal(24,20)), cast(polygontable._c2 as Decimal(24,20)), cast(polygontable._c3 as Decimal(24,20))) as polygonshape from polygontable")
      polygonDf.createOrReplaceTempView("polygondf")
      polygonDf.show()
      var concavehull = sparkSession.sql("select ST_ConcaveHull_Aggr(polygondf.polygonshape) from polygondf")
      assert(concavehull.take(1)(0).get(0).asInstanceOf[Geometry] == 10100)
    }
  }
}
