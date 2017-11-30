/*
 * Copyright (C) 16/02/13 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.core.workflow.task

import org.openmole.core.context.Val
import org.openmole.core.exception.InternalProcessingError
import org.openmole.core.workflow.data._
import org.scalatest._
import org.openmole.core.workflow.mole._
import org.openmole.core.workflow.transition._
import org.openmole.core.workflow.puzzle._
import org.openmole.core.workflow.builder._
import org.openmole.core.workflow.dsl._

import scala.util.Try

class MoleTaskSpec extends FlatSpec with Matchers {

  import org.openmole.core.workflow.tools.StubServices._

  "Implicits" should "work with mole task" in {
    val i = Val[String]("i")
    val emptyT = EmptyTask() set (inputs += i)
    val emptyC = Capsule(emptyT)

    val moleTask =
      MoleTask(Mole(emptyC), emptyC) set (
        implicits += i,
        i := "test"
      )

    MoleExecution(Mole(moleTask)).start.waitUntilEnded
  }

  "MoleTask" should "propagate errors" in {
    val error =
      TestTask { _ ⇒ throw new InternalProcessingError("Some error for test") } set (
        name := "error"
      )

    val moleTask = MoleTask(error)

    val ex = moleTask.start
    Try { ex.waitUntilEnded }

    ex.exception shouldNot equal(None)

  }

  "MoleTask" should "provide its inputs to the first capsule if it is a strainer" in {
    val tm1 = Capsule(EmptyTask(), strain = true)

    val i = Val[String]("i")

    val emptyT = EmptyTask() set (inputs += i, name := "EmptyT")
    val emptyC = Capsule(emptyT)

    val moleTask = MoleTask(tm1 -- emptyC, emptyC) set (
      inputs += i,
      i := "test"
    )

    MoleExecution(Mole(moleTask)).start.waitUntilEnded
  }

}
