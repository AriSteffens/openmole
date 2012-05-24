/*
 * Copyright (C) 2012 mathieu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.ide.core.implementation.workflow

import org.openmole.ide.core.model.dataproxy.IPrototypeDataProxyUI
import org.openmole.ide.core.model.dataproxy.ITaskDataProxyUI
import org.openmole.ide.core.model.workflow._

class DataChannelUI(val source: ICapsuleUI,
                    val target: ICapsuleUI,
                    var filteredPrototypes: List[IPrototypeDataProxyUI] = List.empty) extends IDataChannelUI {

  def availablePrototypes = source.dataUI.task match {
    case Some(x: ITaskDataProxyUI) ⇒
      x.dataUI.prototypesOut ++
        x.dataUI.implicitPrototypesOut
    case None ⇒ List.empty
  }

  def nbPrototypes = {
    val availables = availablePrototypes
    filteredPrototypes = filteredPrototypes.filter { p ⇒ availables.contains(p) }
    availables.size - filteredPrototypes.size
  }
}