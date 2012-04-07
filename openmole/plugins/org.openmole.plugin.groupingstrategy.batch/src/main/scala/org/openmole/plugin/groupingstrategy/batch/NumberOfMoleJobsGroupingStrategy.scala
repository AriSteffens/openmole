/*
 * Copyright (C) 2011 reuillon
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.plugin.groupingstrategy.batch

import org.openmole.core.implementation.mole.MoleJobGroup
import org.openmole.core.model.data.IContext
import org.openmole.core.model.mole.IGroupingStrategy

/**
 * Group mole jobs by group of numberOfMoleJobs.
 * 
 * @param numberOfMoleJobs size of each batch
 */
class NumberOfMoleJobsGroupingStrategy(numberOfMoleJobs: Int) extends IGroupingStrategy {

  private var currentBatchNumber = 0
  private var currentNumberOfJobs = 0

  override def group(context: IContext) = {
    val ret = new MoleJobGroup(currentBatchNumber)
    currentNumberOfJobs += 1
    if(currentNumberOfJobs >= numberOfMoleJobs) {
      currentNumberOfJobs = 0
      currentBatchNumber += 1
    }
    ret
  }
  
  override def complete(jobs: Iterable[IMoleJobs]) = jobs.size >= numberOfMoleJobs
}
